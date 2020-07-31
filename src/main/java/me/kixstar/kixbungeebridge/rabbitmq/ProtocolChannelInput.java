package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import me.kixstar.kixbungeebridge.Config;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ProtocolChannelInput {

    private CustomProtocol proto;

    private Channel channel;

    private String exchange;

    private String queue;

    private String route;

    private String consumerTag;

    private AtomicBoolean bound = new AtomicBoolean();

    public ProtocolChannelInput() {
        this(new CustomProtocol());
    }

    public ProtocolChannelInput(CustomProtocol proto) {
        this.proto = proto;
    }

    public void bind(Channel channel, String exchange, String type, String routingKey) {
        this.bound.set(true);
        this.channel = channel;
        this.exchange = exchange;
        this.route = routingKey;
        try {
            if(this.exchange.equals("")) {
                this.queue = this.channel.queueDeclare(
                        routingKey,
                        false,
                        true,
                        true,
                        null
                ).getQueue();
            } else {
                this.queue = this.channel.queueDeclare().getQueue();
                this.channel.exchangeDeclare(this.exchange, type);
                this.channel.queueBind(this.queue, this.exchange, this.route);
            }
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if(!this.bound.get()) return;
                Packet packet = this.proto.deserialize(delivery.getBody());
                packet.setProperties(delivery.getProperties());

                //logs all outgoing RabbitMQ packets if the plugin isn't running in production
                if(!Config.isProd()) this.log(packet);

                this.onPacket(packet);
            };
            this.consumerTag = this.channel.basicConsume(this.queue, true, deliverCallback, (consumertag) -> {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unbind() {
        try {
            this.bound.set(false);
            if(!this.exchange.equals("")) this.channel.queueUnbind(this.queue, this.exchange, this.route);
            this.channel.basicCancel(this.consumerTag);
            this.channel.queueDelete(this.queue);
            this.consumerTag = null;
            this.channel = null;
            this.route = null;
            this.exchange = null;
            this.queue = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(Packet packet) {
        String log = null;
        try {
            //ignore packets sent from this server to avoid confusion
            if(RabbitMQ.isFromThisServer(packet)) return;
            log = new StringBuilder()
                    .append(RabbitMQ.getOrigin(packet))
                    .append("->")
                    .append(this.exchange.equals("") ? "default" : this.exchange)
                    .append(".")
                    .append(this.exchange.equals("") ? this.queue : this.route)
                    .append(":")
                    .append(packet.toString())
                    .append("correlationID:" + packet.getProperties().getCorrelationId()+ "\n")
                    .toString();
        } catch (UnknownPacketOriginException e) {
            e.printStackTrace();
        }

        System.out.println(log);
    }

    public abstract void onPacket(Packet packet);

}
