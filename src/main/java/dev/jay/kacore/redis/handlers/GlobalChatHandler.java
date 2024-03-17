package dev.jay.kacore.redis.handlers;

import com.google.gson.JsonObject;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class GlobalChatHandler {
    public static void sendGlobalChat(ProxiedPlayer proxiedPlayer, String message) {
        JsonObject jsonObject;
        String prefix;
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        RedisBungeeHook redisBungeeHook = KaCore.getInstance().getRedisBungeeHook();

        if (Config.CHATFILTER_ENABLED && !proxiedPlayer.hasPermission("kacore.admin") && !proxiedPlayer.hasPermission("kacore.staff")) {
            boolean filtered = false;
            String originalMessage = message;

            if (filtered) {
                proxiedPlayer.sendMessage(Config.CHATFILTER_MESSAGE);
                GlobalChatHandler.verboseBlockedWord(proxiedPlayer.getName(), message);

                if (redisBungeeHook.isHooked()) {
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("username", proxiedPlayer.getName());
                    jsonObject.addProperty("message", message);
                    jsonObject.addProperty("server-id", redisBungeeHook.getAPI().getServerId());
                    redisBungeeHook.sendPubSubMessage(RedisBungeeHook.Channel.BADWORD_VERBOSE, jsonObject.toString());
                }
                message = originalMessage;
            }
        }

        String formattedMessage = proxiedPlayer.hasPermission("karank.donate") || proxiedPlayer.hasPermission("kacore.staff") ? Utils.color(message) : message;
        String serverName = serverInfo.getName();

        if (serverName.contains("hub") || serverName.contains("HUB") || serverName.contains("lobby") || serverName.contains("LOBBY")) {
            prefix = "§8[§aHUB§8] ";
        } else if (serverName.startsWith("event")) {
            prefix = "§8[§aEVENT§8] ";
        } else if (serverName.startsWith("survivalmix") || serverName.startsWith("SURVIVALMIX") || serverName.startsWith("SurvivalMix")) {
            prefix = "§8[§aSURVIVALMIX§8] ";
        } else {
            prefix = "§8[§a" + serverName.toUpperCase() + "§8] ";
        }

        String formattedPrefix = "§8[§dGLOBAL§8] " + prefix + Utils.getUserPrefix(proxiedPlayer);
        String coloredMessage = proxiedPlayer.hasPermission("kacore.donate") ? "§a" + formattedMessage : formattedMessage;
        coloredMessage = proxiedPlayer.hasPermission("kacore.staff") ? "§c" + coloredMessage : coloredMessage;

        GlobalChatHandler.handleGlobalChat(proxiedPlayer.getName(), formattedPrefix, coloredMessage);

        if (redisBungeeHook.isHooked()) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("username", proxiedPlayer.getName());
            jsonObject.addProperty("prefix-format", formattedPrefix);
            jsonObject.addProperty("message", coloredMessage);
            jsonObject.addProperty("server-id", redisBungeeHook.getAPI().getServerId());
            redisBungeeHook.sendPubSubMessage(RedisBungeeHook.Channel.GLOBAL_CHAT, jsonObject.toString());
        }
    }

    public static void handleGlobalChat(String playerName, String prefix, String message) {
        ConcurrentMap<UUID, PlayerProfile> profileCache = PlayerProfile.getProfileCache().asMap();
        List<PlayerProfile> profiles = profileCache.values().stream()
                .filter(PlayerProfile::isUseGlobalChat) // <-- Filter berdasarkan useGlobalChat
                .collect(Collectors.toList());
        String formattedMessage = prefix + playerName + "§7: " + message;
        int count = 0;

        for (PlayerProfile profile : profiles) {
            ProxiedPlayer player = profile.getPlayer();

            if (player == null || (player.getServer().getInfo().getName().contains("hub") && !player.hasPermission("kacore.staff"))) {
                continue;
            }

            count++;
            player.sendMessage(formattedMessage);
        }

        GlobalChatHandler.writeStatistics(playerName, formattedMessage, count);
    }


    public static void verboseBlockedWord(String playerName, String message) {
        for (ProxiedPlayer player : Utils.getPlayers()) {
            if (player.hasPermission("kacore.staff")) {
                player.sendMessage(new TextComponent(Config.CHATFILTER_PREFIX + "§7" + playerName + ": " + message));
            }
        }
    }

    private static void writeStatistics(String playerName, String message, int count) {
    }
}