package me.kixstar.kixbungeebridge;

import me.kixstar.kixbungeebridge.command.NickCommand;
import net.md_5.bungee.api.plugin.Plugin;

public final class KixBungeeBridge extends Plugin {

    private static KixBungeeBridge plugin;

    @Override
    public void onEnable() {
        plugin = this;

        this.getProxy().getPluginManager().registerCommand(this, new NickCommand());
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    public static KixBungeeBridge getInstance() {
        return plugin;
    }
}