package me.kixstar.kixbungeebridge;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    private static Configuration config;

    private static File getDataFolder() {
        Plugin plugin = KixBungeeBridge.getInstance();
        if(plugin != null) return plugin.getDataFolder();
        throw new RuntimeException("Cannot get DataFolder of plugin that isn't enabled");
    }

    private static Configuration getConfig() {
        if(config instanceof Configuration) return config;
        try {
            config = ConfigurationProvider
                    .getProvider(YamlConfiguration.class)
                    .load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static boolean isProd() {
        return getConfig().getBoolean("environment", false);
    }

    public static String getRabbitMQ() {
        return getConfig().getString("rabbit-mq", "amqp://admin:root@localhost:5672");
    }

    public static String getServerHandle() {
        Object result = getConfig().get("server-handle");
        if(result instanceof String) return (String) result;
        throw new RuntimeException("Property \"server-handle\" must be provided in the config.yml so the plugin can function properly");
    }

    public static String getMongoDB() {
        return getConfig().getString("mongo-db", "mongodb://admin:root@localhost:27017");
    }
}
