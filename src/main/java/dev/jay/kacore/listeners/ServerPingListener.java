package dev.jay.kacore.listeners;

import dev.jay.kacore.KaCore;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerPingListener implements Listener {
    private final KaCore plugin;

    public ServerPingListener(KaCore kaCore) {
        this.plugin = kaCore;
    }

    @EventHandler
    public void onServerPing(ProxyPingEvent proxyPingEvent) {
        int maxPlayers = 1000;  // Set the maximum players to desired value
        proxyPingEvent.getResponse().setPlayers(new ServerPing.Players(maxPlayers, this.plugin.getPlayerCount(), new ServerPing.PlayerInfo[0]));
    }
}
