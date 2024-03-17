package dev.jay.kacore.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.objects.PrefixConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.TimeZone;
import java.util.UUID;

public class Utils {
    public static final Gson DEFAULT_GSON = new GsonBuilder().create();
    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String rainbowify(String text) {
        StringBuilder rainbowText = new StringBuilder();
        String rainbowColors = "\u001B[31m\u001B[33m\u001B[32m\u001B[34m"; // Rainbow colors in ANSI format
        int colorIndex = 0;
        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar != ' ') {
                String currentColor = rainbowColors.substring(colorIndex * 5, (colorIndex + 1) * 5);
                rainbowText.append(currentColor).append(currentChar);
                colorIndex = (colorIndex + 1) % (rainbowColors.length() / 5);
            } else {
                rainbowText.append(currentChar);
            }
        }
        return rainbowText.toString();
    }


    @NotNull
    public static String getDateFormat(String pattern, long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        return simpleDateFormat.format(timestamp);
    }

    @NotNull
    public static String getDateFormat(String pattern) {
        return getDateFormat(pattern, System.currentTimeMillis());
    }

    public static void sendActionBar(ProxiedPlayer proxiedPlayer, String string) {
        proxiedPlayer.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(string));
    }

    @Nullable
    public static ProxiedPlayer getPlayer(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }

    @Nullable
    public static ProxiedPlayer getPlayer(UUID uuid) {
        return ProxyServer.getInstance().getPlayer(uuid);
    }

    @NotNull
    public static Collection<ProxiedPlayer> getPlayers() {
        return Collections.unmodifiableCollection(ProxyServer.getInstance().getPlayers());
    }

    @NotNull
    public static String getUserPrefix(CommandSender commandSender) {
        KaCore kaCore = KaCore.getInstance();
        PrefixConfig prefixConfig = kaCore.getPrefixConfig();
        if (!(commandSender instanceof ProxiedPlayer)) {
            return prefixConfig.getConsolePrefix();
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        String playerName = player.getName();

        if (prefixConfig.getPlayers().contains(playerName)) {
            return prefixConfig.getPlayerPrefix(playerName);
        }

        for (String permission : prefixConfig.getPermissions()) {
            if (player.hasPermission(permission)) {
                return prefixConfig.getPermissionPrefix(permission);
            }
        }

        return prefixConfig.getDefaultPrefix();
    }


    public static String getPingColor(int ping) {
        return ping > 200 ? "§4" : (ping > 100 ? "§e" : "§a");
    }

    public static boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z_0-9]{3,16}$");
    }
}