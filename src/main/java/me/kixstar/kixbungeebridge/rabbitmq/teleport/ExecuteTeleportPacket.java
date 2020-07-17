package me.kixstar.kixbungeebridge.rabbitmq.teleport;

import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class ExecuteTeleportPacket extends Packet {

    public ExecuteTeleportPacket() {}

    @Override
    public byte[] serialize() {
        return new byte[]{(byte) 1};
    }

    @Override
    public void deserialize(byte[] raw) {
    }
}
