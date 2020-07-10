package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {

    private static Connection conn;

    private static Channel channel;

    public static void bind() {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername("user");
        factory.setPassword("password");
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);

        try {
            conn = factory.newConnection();
            channel = conn.createChannel();
            } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void unbind() {
        try {
            conn.close();
            conn = null;
            channel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return conn;
    }

    public static Channel getChannel() {
        return channel;
    }

}
