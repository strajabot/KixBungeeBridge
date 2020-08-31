package me.kixstar.kixbungeebridge.command.home;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.database.entities.Location;
import me.kixstar.kixbungeebridge.database.abstraction.player.KixPlayer;
import me.kixstar.kixbungeebridge.feature.teleport.TeleportTransaction;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Permissions:
 * //setting homes
 * - kix.utilities.home.self.set - lets player set his homes
 * - kix.utilities.home.target.set - lets player set homes for other players
 * <p>
 * //teleporting to homes
 * - kix.utilities.home.self.tp - lets player teleport to his homes
 * - kix.utilities.home.target.tp - lets player teleport to homes of other players
 * <p>
 * //listing homes
 * - kix.utilities.home.self.list - lets player list his homes
 * - kix.utilities.home.target.list - lets player list homes of other players
 * <p>
 * //deleting homes
 * - kix.utilities.home.self.delete - lets player delete his homes
 * - kix.utilities.home.target.delete - lets player delete homes of other players
 */
public class HomeCommand extends Command {

    public HomeCommand() {
        super("home");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            log(Level.WARNING, "\"/sethome \" can't be run from console");
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        //todo: improve error handling
        if (args.length == 0) {
            selfTeleport(player, "default");
        } else if (args.length == 1) {
            selfTeleport(player, args[0]);
        } else if (args.length == 2) {
            targetTeleport(player, args[0], args[1]);
        }


    }

    public void selfTeleport(
            @NotNull ProxiedPlayer player,
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(player, "Argument \"playerName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        if(!player.hasPermission("kix.utilities.home.target.tp")) {
            this.noPermission(player);
            return;
        }

        CompletableFuture<Location> getHomeFuture = KixPlayer.get(player.getUniqueId().toString()).getHomeLocation(homeName);

        getHomeFuture.whenComplete((location, throwable) -> {
            if(throwable != null) {
                //todo: probably make another method for when the target player is the player who called the command
                this.unexpectedError(player, player.getName(), homeName, throwable);
                return;
            }
            if (location == null) {
                this.selfTeleport(player, homeName);
                return;
            }
            this.teleport(player, location);

        });
    }

    private void targetTeleport(
            @NotNull ProxiedPlayer player,
            @NotNull String targetName,
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(player, "Argument \"playerName\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        if(!player.hasPermission("kix.utilities.home.target.tp")) {
            this.noPermission(player);
            return;
        }
        ProxiedPlayer target = KixBungeeBridge.getInstance().getProxy().getPlayer(targetName);
        if(target == null) {
            this.targetOffline(player, targetName);
            return;
        }

        CompletableFuture<Location> getHomeFuture = KixPlayer.get(target.getUniqueId().toString()).getHomeLocation(homeName);

        getHomeFuture.whenComplete((location, throwable) -> {
            if(throwable != null) {
                this.unexpectedError(player, targetName, homeName, throwable);
                return;
            }
            if (location == null) {
                this.targetHomeNotExist(player, targetName, homeName);
                return;
            }
            this.teleport(player, location);

        });

    }

    public void teleport(ProxiedPlayer player, Location location) {
        //todo: probably shouldn't be hardcoded
        int delay = 5;
        if(player.hasPermission("kix.utilities.teleport.override-delay")) delay = 0;
        TeleportTransaction.playerToLocation(player, location, delay).init();

    }

    private void unexpectedError(
            @NotNull ProxiedPlayer player,
            @NotNull String targetName,
            @NotNull String homeName,
            @NotNull Throwable throwable
    ) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        Preconditions.checkNotNull(throwable, "Argument \"throwable\" can't be null");

        String formattedStackTrace = this.getFormattedTrace(throwable);

        TextComponent stackTrace = new TextComponent();
        stackTrace.setText(formattedStackTrace);
        stackTrace.setColor(ChatColor.DARK_RED);
        TextComponent errMessage = new TextComponent();
        errMessage.setText("Couldn't teleport you to " + targetName + ":" + homeName + " because of an internal error:\n");
        errMessage.setColor(ChatColor.DARK_RED);
        errMessage.addExtra(stackTrace);
        errMessage.addExtra("\nPlease report to staff as soon as possible");

        this.log(Level.SEVERE, errMessage.toString());

        player.sendMessage(errMessage);
    }

    public void selfHomeNotExist(
            @NotNull ProxiedPlayer player,
            @NotNull String targetName,
            @NotNull String homeName
    ) {
        TextComponent message = new TextComponent("Error while teleporting: You don't own a home \""+ homeName+"\"");
        message.setColor(ChatColor.RED);
        player.sendMessage(message);
    }

    public void targetHomeNotExist(
            @NotNull ProxiedPlayer player,
            @NotNull String targetName,
            @NotNull String homeName
    ) {
        TextComponent message = new TextComponent("Error while teleporting: Player " + targetName  + " doesn't own a home: \""+ homeName+"\"");
        message.setColor(ChatColor.RED);
        player.sendMessage();
    }

    private void targetOffline(@NotNull ProxiedPlayer player, @NotNull String targetName) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        TextComponent targetOffline = new TextComponent("The player " + targetName + " isn't online, so you can't teleport to their homes");
        targetOffline.setColor(ChatColor.RED);
        player.sendMessage(targetOffline);
    }

    private void noPermission(@NotNull ProxiedPlayer player) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        TextComponent noPermission = new TextComponent("You don't have permissions needed to run this command");
        noPermission.setColor(ChatColor.RED);
        player.sendMessage(noPermission);
    }

    public void log(@NotNull Level level, @NotNull String string) {
        Preconditions.checkNotNull(level, "Argument \"level\" can't be null");
        Preconditions.checkNotNull(string, "Argument \"string\" can't be null");
        Plugin plugin = KixBungeeBridge.getInstance();
        plugin.getLogger().log(level, string);
    }

    @NotNull
    private String getFormattedTrace(@NotNull Throwable throwable) {
        Preconditions.checkNotNull(throwable, "Argument \"throwable\" can't be null");
        Writer stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
