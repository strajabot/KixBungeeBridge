package me.kixstar.kixbungeebridge.command.home;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.feature.homes.HomeNotExistException;
import me.kixstar.kixbungeebridge.feature.homes.HomeService;
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
public class DelHomeCommand extends Command {

    public DelHomeCommand() {
        super("delhome");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 2) {
            if (!(sender instanceof ProxiedPlayer)) {
                log(Level.WARNING, "Only \"/delhome <targetName> <homeName> \" can be run from console");
                return;
            }
        }
        //todo: improve error handling
        if(args.length == 0) {
            /* "/delhome" */
            selfDeleteHome((ProxiedPlayer) sender, "default");
        } else if(args.length == 1) {
            /* "/delhome <homeName>" */
            selfDeleteHome((ProxiedPlayer) sender, args[0]);
        } else if(args.length == 2) {
            //note: can be run by the console
            /* "/delhome <targetName> <homeName>" */
            targetDeleteHome(sender, args[0], args[1]);
        }
    }

    private void selfDeleteHome(@NotNull ProxiedPlayer player, @NotNull String homeName) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");

        CompletableFuture<Void> deleteHomeFuture = HomeService.deleteHome(player.getUniqueId().toString(), homeName);

        deleteHomeFuture.thenAccept((ignore) -> {
            //Home successfully deleted
            TextComponent homeDeleted = new TextComponent();
            homeDeleted.setText(String.format("Home \"%s\" has been deleted successfully", homeName));
            homeDeleted.setColor(ChatColor.GOLD);

            player.sendMessage(homeDeleted);
        });

       /* Catch exceptions that can be expected like HomeNotExistException
        * and reformat the messages to be more friendly,
        * also if an unknown exception is thrown log to console and send
        * a message to the player that the action couldn't be performed
        */
        deleteHomeFuture.whenComplete((result, ex) -> {
            if(ex == null) return;
            if(ex instanceof HomeNotExistException) {
                this.selfHomeNotExist(player, homeName);
            } else {
                this.unexpectedError(player, player.getName(), homeName, ex);
            }
        });

    }

    private void targetDeleteHome(
            @NotNull CommandSender sender,
            @NotNull String targetName,
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");

        ProxiedPlayer target = KixBungeeBridge.getInstance().getProxy().getPlayer(targetName);
        if(target == null) {
            this.targetOffline(sender, targetName);
            return;
        }

        CompletableFuture<Void> deleteHomeFuture = HomeService.deleteHome(target.getUniqueId().toString(), homeName);

        deleteHomeFuture.thenAccept((ignore) -> {
            //Home successfully deleted
            TextComponent homeDeleted = new TextComponent();
            homeDeleted.setText(String.format("%s's home \"%s\" has been deleted successfully", targetName, homeName));
            homeDeleted.setColor(ChatColor.GOLD);

            if(sender instanceof ProxiedPlayer) {
                sender.sendMessage(homeDeleted);
            } else {
                this.log(Level.INFO, homeDeleted.getText());
            }
        });

        /* Catch exceptions that can be expected like HomeNotExistException
         * and reformat the messages to be more friendly,
         * also if an unknown exception is thrown log to console and send
         * a message to the player that the action couldn't be performed
         */
        deleteHomeFuture.whenComplete((result, ex) -> {
            if(ex == null) return;
            if(ex instanceof HomeNotExistException) {
                this.targetHomeNotExist(sender, targetName, homeName);
            } else {
                this.unexpectedError(sender, targetName, homeName, ex);
            }
        });

    }

    private void unexpectedError(
            @NotNull CommandSender sender,
            @NotNull String targetName,
            @NotNull String homeName,
            @NotNull Throwable throwable
    ) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        Preconditions.checkNotNull(throwable, "Argument \"throwable\" can't be null");

        String formattedStackTrace = this.getFormattedTrace(throwable);

        TextComponent stackTrace = new TextComponent();
        stackTrace.setText(formattedStackTrace);
        stackTrace.setColor(ChatColor.RED);
        TextComponent errMessage = new TextComponent();
        errMessage.setText("Couldn't delete " + targetName + ":" + homeName + " because of an internal error:\n");
        errMessage.setColor(ChatColor.RED);
        errMessage.addExtra(stackTrace);
        errMessage.addExtra("\nPlease report to staff as soon as possible");

        this.log(Level.SEVERE, errMessage.getText());

        if(sender instanceof ProxiedPlayer) {
            sender.sendMessage(errMessage);
        }
    }

    private void selfHomeNotExist(@NotNull ProxiedPlayer player, @NotNull String homeName) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        TextComponent homeNotExist = new TextComponent();
        homeNotExist.setText(String.format("Couldn't delete home \"%s\" since you don't own a home with that name", homeName));
        homeNotExist.setColor(ChatColor.RED);
        player.sendMessage(homeNotExist);
    }

    public void targetHomeNotExist(
            @NotNull CommandSender sender,
            @NotNull String targetName,
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        TextComponent homeNotExist = new TextComponent();
        homeNotExist.setText(String.format("Couldn't delete %s's home \"%s\" since he doesn't have a home with that name"));
        homeNotExist.setColor(ChatColor.RED);
        // "/delhome <targetName> <homeName>" can be sent by both a player and the console
        if(sender instanceof ProxiedPlayer) {
            sender.sendMessage(homeNotExist);
        } else {
            //todo: check if TextComponent::getText() returns raw text or text with color codes.
            log(Level.INFO, homeNotExist.getText());
        }

    }

    private void targetOffline(@NotNull CommandSender sender, @NotNull String targetName) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        TextComponent targetOffline = new TextComponent("The player " + targetName + " isn't online, so you can't teleport to their homes");
        targetOffline.setColor(ChatColor.RED);

        if(sender instanceof ProxiedPlayer) {
            sender.sendMessage(targetOffline);
        } else {
            //todo: check if TextComponent::getText() returns raw text or text with color codes.
            log(Level.INFO, targetOffline.getText());
        }
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
