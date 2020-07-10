package me.kixstar.kixbungeebridge.rabbitmq.nickname;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.rabbitmq.Packet;

public class NicknameChangePacket implements Packet {

    private String playerUUID;
    private String nickname;

    //no args constructor is required for deserialization of packets
    public NicknameChangePacket() {}

    public NicknameChangePacket(String playerUUID, String nickname) {
        this.playerUUID = playerUUID;
        this.nickname = nickname;
    }

    @Override
    public byte[] serialize() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(this.playerUUID);
        out.writeUTF(this.nickname);

        return out.toByteArray();
    }

    @Override
    public void deserialize(byte[] raw) {
        ByteArrayDataInput in = ByteStreams.newDataInput(raw);
        this.playerUUID = in.readUTF();
        this.nickname = in.readUTF();
    }

}
