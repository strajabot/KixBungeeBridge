package me.kixstar.kixbungeebridge.rabbitmq;

import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.lang.reflect.InvocationTargetException;

public class CustomProtocol {
    private HashBiMap<String, Class<? extends Packet>> packets = HashBiMap.create();

    protected void addPacket(String identifier, Class<? extends Packet> packet) {
        try {
            packet.getConstructor();
            this.packets.put(identifier, packet);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    new StringBuilder()
                            .append("Every packet needs to have a no arguments constructor and ")
                            .append(packet.getName())
                            .append(" doesn't \n")
                            .append(packet.getName())
                            .append(" will not be listened for")
                            .toString()
            );
        }
    }

    public Packet deserialize(byte[] in) {
        ByteArrayDataInput frame = ByteStreams.newDataInput(in);

        String identifier = frame.readUTF();
        int packetLength = frame.readInt();
        byte[] raw = new byte[packetLength];
        frame.readFully(raw);

        Class<? extends Packet> pClass = this.packets.get(identifier);

        try {
            Packet packet = pClass.getConstructor().newInstance();
            packet.deserialize(raw);

            return packet;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] serialize(Packet packet) {
        if (!this.packets.containsValue(packet.getClass())) return null;
        ByteArrayDataOutput frame = ByteStreams.newDataOutput();

        byte[] raw  = packet.serialize();

        frame.writeUTF(this.packets.inverse().get(packet.getClass()));
        frame.writeInt(raw.length);
        frame.write(raw);

        return frame.toByteArray();
    }

}
