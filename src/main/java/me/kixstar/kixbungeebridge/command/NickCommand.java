package me.kixstar.kixbungeebridge.command;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.feature.NicknameService;
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
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;
        boolean formatting = false;
        if(player.hasPermission("kix.utilities.nick.formatting")) formatting = true;
        if(args.length == 1) {
            if(!this.checkPermissions(player, player)) return;
            if(args[0].equalsIgnoreCase("cl")) {
                this.clearNickname(player);
            } else {
                String nick = args[0];
                this.setNickname(player, nick, formatting);
            }
        }
        if(args.length == 2) {
            String playerName = args[0];
            ProxiedPlayer target = proxy.getPlayer(playerName);
            if(target == null) return;
            if(!this.checkPermissions(player, target)) return;
            if(args[1].equalsIgnoreCase("cl")) {
                this.clearNickname(target);
            } else {
                this.setNickname(target, args[1], formatting);
            }
        }
    }

    public boolean setNickname(ProxiedPlayer target, String nick, boolean formatting) {

        if(!this.validNickname(nick)) return false;
        if(!formatting) nick = this.stripColorCodes(nick);

        NicknameService service = NicknameService.get();

        service.setNickname(target, nick, formatting);
        return true;
    }

    public void clearNickname(ProxiedPlayer target) {
        //prepare packet
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("NicknameClearEvent");
        out.writeUTF(target.getUniqueId().toString());

        //send packet
        target.getServer().sendData("kixutilities:nickname", out.toByteArray());

    }

    public boolean checkPermissions(ProxiedPlayer player, ProxiedPlayer target) {
        if(player.equals(target)) {
            if(!player.hasPermission("kix.utilities.nick.self")) return false;
        } else {
            if(!player.hasPermission("kix.utilities.nick.target")) return false;
        }
        return true;
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
