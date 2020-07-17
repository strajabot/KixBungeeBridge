package me.kixstar.kixbungeebridge.rabbitmq.teleport;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class ConfirmReadyTeleportPacket extends Packet {

    private boolean playerSide;
    private boolean targetSide;

    public ConfirmReadyTeleportPacket() {}

    public ConfirmReadyTeleportPacket(
            boolean playerSide,
            boolean targetSide
    ) {
        this.playerSide = playerSide;
        this.targetSide = targetSide;
    }

    @Override
    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();
        frame.writeBoolean(this.playerSide);
        frame.writeBoolean(this.targetSide);

        return frame.toByteArray();
    }

    @Override
    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);
        this.playerSide  = frame.readBoolean();
        this.targetSide  = frame.readBoolean();
    }

    public boolean isPlayerSide() {
        return playerSide;
    }

    public boolean isTargetSide() {
        return targetSide;
    }
}
