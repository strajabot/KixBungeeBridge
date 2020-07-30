package me.kixstar.kixbungeebridge.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import me.kixstar.kixbungeebridge.Config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQ {

    private static Connection conn;

    private static Channel channel;

    private static String originHeader;

    public static boolean setOrigin(Packet packet) {
        if(originHeader == null) return false;
        Map<String, Object> headers = packet.getProperties().getHeaders();
        headers.put("origin", originHeader);
        return true;
    }

    public static String isFromThisServer(Packet packet) throws UnknownPacketOriginException {
        Object origin = packet.getProperties().getHeaders().get("origin");
        if(origin == null) throw new UnknownPacketOriginException();
        if(!(origin instanceof String)) throw new UnknownPacketOriginException();
        return (String) origin;
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
