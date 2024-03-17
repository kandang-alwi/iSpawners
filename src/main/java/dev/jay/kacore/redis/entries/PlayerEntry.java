package dev.jay.kacore.redis.entries;

import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerEntry {
    private final String username;
    private final UUID id;
    private final String rankPrefix;
    private final String serverName;
    private boolean staff;
    private boolean hidden;

    public PlayerEntry(ProxiedPlayer proxiedPlayer) {
        this.username = proxiedPlayer.getName();
        this.id = proxiedPlayer.getUniqueId();
        this.rankPrefix = Utils.getUserPrefix(proxiedPlayer);
        this.serverName = proxiedPlayer.getServer().getInfo().getName();
        this.staff = proxiedPlayer.hasPermission("kacore.staff");
        this.hidden = false;  // Set to false, update accordingly if there's a way to determine if a player is hidden
    }


    public ProxiedPlayer getPlayer() {
        return Utils.getPlayer(username);
    }

    public CompletableFuture<PlayerProfile> getProfile() {
        return PlayerProfile.getProfile(id);
    }

    public boolean isStaff() {
        return staff;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getRankPrefix() {
        return rankPrefix;
    }

    public String getUsername() {
        return username;
    }

    public String getServerName() {
        return serverName;
    }
}