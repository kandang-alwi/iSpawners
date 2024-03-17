package dev.jay.kacore.redis.entries;

// MessagePlayerEntry.java
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

public class MessagePlayerEntry {
    private final String username;
    private final String prefix;

    public MessagePlayerEntry(String username, String prefix) {
        this.username = username;
        this.prefix = prefix;
    }

    public MessagePlayerEntry(PlayerEntry playerEntry) {
        this.username = playerEntry.getUsername();
        this.prefix = playerEntry.getRankPrefix();
    }

    @Nullable
    public ProxiedPlayer getPlayer() {
        return Utils.getPlayer(username);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUsername() {
        return username;
    }

    public String getFormattedName() {
        return prefix + username;
    }
}
