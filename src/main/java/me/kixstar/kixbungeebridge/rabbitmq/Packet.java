package me.kixstar.kixbungeebridge.rabbitmq;

public interface Packet {

    byte[] serialize();

    Packet deserialize(byte[] raw);
}
