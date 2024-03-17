package dev.jay.kacore.listeners;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.objects.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ChatListener implements Listener {
    private final KaCore plugin;

    public ChatListener(KaCore kaCore) {
        this.plugin = kaCore;
    }

    @EventHandler
    public void onChat(ChatEvent chatEvent) {
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) chatEvent.getSender();
        String message = chatEvent.getMessage();

        // Check if the message starts with "/"
        if (!message.startsWith("/")) {
            return;
        }

        // Check if SERVER_SWITCHER_ENABLED is true and player has the required permission
        if (Config.SERVER_SWITCHER_ENABLED && proxiedPlayer.hasPermission("serverswitcher.switch")) {
            String command = message.replace("/", "");

            CompletableFuture.runAsync(() -> {
                ServerInfo serverInfo = findServer(command);
                if (serverInfo != null) {
                    proxiedPlayer.sendMessage(Config.PREFIX +
                            "§bPlease wait while we connect you to §a" + serverInfo.getName());
                    proxiedPlayer.connect(serverInfo);
                    chatEvent.setCancelled(true);
                }
            });
        }
    }

    private ServerInfo findServer(String command) {
        Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
        for (ServerInfo serverInfo : servers) {
            if (serverInfo.getName().equalsIgnoreCase(command)) {
                return serverInfo;
            }
        }
        return null;
    }
}