package me.kixstar.kixbungeebridge.rabbitmq.nickname;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class NicknameClearPacket extends Packet {

    //no args constructor is required for deserialization of packets
    public NicknameClearPacket() {}

    @Override
    public byte[] serialize() { return new byte[]{(byte) 1}; }

    @Override
    public void deserialize(byte[] raw) {}

}
