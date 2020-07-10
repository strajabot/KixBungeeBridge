package me.kixstar.kixbungeebridge.rabbitmq;

public interface Packet {

    byte[] serialize();

    void deserialize(byte[] raw);
}
