package me.kixstar.kixbungeebridge.feature;

import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.rabbitmq.*;
import me.kixstar.kixbungeebridge.rabbitmq.teleport.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TeleportTransaction implements Listener {

    private static ConcurrentHashMap<String, TeleportTransaction> instances = new ConcurrentHashMap<>();

    public static TeleportTransaction playerToPlayer(ProxiedPlayer player, ProxiedPlayer target) {
        return playerToPlayer(player, target, 0);
    }

    public static TeleportTransaction playerToPlayer(ProxiedPlayer player, ProxiedPlayer target, int delay) {
        TeleportTransaction transaction =  new TeleportTransaction(player, delay);
        transaction.teleportType = true;
        transaction.targetPlayer = target;

        return transaction;
    }

    public static TeleportTransaction playerToLocation(ProxiedPlayer player, Location location) {
        return playerToLocation(player, location, 0);
    }

    public static TeleportTransaction playerToLocation(ProxiedPlayer player, Location location, int delay) {
        TeleportTransaction transaction = new TeleportTransaction(player, delay);
        transaction.teleportType = false;
        transaction.targetLocation = location;

        return transaction;
    }

    private static ProtocolChannelOutput PCO = new ProtocolChannelOutput(new TeleportProtocol());

    private TeleportTransaction(ProxiedPlayer player, int delay) {
        this.player = player;
        this.delay = delay;
        this.transactionID = UUID.randomUUID().toString();
        this.playerServerConfirm = new AtomicBoolean(false);
        this.targetServerConfirm = new AtomicBoolean(false);
        this.delayWaited = new AtomicBoolean(true);
        if(this.delay != 0) {
            this.delayWaited.set(false);
            this.waitDelayAsync().thenApply((ignore) -> {
                this.delayWaited.set(true);
                if(this.playerServerConfirm.get() && this.targetServerConfirm.get()) execute();
                return ignore;
            });
        }
        this.bind();
        instances.put(this.transactionID, this);
    }

    private String transactionID;

    private AtomicBoolean playerServerConfirm;

    private AtomicBoolean targetServerConfirm;

    private AtomicBoolean delayWaited;

    //false - player to location, true - player to player
    private boolean teleportType;

    private ProxiedPlayer player;

    private ProxiedPlayer targetPlayer;

    private Location targetLocation;

    private int delay;

    private ProtocolChannelInput PCI = new ProtocolChannelInput(new TeleportProtocol()) {
        @Override
        //runs on a different thread, needs to be synchronised https://rabbitmq.github.io/rabbitmq-java-client/api/4.x.x/com/rabbitmq/client/Consumer.html
        public void onPacket(Packet frame) {

            if(frame instanceof CancelTeleportPacket) {
                if(!frame.getProperties().getHeaders().get("origin").equals("bungee")) unbind();
            }

            if(frame instanceof ConfirmReadyTeleportPacket) {
                ConfirmReadyTeleportPacket packet = (ConfirmReadyTeleportPacket) frame;

                //if both servers say they own the player or the target cancel the teleport.
                if(packet.isPlayerSide() && !playerServerConfirm.compareAndSet(false, true)) cancel();
                if(packet.isTargetSide() && !targetServerConfirm.compareAndSet(false, true)) cancel();

                if(playerServerConfirm.get() && targetServerConfirm.get() && delayWaited.get()) execute();
            }
        }
    };

    private CompletableFuture<Void> waitDelayAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(this.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });

    }

    @EventHandler
    public void onSwitchServer(ServerSwitchEvent event) {
        ProxiedPlayer p = event.getPlayer();
        if(!(this.player.equals(p) || (this.teleportType && this.targetPlayer.equals(p)))) return;
        //disconnect spigot servers from this transaction
        this.fakeCancel();
        this.notifyTransaction().thenApply(success -> {
           if(success) {
               this.sendTeleportInfo();
           } else {
               this.cancel();
           }
           return success;
        });
    }

    @EventHandler
    public void onDisconnect(ServerDisconnectEvent event) {
        ProxiedPlayer p = event.getPlayer();
        if(!(this.player.equals(p) || (this.teleportType && this.targetPlayer.equals(p)))) return;
        this.cancel();
    }

    public ServerInfo getTargetServer() {
        if(teleportType) {
            return KixBungeeBridge.getInstance().getProxy().getServerInfo(this.targetLocation.getServerName());
        } else {
            return this.targetPlayer.getServer().getInfo();
        }
    }

    public CompletableFuture<Boolean> notifyTransaction() {
        CompletableFuture<Boolean> playerServerACK = ServerCommandService.get().subscribeTeleport(this.transactionID, this.player.getServer());
        CompletableFuture<Boolean> targetServerACK = ServerCommandService.get().subscribeTeleport(this.transactionID, this.getTargetServer().getName());

        return CompletableFuture.supplyAsync(() -> playerServerACK.join() && targetServerACK.join());
    }

    public void init() {
        ServerCommandService.get().subscribeTeleport(this.transactionID, this.player.getServer().getInfo().getName());
        ServerCommandService.get().subscribeTeleport(this.transactionID, this.getTargetServer().getName());
    }

    //sends a cancel packet that is used to force spigot instances to disconnect their consumers
    //used when players change the server to force the old server to disconnect from RabbitMQ
    public void fakeCancel() {
        Packet packet = new CancelTeleportPacket();

        PCO.sendPacket(packet, this.transactionID);
    }

    public void cancel() {
        this.fakeCancel();
        this.unbind();
    }

    public void sendTeleportInfo() {
        Packet packet;
        if(this.teleportType) {
            //player to player teleport
            packet = new PlayerTeleportPacket(
                    this.player.getUniqueId().toString(),
                    this.targetPlayer.getUniqueId().toString()
            );
        } else {
            packet = new LocationTeleportPacket(
                    this.player.getUniqueId().toString(),
                    this.targetLocation.getServerName(),
                    this.targetLocation.getWorldName(),
                    this.targetLocation.getX(),
                    this.targetLocation.getY(),
                    this.targetLocation.getZ(),
                    this.targetLocation.getYaw(),
                    this.targetLocation.getPitch()
            );
        }

        PCO.sendPacket(packet, this.transactionID);

    }

    //executes a teleport transaction
    public CompletableFuture<Boolean> execute() {
        Packet packet = new ExecuteTeleportPacket();
        PCO.sendPacket(packet, this.transactionID);
        CompletableFuture cb = new CompletableFuture();

        player.connect(this.getTargetServer(), (result, error) -> {
            if(!result) player.sendMessage(new TextComponent(error.getMessage()));
            cb.complete(result);
        });

        return cb;
    }

    public static void register() {
        PCO.bind(RabbitMQ.getChannel(), "teleport", "direct");
    }

    public static void unregister() {
        PCO.unbind();
        instances.values().forEach(transaction -> {
            transaction.unbind();
        });
    }

    public void bind() {
        Plugin plugin = KixBungeeBridge.getInstance();
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
        this.PCI.bind(RabbitMQ.getChannel(), "teleport", "direct", this.transactionID);

    }

    public synchronized void unbind() {
        Plugin plugin = KixBungeeBridge.getInstance();
        plugin.getProxy().getPluginManager().unregisterListener(this);
        this.PCI.unbind();
    }

}
