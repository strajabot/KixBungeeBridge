package me.kixstar.kixbungeebridge.feature.teleport;

import me.kixstar.kixbungeebridge.rabbitmq.teleport.ConfirmReadyTeleportPacket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfirmState {


    private String bothServersClaim =
            "Teleport cancelled because multiple servers claimed to have the same player connected to them";

    private String confirmTimeout =
            "Servers didn't respond to teleport request in 10 seconds, teleport cancelled";


    private final String correlationID;

    private AtomicBoolean playerServerConfirm = new AtomicBoolean(false);
    private AtomicBoolean targetServerConfirm = new AtomicBoolean(false);

    private final CompletableFuture<Void> future = new CompletableFuture<>();

    public ConfirmState(String correlationID) {
        this.correlationID = correlationID;

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.future.completeExceptionally(new Exception(this.confirmTimeout));
            }
        });
    }

    public void handleConfirmReadyTeleport(ConfirmReadyTeleportPacket packet) {
        if(packet.isPlayerSide()) {
            //if both servers say they own the player or the target cancel the teleport.
            if(!this.playerServerConfirm.compareAndSet(false, true))
                this.future.completeExceptionally(new Exception(this.bothServersClaim));
        }
        if(packet.isTargetSide()) {
            //if both servers say they own the player or the target cancel the teleport.
            if(!this.targetServerConfirm.compareAndSet(false, true))
                this.future.completeExceptionally(new Exception(this.bothServersClaim));
        }
        if(this.playerServerConfirm.get() && this.targetServerConfirm.get()) future.complete(null);
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public AtomicBoolean getPlayerServerConfirm() {
        return playerServerConfirm;
    }

    public AtomicBoolean getTargetServerConfirm() {
        return targetServerConfirm;
    }

    public CompletableFuture<Void> getFuture() {
        return this.future;
    }
}
