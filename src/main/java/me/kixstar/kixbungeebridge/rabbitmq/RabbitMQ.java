package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.*;
import me.kixstar.kixbungeebridge.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {

    private static Connection conn;

    private static Channel channel;

    private static String originHeader;

    public static String getOrigin() {
        return originHeader;
    }

    public static String getOrigin(Packet packet) throws UnknownPacketOriginException {
        Object origin = packet.getProperties().getHeaders().get("origin");
        if(origin == null) throw new UnknownPacketOriginException();
        try {
            return new String(((LongString) origin).getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnknownPacketOriginException();
        }
    }

    public static boolean setOrigin(Packet packet) {
        if(originHeader == null) return false;
        AMQP.BasicProperties oldProperties = packet.getProperties();
        Map<String, Object> oldHeaders = oldProperties.getHeaders();
        Map<String, Object> newHeaders = oldHeaders == null? new HashMap() : new HashMap<>(oldHeaders);
        newHeaders.put("origin", originHeader);
        AMQP.BasicProperties newProperties = oldProperties.builder().headers(newHeaders).build();
        packet.setProperties(newProperties);
        return true;
    }

    public static boolean isFromThisServer(Packet packet) throws UnknownPacketOriginException {
        return getOrigin(packet).equals(originHeader);
    }

    public static void bind(String serverHandle) {
        originHeader = serverHandle;
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(Config.getRabbitMQ());
            conn = factory.newConnection();
            channel = conn.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
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
