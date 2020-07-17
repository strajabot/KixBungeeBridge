package me.kixstar.kixbungeebridge.rabbitmq.servercommand;

import me.kixstar.kixbungeebridge.rabbitmq.CustomProtocol;

public class ServerCommandProtocol extends CustomProtocol {

    public ServerCommandProtocol() {
        this.addPacket("SubscribeTeleportPacket", SubscribeTeleportPacket.class);
    }

}


