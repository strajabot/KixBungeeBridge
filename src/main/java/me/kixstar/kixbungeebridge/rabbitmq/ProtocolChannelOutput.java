package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public class ProtocolChannelOutput {

    private CustomProtocol proto;

    private Channel channel;

    private String exchange;

    public ProtocolChannelOutput(CustomProtocol proto) {
        this.proto = proto;
    }

    public void bind(Channel channel, String exchange, String type) {
        this.channel = channel;
        this.exchange = exchange;
        try {
            if(!this.exchange.equals("")) this.channel.exchangeDeclare(this.exchange, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unbind() {
        try {
            if(!this.exchange.equals("")) this.channel.exchangeDelete(this.exchange, true);
            this.channel = null;
            this.exchange = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(Packet packet, String route) {
        try {
            if(!RabbitMQ.setOrigin(packet))
                throw new RuntimeException("Couldn't set packet's \"origin\" header, packet will be ignored");
            channel.basicPublish(exchange, route, packet.getProperties(), proto.serialize(packet));
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
