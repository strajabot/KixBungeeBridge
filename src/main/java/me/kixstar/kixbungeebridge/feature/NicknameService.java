package me.kixstar.kixbungeebridge.feature;

import me.kixstar.kixbungeebridge.rabbitmq.Packet;
import me.kixstar.kixbungeebridge.rabbitmq.ProtocolChannelInput;
import me.kixstar.kixbungeebridge.rabbitmq.ProtocolChannelOutput;
import me.kixstar.kixbungeebridge.rabbitmq.RabbitMQ;
import me.kixstar.kixbungeebridge.rabbitmq.nickname.NicknameChangePacket;
import me.kixstar.kixbungeebridge.rabbitmq.nickname.NicknameClearPacket;
import me.kixstar.kixbungeebridge.rabbitmq.nickname.NicknameProtocol;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NicknameService {


    private ProtocolChannelOutput PCO = new ProtocolChannelOutput(new NicknameProtocol());


    private static NicknameService instance = new NicknameService();

    public static NicknameService get() {
        return instance;
    }

    NicknameService() {}

    public boolean setNickname(ProxiedPlayer player, String nickname, boolean formatting) {
        if(!formatting) nickname = this.stripColorCodes(nickname);
        if(!this.validNickname(nickname)) return false;

        String playerUUID = player.getUniqueId().toString();

        NicknameChangePacket packet = new NicknameChangePacket(nickname);

        this.PCO.sendPacket(packet, playerUUID );
        return true;
    }

    public void clearNickname(ProxiedPlayer player) {

        String playerUUID = player.getUniqueId().toString();

        NicknameClearPacket packet  = new NicknameClearPacket();

        this.PCO.sendPacket(packet, playerUUID);
    }

    public boolean validNickname(String nickname) {
        /*   Minecraft username info: https://help.mojang.com/customer/en/portal/articles/928638-minecraft-usernames
         *
         *  -Usernames can consist of the whole English Alphabet
         *  -Usernames can consist of all digits
         *  -Out of all special characters usernames can only use the underscore
         *  -Usernames mustn't be shorter than 3 characters
         *  -Usernames mustn't be longer than 16 characters
         */
        String usernameRegex = "^[a-zA-Z0-9_]{3,16}$";
        return nickname.matches(usernameRegex);
    }

    public String stripColorCodes(String nick) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while( i < nick.length()) {
            char c = nick.charAt(i);
            if(c == '&') {
                //skip the next char too since color codes are formatted like "&a"
                i++;
            } else {
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }

    public static void register() {
        instance.PCO.bind(RabbitMQ.getChannel(), "nickname", "topic");
    }

    public static void unregister() {
        instance.PCO.unbind();
    }
}
