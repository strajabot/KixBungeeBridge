package me.kixstar.kixbungeebridge.command;

import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.feature.teleport.TeleportTransaction;
import me.kixstar.kixbungeebridge.rabbitmq.Location;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

//todo: fix command failing silently
public class TpCommand extends Command {
    public TpCommand() {
        super("srvtp");
    }

    //"cl" is too short to be a valid nickname so we can use it to trigger nickname clearing
    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxyServer proxy = KixBungeeBridge.getInstance().getProxy();
        if(!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        List<ProxiedPlayer> players = new ArrayList(proxy.getPlayers());
        players.remove(player);
        ProxiedPlayer target = players.get(0);

        TeleportTransaction.playerToPlayer(player, target, 10).init();

    }

}
