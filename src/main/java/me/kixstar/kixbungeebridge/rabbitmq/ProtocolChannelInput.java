package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

public abstract class ProtocolChannelInput {

    private CustomProtocol proto;

    private Channel channel;

    private String exchange;

    private String queue;

    private String route;

    public ProtocolChannelInput() {
        this(new CustomProtocol());
    }

    public ProtocolChannelInput(CustomProtocol proto) {
        this.proto = proto;
    }

    public void bind(Channel channel, String exchange, String type, String routingKey) {
        this.channel = channel;
        this.exchange = exchange;
        this.route = routingKey;
        try {
            this.channel.exchangeDeclare(this.exchange, type);
            this.queue = this.channel.queueDeclare().getQueue();
            this.channel.queueBind(this.queue, this.exchange, this.route);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                Packet packet = this.proto.deserialize(delivery.getBody());
                this.onPacket(packet);
            };
            this.channel.basicConsume(queue, true, deliverCallback, (consumertag) -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unbind() {
        try {
            this.channel.queueUnbind(this.queue, this.exchange, this.route);
            this.channel.queueDelete(this.queue);
            this.channel = null;
            this.route = null;
            this.exchange = null;
            this.queue = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void onPacket(Packet packet);

}
