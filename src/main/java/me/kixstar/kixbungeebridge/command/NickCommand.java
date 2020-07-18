package me.kixstar.kixbungeebridge.command;

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

        NicknameService service = NicknameService.get();

        service.setNickname(target, nick, formatting);
        return true;
    }

    public void clearNickname(ProxiedPlayer target) {

        NicknameService service =  NicknameService.get();

        service.clearNickname(target);

    }

    public boolean checkPermissions(ProxiedPlayer player, ProxiedPlayer target) {
        if(player.equals(target)) {
            if(!player.hasPermission("kix.utilities.nick.self")) return false;
        } else {
            if(!player.hasPermission("kix.utilities.nick.target")) return false;
        }
        return true;
    }

}
