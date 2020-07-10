package me.kixstar.kixbungeebridge.rabbitmq.nickname;

import me.kixstar.kixbungeebridge.rabbitmq.CustomProtocol;

public class NicknameProtocol extends CustomProtocol {

    public NicknameProtocol() {
        super.addPacket("NicknameChangePacket", NicknameChangePacket.class);
        super.addPacket("NicknameClearPacket", NicknameClearPacket.class);
    }

}
