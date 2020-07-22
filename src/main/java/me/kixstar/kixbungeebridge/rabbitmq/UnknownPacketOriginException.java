package me.kixstar.kixbungeebridge.rabbitmq;

public class UnknownPacketOriginException extends Exception {

    public UnknownPacketOriginException() {
        super("The server which this packet was sent from cannot be determined, so it will be ignored");
    }
}
