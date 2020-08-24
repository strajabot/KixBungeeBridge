package me.kixstar.kixbungeebridge.feature;

import com.rabbitmq.client.AMQP;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.rabbitmq.*;
import me.kixstar.kixbungeebridge.rabbitmq.servercommand.CommandStatusPacket;
import me.kixstar.kixbungeebridge.rabbitmq.servercommand.ServerCommandProtocol;
import me.kixstar.kixbungeebridge.rabbitmq.servercommand.SubscribeTeleportPacket;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerCommandService {

    //correlationID -> callback map
    private ConcurrentHashMap<String, CompletableFuture<Boolean>> correlationCBMap = new ConcurrentHashMap<>();

    private ProtocolChannelOutput PCO = new ProtocolChannelOutput(new ServerCommandProtocol());

    private ProtocolChannelInput PCI = new ProtocolChannelInput(new ServerCommandProtocol()) {
        @Override
        public void onPacket(Packet in) {
            String correlationID = in.getProperties().getCorrelationId();
            if(correlationID == null) return;

            CompletableFuture<Boolean> reqSuccess = correlationCBMap.remove(correlationID);
            if(reqSuccess == null) return;

            CommandStatusPacket packet = (CommandStatusPacket) in;

            reqSuccess.complete(packet.getStatus());
        }
    };

    public static final String RESPONSE_ROUTE = "server-command-reply";

    private static ServerCommandService instance = new ServerCommandService();

    public static ServerCommandService get() {
        return instance;
    }

    private ServerCommandService() {}

    //completes with false if the server is deemed unreachable or if the server returns a negative status.
    public CompletableFuture<Boolean> subscribeTeleport(String transactionID, String serverName) {
        final String correlationId = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .replyTo(RESPONSE_ROUTE)
                .build();
        SubscribeTeleportPacket packet = new SubscribeTeleportPacket(transactionID);
        packet.setProperties(props);
        this.PCO.sendPacket(packet, serverName);
        CompletableFuture<Boolean> callback = new CompletableFuture<>();
        this.correlationCBMap.put(correlationId, callback);
        Plugin plugin = KixBungeeBridge.getInstance();
        TaskScheduler scheduler = plugin.getProxy().getScheduler();
        scheduler.schedule(plugin, () -> {
            //if the server doesn't respond in 10 seconds return false to CompletableFuture;
            CompletableFuture<Boolean> reqFail  = this.correlationCBMap.remove(correlationId);
            if(reqFail != null) reqFail.complete(false);
        }, 10, TimeUnit.SECONDS);

        return  callback;
    }

    public CompletableFuture<Boolean> subscribeTeleport(String transactionID, Server server) {
        return this.subscribeTeleport(transactionID, server.getInfo().getName());
    }

    public static void register() {
        instance.PCO.bind(RabbitMQ.getChannel(), "server-command", "direct");
        instance.PCI.bind(RabbitMQ.getChannel(), "", "direct", RESPONSE_ROUTE);
    }

    public static void unregister() {
        instance.PCO.unbind();
        instance.PCI.unbind();
    }


}
