package me.kixstar.kixbungeebridge;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.command.home.DelHomeCommand;
import me.kixstar.kixbungeebridge.command.home.HomeCommand;
import me.kixstar.kixbungeebridge.command.NickCommand;
import me.kixstar.kixbungeebridge.command.TpCommand;
import me.kixstar.kixbungeebridge.database.abstraction.player.KixPlayer;
import me.kixstar.kixbungeebridge.database.entities.Location;
import me.kixstar.kixbungeebridge.feature.NicknameService;
import me.kixstar.kixbungeebridge.feature.ServerCommandService;
import me.kixstar.kixbungeebridge.feature.teleport.TeleportTransaction;
import me.kixstar.kixbungeebridge.database.Database;
import me.kixstar.kixbungeebridge.rabbitmq.RabbitMQ;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public final class KixBungeeBridge extends Plugin {

    private static KixBungeeBridge plugin;

    private static LuckPerms luckPerms;

    @Override
    public void onEnable() {
        plugin = this;

        luckPerms = LuckPermsProvider.get();
        Preconditions.checkNotNull(luckPerms, "This plugin depends on LuckPerms to run");

        //connect to MongoDB
        Database.bind();
        //connect to RabbitMQ
        RabbitMQ.bind(Config.getServerHandle());

        //connect services that depend on RabbitMQ
        NicknameService.register();
        ServerCommandService.register();
        TeleportTransaction.register();

        this.getProxy().getPluginManager().registerCommand(this, new NickCommand());
        //command for testing
        this.getProxy().getPluginManager().registerCommand(this, new TpCommand());
        //note: doesn't work yet
        this.getProxy().getPluginManager().registerCommand(this, new HomeCommand());
        this.getProxy().getPluginManager().registerCommand(this, new DelHomeCommand());

    }

    @Override
    public void onDisable() {

        ServerCommandService.unregister();
        TeleportTransaction.unregister();
        NicknameService.unregister();

        RabbitMQ.unbind();

        plugin = null;
    }

    public static KixBungeeBridge getInstance() {
        return plugin;
    }

    public static LuckPerms getLuckPerms() {
        return luckPerms;
    }
}