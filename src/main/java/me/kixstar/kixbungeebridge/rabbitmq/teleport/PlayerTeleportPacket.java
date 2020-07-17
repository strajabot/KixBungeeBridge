package me.kixstar.kixbungeebridge.rabbitmq.teleport;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class PlayerTeleportPacket extends Packet {

    private String playerUUID;
    private String targetUUID;

    public PlayerTeleportPacket() {

    }

    public PlayerTeleportPacket(
            String playerUUID,
            String targetUUID
    ) {
        this.playerUUID = playerUUID;
        this.targetUUID = targetUUID;
    }

    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeUTF(this.playerUUID);
        frame.writeUTF(this.targetUUID);

        return frame.toByteArray();

    }

    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);

        this.playerUUID = frame.readUTF();
        this.targetUUID = frame.readUTF();

    }


    public String getPlayerUUID() {
        return playerUUID;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

}
