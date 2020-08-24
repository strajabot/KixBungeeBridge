package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;
import me.kixstar.kixbungeebridge.Config;

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

            //logs all outgoing RabbitMQ packets if the plugin isn't running in production
            if(!Config.isProd()) this.log(packet, route);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Packet packet, String route) {
        String log = new StringBuilder()
                .append(RabbitMQ.getOrigin())
                .append("->")
                .append(this.exchange.equals("") ? "default" : this.exchange)
                .append(".")
                .append(route)
                .append(":")
                .append(packet.toString())
                .append("correlationID:" + packet.getProperties().getCorrelationId()+ "\n")
                .toString();

        System.out.println(log);
    }

    public CustomProtocol getProto() {
        return this.proto;
    }

    public void setProto(CustomProtocol proto) {
        this.proto = proto;
    }
}
