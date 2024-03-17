package dev.jay.kacore.hook;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginManager;

public abstract class Hook {

    public abstract String getName();

    public boolean isHooked() {
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        return pluginManager.getPlugin(getName()) != null;
    }

    public abstract void initialize();

    public abstract void shutdown();
}

