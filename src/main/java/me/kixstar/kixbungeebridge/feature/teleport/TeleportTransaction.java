package me.kixstar.kixbungeebridge.feature.teleport;

import com.rabbitmq.client.AMQP;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.database.entities.Location;
import me.kixstar.kixbungeebridge.feature.ServerCommandService;
import me.kixstar.kixbungeebridge.rabbitmq.*;
import me.kixstar.kixbungeebridge.rabbitmq.teleport.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;
import java.util.concurrent.*;

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
        this.delayWaited = new CompletableFuture<>();
        this.stateFuture = new CompletableFuture<>();
        this.bind();
        instances.put(this.transactionID, this);
    }

    private final String transactionID;

    private TransactionState transactionState;

    private CompletableFuture<Void> stateFuture;

    private ConcurrentHashMap<String, ConfirmState> confirms = new ConcurrentHashMap<>();

    private CompletableFuture<Boolean> delayWaited;

    //false - player to location, true - player to player
    private boolean teleportType;

    private ProxiedPlayer player;

    private ProxiedPlayer targetPlayer;

    private Location targetLocation;

    private int delay;

    private ProtocolChannelInput PCI = new ProtocolChannelInput(new TeleportProtocol()) {
        @Override
        //runs on a different thread, needs to be synchronised https://rabbitmq.github.io/rabbitmq-java-client/api/4.x.x/com/rabbitmq/client/Consumer.html
        public void onPacket(Packet in) {
            if(in instanceof CancelTeleportPacket) {
                CancelTeleportPacket packet = (CancelTeleportPacket) in;
                try {
                    if (!RabbitMQ.isFromThisServer(in)) {
                        try {
                            setState(TransactionState.CANCELED);
                        } catch (TransactionFinishedException | TransactionCancelledException e) {
                            //shouldn't ever happen because of the if check
                        }
                    }
                } catch (UnknownPacketOriginException e) {
                    //if we cannot find the packet sender cancel just in case
                    cancel("An unknown server cancelled your teleport, please report to the dev team");
                }
            } else if(in instanceof ConfirmReadyTeleportPacket) {
                ConfirmReadyTeleportPacket packet = (ConfirmReadyTeleportPacket) in;
                ConfirmState confirmState = confirms.get(packet.getProperties().getCorrelationId());
                if(confirmState == null) return;
                confirmState.handleConfirmReadyTeleport(packet);
                if(!confirmState.getFuture().isDone()) return;
                if(!confirmState.getFuture().isDone()) return;
                confirms.remove(packet.getProperties().getCorrelationId());
                try {
                    confirmState.getFuture().get();
                } catch (CancellationException | InterruptedException | ExecutionException e) {
                    cancel(e.getMessage());
                }
            }
        }
    };

    private void waitDelayAsync() {
        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(this.delay);
                this.delayWaited.complete(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.delayWaited.complete(false);
            return null;
        });
    }

    @EventHandler
    public void onSwitchServer(ServerSwitchEvent event) {
        ProxiedPlayer p = event.getPlayer();

        if(!(this.player.equals(p) || (this.teleportType && this.targetPlayer.equals(p)))) return;
        //disconnect spigot servers from this transaction
        this.fakeCancel();
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer p = event.getPlayer();

        if(this.player.equals(p) || (this.teleportType && this.targetPlayer.equals(p)))
                this.cancel("Teleport canceled since player " + p.getDisplayName() + " disconnected from the server");
    }


    public ServerInfo getTargetServer() {
        if(teleportType) {
            return this.targetPlayer.getServer().getInfo();
        } else {
            return KixBungeeBridge.getInstance().getProxy().getServerInfo(this.targetLocation.getServerName());
            
        }
    }

    /**
     *  Sends {@link me.kixstar.kixbungeebridge.rabbitmq.servercommand.SubscribeTeleportPacket} to the player and target server (sends it only once if it's the same server)
     *  Returns a CompletableFuture that completes with true if both the player and target server correctly subscribed
     *  to the transaction.
     *
     * @return A future that represents the subscription state
     */
    private CompletableFuture<Boolean> notifyTransaction() {
        ServerCommandService service = ServerCommandService.get();
        CompletableFuture<Boolean> cf = new CompletableFuture<>();
        boolean isSameServer = (this.player.getServer().getInfo().equals(this.getTargetServer()));

        //default to completed with true, used when only one server is needs to subscribe
        CompletableFuture<Boolean> targetServerACK = new CompletableFuture<>();
        targetServerACK.complete(true);

        CompletableFuture<Boolean> playerServerACK = service.subscribeTeleport(
                this.transactionID,
                this.player.getServer()
        );
        //makes sure that the same packet doesn't get sent to the same server twice.
        if(!isSameServer) {
            targetServerACK = ServerCommandService.get().subscribeTeleport(
                    this.transactionID,
                    this.getTargetServer().getName()
            );
        }

        //targetServerACK needs to be effectively final fix.
        CompletableFuture<Boolean> finalTargetServerACK = targetServerACK;
        CompletableFuture.supplyAsync(() -> {
            try {
                cf.complete (playerServerACK.get() && finalTargetServerACK.get());
            } catch (InterruptedException | ExecutionException e) {
                cf.complete(false);
            }
            return null;
        });
        return cf;
    }

    public void init() {
        try {
            this.setState(TransactionState.INITIAL);
        } catch (TransactionFinishedException e) {
            e.printStackTrace();
        } catch (TransactionCancelledException e) {
            e.printStackTrace();
        }

    }

    private void setState(TransactionState state) throws TransactionFinishedException, TransactionCancelledException {
        synchronized (this.stateFuture) {
            if(this.transactionState == TransactionState.CANCELED) throw new TransactionCancelledException();
            if(this.transactionState == TransactionState.FINISHED) throw new TransactionFinishedException();
            CompletableFuture oldFuture = this.stateFuture;
            //set new state
            this.stateFuture = new CompletableFuture<>();
            this.transactionState = state;
            //cancel all callbacks related to the old state
            if (oldFuture.isDone() == false) oldFuture.cancel(false);
            switch (state) {
                case INITIAL:
                    this.stateInitial();
                    break;
                case CONTACT:
                    this.stateContact();
                    break;
                case INFORMATION:
                    this.stateInformation();
                    break;
                case EXECUTION:
                    this.stateExecution();
                    break;
                case FINISHED:
                    if(!player.getServer().getInfo().equals(this.getTargetServer())) this.player.connect(this.getTargetServer());
                    this.unbind();
                    break;
                case CANCELED:
                    //see TeleportTransaction::cancel()
                    this.unbind();
                    break;
            }
        }
    }

    private void stateInitial() {
        this.waitDelayAsync();
        try {
            this.setState(TransactionState.CONTACT);
        } catch (TransactionFinishedException | TransactionCancelledException e) {
            //this shouldn't ever happen since INITIAL state is the first state in the chain
            e.printStackTrace();
        }
    }

    private void stateContact() {
        //save the reference if it changes while code runs async
        CompletableFuture<Void> currentStateFuture = this.stateFuture;

        this.notifyTransaction().thenApply((success) -> {
            //synchronise to this.stateFuture  to make sure that the state can't change during execution.
            synchronized (this.stateFuture) {
                try {

                    //used to check if the state has changed and ignore if it did;
                    currentStateFuture.getNow(null);

                    if (!success) {
                        this.cancel("Server instances couldn't subscribe to the transaction correctly, report to staff");
                        return false;
                    }

                    this.setState(TransactionState.INFORMATION);
                    return true;
                } catch (CancellationException e) {
                    //state of transaction has changed, do notihing
                } catch (TransactionFinishedException | TransactionCancelledException e) {
                    //this can't happen since currentStateFuture would throw CancellationException???
                    this.cancel(e.getMessage());
                }
            }
            return false;
        });

    }

    public void stateInformation() {
        CompletableFuture<Void> currentStateFuture = this.stateFuture;

        ConfirmState confirmState = this.sendTeleportInfo();

        CompletableFuture.supplyAsync(() -> {
            try {
                confirmState.getFuture().get();
                return this.delayWaited.get();
            } catch (CancellationException | InterruptedException | ExecutionException  e) {
                this.cancel(e.getMessage());
                return false;
            }
        }).thenApply((success) -> {
            synchronized (this.stateFuture) {
                try {

                    //used to check if the state has changed and ignore if it did;
                    currentStateFuture.getNow(null);

                    //this is probably unnecessary since
                    if (!success)  {
                        this.cancel("Teleport was cancelled in the INFORMATION stage due to an unknown error, please report to staff");
                        return false;
                    }

                    this.setState(TransactionState.EXECUTION);
                    return true;
                    //ignore
                } catch (CancellationException e) {
                    //state of transaction has changed, do notihing
                } catch (TransactionFinishedException | TransactionCancelledException e) {
                    this.cancel(e.getMessage());
                }
            }
            return false;
        });
    }

    private void stateExecution() {
        CompletableFuture<Void> currentStateFuture = this.stateFuture;

        ConfirmState confirmState = this.sendExecute();

        CompletableFuture.supplyAsync(() -> {
            try {
                confirmState.getFuture().get();
                return true;
            } catch (CancellationException | InterruptedException | ExecutionException  e) {
                this.cancel(e.getMessage());
                return false;
            }
        }).thenApply((success) -> {
            synchronized (this.stateFuture) {
                try {

                    //used to check if the state has changed and ignore if it did;
                    currentStateFuture.getNow(null);

                    if (!success) {
                        this.cancel("Your teleportation has been cancelled in the EXECUTION stage, please report to dev team");
                        return false;
                    }

                    this.setState(TransactionState.FINISHED);
                    return true;

                } catch (CancellationException e) {
                    //state of transaction has changed, do notihing
                } catch (TransactionFinishedException | TransactionCancelledException e) {
                    this.cancel(e.getMessage());
                }
            }
            return false;
        });
    }


    //sends a cancel packet that is used to force spigot instances to disconnect their consumers
    //used when players change the server to force the old server to disconnect from RabbitMQ
    public void fakeCancel() {
        synchronized (this.stateFuture) {
            if(this.transactionState != TransactionState.CANCELED
                && this.transactionState != TransactionState.FINISHED) {
                this.stateFuture.cancel(false);
                Packet packet = new CancelTeleportPacket("!!!FakeCancel: IF YOU SEE THIS REPORT TO THE DEV TEAM ASAP!!!");

                PCO.sendPacket(packet, this.transactionID);
                try {
                    this.setState(TransactionState.CONTACT);
                }  catch (TransactionFinishedException | TransactionCancelledException e) {
                    //shouldn't ever happen because of the if check
                }
            }
        }
    }

    public void cancel(String reason) {
        synchronized (this.stateFuture) {
            if(this.transactionState != TransactionState.CANCELED
                    && this.transactionState != TransactionState.FINISHED) {
                Packet packet = new CancelTeleportPacket(reason);
                PCO.sendPacket(packet, this.transactionID);
                try {
                    this.setState(TransactionState.CANCELED);
                }  catch (TransactionFinishedException | TransactionCancelledException e) {
                    //shouldn't ever happen because of the if check
                }
            }
        }
    }

    public ConfirmState sendTeleportInfo() {
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

        String correlationID = UUID.randomUUID().toString();

        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .correlationId(correlationID)
                .build();

        packet.setProperties(properties);

        PCO.sendPacket(packet, this.transactionID);

        //adds listening for confirms in response to this request.
        ConfirmState confirmState = new ConfirmState(correlationID);
        this.confirms.put(correlationID, confirmState);

        return confirmState;
    }

    public ConfirmState sendExecute() {
        String correlationID = UUID.randomUUID().toString();

        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .correlationId(correlationID)
                .build();

        Packet packet = new ExecuteTeleportPacket();
        packet.setProperties(properties);

        PCO.sendPacket(packet, this.transactionID);

        //adds listening for confirms in response to this request.
        ConfirmState confirmState = new ConfirmState(correlationID);
        this.confirms.put(correlationID, confirmState);

        return confirmState;
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
