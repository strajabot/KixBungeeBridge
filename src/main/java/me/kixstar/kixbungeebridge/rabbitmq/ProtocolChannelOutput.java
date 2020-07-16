package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public class ProtocolChannelOutput {

    private CustomProtocol proto;

    private Channel channel;

    private String exchange;

    public ProtocolChannelOutput() {
        this(new CustomProtocol());
    }

    public ProtocolChannelOutput(CustomProtocol proto) {
        this.proto = proto;
    }

    public void bind(Channel channel, String exchange, String type) {
        this.channel = channel;
        this.exchange = exchange;
        try {
            this.channel.exchangeDeclare(this.exchange, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unbind() {
        try {
            this.channel.exchangeDelete(this.exchange, false);
            this.channel = null;
            this.exchange = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(Packet packet, String route) {
        try {
            channel.basicPublish(exchange, route, packet.getProperties(), proto.serailize(packet));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CustomProtocol getProto() {
        return this.proto;
    }

    public void setProto(CustomProtocol proto) {
        this.proto = proto;
    }
}
