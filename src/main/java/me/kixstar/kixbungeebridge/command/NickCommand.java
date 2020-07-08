package me.kixstar.kixbungeebridge.command;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

//todo: fix command failing silently
public class NickCommand extends Command {
    public NickCommand() {
        super("nick");
    }

    //"cl" is too short to be a valid nickname so we can use it to trigger nickname clearing
    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxyServer proxy = KixBungeeBridge.getInstance().getProxy();
        if(args.length == 1) {
            if(!(sender instanceof ProxiedPlayer)) return;
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(args[0].equalsIgnoreCase("cl")) {
                this.clearNickname(player);
                return;
            }
            String nickname = args[0];
            this.setNickname(player, nickname);
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("cl")) {
                String playerName = args[1];
                ProxiedPlayer player = proxy.getPlayer(playerName);
                if(player == null) return;
                this.clearNickname(player);
                return;
            }
            String playerName = args[0];
            String nick = args[1];
            ProxiedPlayer player = proxy.getPlayer(playerName);
            if(player == null) return;
            this.setNickname(player, nick);
        }
    }

    public boolean setNickname(ProxiedPlayer player, String nick) {
        if(!this.validNickname(nick)) return false;
        if(!player.hasPermission("kix.utilities.nick.formatting"))
            nick = this.stripColorCodes(nick);

        //prepare packet
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("NicknameChangeEvent");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(nick);

        //send packet
        player.getServer().sendData("kixutilities:nickname", out.toByteArray());

        return true;
    }

    public void clearNickname(ProxiedPlayer player) {
        //prepare packet
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("NicknameClearEvent");
        out.writeUTF(player.getUniqueId().toString());

        //send packet
        player.getServer().sendData("kixutilities:nickname", out.toByteArray());

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



    public boolean validNickname(String nickname) {
        /*   Minecraft username info: https://help.mojang.com/customer/en/portal/articles/928638-minecraft-usernames
         *
         *  -Usernames can consist of the whole English Alphabet
         *  -Usernames can consist of all digits
         *  -Out of all special characters usernames can only use the underscore
         *  -Usernames mustn't be shorter than 3 characters
         *  -Usernames mustn't be longer than 16 characters
         */
        String usernameRegex = "/^[a-zA-Z0-9_]{3,16}$/";
        return nickname.matches(usernameRegex);
    }

}
