package me.kixstar.kixbungeebridge.command.home;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.database.abstraction.HomeNotExistException;
import me.kixstar.kixbungeebridge.database.abstraction.player.KixPlayer;
import me.kixstar.kixbungeebridge.database.entities.HomeData;
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
import java.util.List;
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
public class HomesCommand extends Command {

    public HomesCommand() {
        super("homes");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1) {
            if (!(sender instanceof ProxiedPlayer)) {
                log(Level.WARNING, "Only \"/homes <targetName>\" can be run from console");
                return;
            }
        }
        //todo: improve error handling
        if(args.length == 0) {
            /* "/homes" */
            selfHomes((ProxiedPlayer) sender);
        } else if(args.length == 1) {
            /* "/homes <targetName>" */
            targetHomes(sender, args[0]);
        } else {
            //todo: implement showing help menu
        }
    }

    private void selfHomes(@NotNull ProxiedPlayer player) {
        Preconditions.checkNotNull(player, "Argument \"player\" can't be null");

        KixPlayer kixPlayer = KixPlayer.get(player.getUniqueId().toString());
        CompletableFuture<List<HomeData>> getHomesFuture = kixPlayer.getHomes();

        getHomesFuture.whenComplete((result, ex) -> {
            if(ex == null) {
                //todo: implement logging homes to player.
            } else {
                this.unexpectedError(player, player.getName(), ex);
            }
        });

    }

    private void targetHomes(
            @NotNull CommandSender sender,
            @NotNull String targetName
    ) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");

        ProxiedPlayer target = KixBungeeBridge.getInstance().getProxy().getPlayer(targetName);
        if(target == null) {
            this.targetOffline(sender, targetName);
            return;
        }

        KixPlayer kixPlayer = KixPlayer.get(target.getUniqueId().toString());
        CompletableFuture<List<HomeData>> getHomesFuture = kixPlayer.getHomes();

        getHomesFuture.whenComplete((result, ex) -> {
            if(ex == null) {
                //todo: implement logging homes to player.
            } else {
                this.unexpectedError(sender, targetName, ex);
            }
        });

    }

    private void unexpectedError(
            @NotNull CommandSender sender,
            @NotNull String targetName,
            @NotNull Throwable throwable
    ) {
        Preconditions.checkNotNull(sender, "Argument \"sender\" can't be null");
        Preconditions.checkNotNull(targetName, "Argument \"targetName\" can't be null");
        Preconditions.checkNotNull(throwable, "Argument \"throwable\" can't be null");

        String formattedStackTrace = this.getFormattedTrace(throwable);

        TextComponent stackTrace = new TextComponent();
        stackTrace.setText(formattedStackTrace);
        stackTrace.setColor(ChatColor.RED);
        TextComponent errMessage = new TextComponent();
        errMessage.setText("Couldn't list " + targetName + "'s homes because of an internal error:\n");
        errMessage.setColor(ChatColor.RED);
        errMessage.addExtra(stackTrace);
        errMessage.addExtra("\nPlease report to staff as soon as possible");

        this.log(Level.SEVERE, errMessage.getText());

        if(sender instanceof ProxiedPlayer) {
            sender.sendMessage(errMessage);
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
