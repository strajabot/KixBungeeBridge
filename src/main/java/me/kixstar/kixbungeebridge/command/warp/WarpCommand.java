package me.kixstar.kixbungeebridge.command.warp;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class WarpCommand extends Command {

    public WarpCommand() {
        super("warp");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            if (!(sender instanceof ProxiedPlayer)) {
                log(Level.WARNING, "Only \"/warp <warpName> <playerName> \" can be run from console");
                return;
            }
        }

        if (args.length == 0) {
            /* "/warp" */
            //list all warps that this player can access
            listWarps((ProxiedPlayer) sender);
        } else if (args.length == 1) {
            /* "/warp <warpName>" */
            selfWarp((ProxiedPlayer) sender, args[0]);
        } else if (args.length == 2) {
            //note: can be run by the console
            /* "/warp <warpName> <playerName>" */
            targetWarp(sender, args[0], args[1]);
        }

    }

    private void listWarps (@NotNull ProxiedPlayer player){
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");

        //todo: implement
    }

    private void selfWarp(@NotNull ProxiedPlayer player, @NotNull String warpName) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(warpName, "Argument \"warpName\" can't be null");

        //todo: implement
    }

    private void targetWarp(
            @NotNull CommandSender sender,
            @NotNull String playerName,
            @NotNull String warpName
    ) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(playerName, "Argument \"playerName\" can't be null");
        Preconditions.checkNotNull(warpName, "Argument \"warpName\" can't be null");

        //todo: implement
    }

    private void log(@NotNull Level level, @NotNull String string) {
        Preconditions.checkNotNull(level, "Argument \"level\" can't be null");
        Preconditions.checkNotNull(string, "Argument \"string\" can't be null");
        Plugin plugin = KixBungeeBridge.getInstance();
        plugin.getLogger().log(level, string);
    }
}
