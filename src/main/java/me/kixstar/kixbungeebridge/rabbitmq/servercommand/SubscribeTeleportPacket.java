package me.kixstar.kixbungeebridge.rabbitmq.servercommand;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.rabbitmq.client.AMQP;
import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class SubscribeTeleportPacket extends Packet {

    private String transactionID;

    public SubscribeTeleportPacket() {
    }

    public SubscribeTeleportPacket(String transactionID) {
        this.transactionID = transactionID;
    }

    @Override
    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();
        frame.writeUTF(this.transactionID);

        return frame.toByteArray();
    }

    @Override
    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);
        this.transactionID = frame.readUTF();
    }

    public String getTransactionID() {
        return transactionID;
    }

}
