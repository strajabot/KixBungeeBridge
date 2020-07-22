package me.kixstar.kixbungeebridge.rabbitmq.servercommand;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class CommandStatusPacket extends Packet {

    private boolean status;

    public CommandStatusPacket() {}

    public CommandStatusPacket(boolean status) {
        this.status = status;
    }

    @Override
    public byte[] serialize() {
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        frame.writeBoolean(this.status);
        return frame.toByteArray();
    }

    @Override
    public void deserialize(byte[] raw) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(raw);

        this.status = frame.readBoolean();
    }

    public boolean getStatus() {
        return this.status;
    }
}
