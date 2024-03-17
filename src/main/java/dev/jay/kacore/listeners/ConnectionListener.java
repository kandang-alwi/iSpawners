package dev.jay.kacore.listeners;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.profiles.PlayerProfile;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ConnectionListener implements Listener {
    private final KaCore plugin;

    public ConnectionListener(KaCore kaCore) {
        this.plugin = kaCore;
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        handleRemoveData(player);
        RedisBungeeHook redisBungeeHook = plugin.getRedisBungeeHook();
        if (redisBungeeHook.isHooked()) {
            redisBungeeHook.addToQueue(player);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PlayerProfile.delete(player.getUniqueId(), false);
        handleRemoveData(player);
        RedisBungeeHook redisBungeeHook = plugin.getRedisBungeeHook();
        if (redisBungeeHook.isHooked()) {
            redisBungeeHook.cleanUser(player.getName());
        }
    }

    private void handleRemoveData(ProxiedPlayer player) {
        plugin.hiddenPlayers.remove(player.getName());
        plugin.spyingPlayers.remove(player.getName());
        plugin.toggledStaffChat.remove(player.getName());
        plugin.disabledStaffChat.remove(player.getName());
        plugin.globalChatMap.remove(player.getUniqueId());
        plugin.messageData.remove(player.getName());
    }
}
