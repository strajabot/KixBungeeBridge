package me.kixstar.kixbungeebridge;

import me.kixstar.kixbungeebridge.command.NickCommand;
import me.kixstar.kixbungeebridge.feature.NicknameService;
import me.kixstar.kixbungeebridge.feature.ServerCommandService;
import me.kixstar.kixbungeebridge.feature.TeleportTransaction;
import me.kixstar.kixbungeebridge.rabbitmq.RabbitMQ;
import net.md_5.bungee.api.plugin.Plugin;

public final class KixBungeeBridge extends Plugin {

    private static KixBungeeBridge plugin;

    @Override
    public void onEnable() {
        plugin = this;

        //connect to RabbitMQ
        RabbitMQ.bind();

        //connect services that depend on RabbitMQ
        NicknameService.register();
        ServerCommandService.register();
        TeleportTransaction.register();

        this.getProxy().getPluginManager().registerCommand(this, new NickCommand());

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
}