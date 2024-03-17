package dev.jay.kacore.redis.handlers;

import com.google.gson.JsonObject;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffChatHandler {
    private static final KaCore kaCore = KaCore.getInstance();
    private static final RedisBungeeHook redisBungeeHook = kaCore.getRedisBungeeHook();
    public static void sendStaffChat(CommandSender commandSender, String message) {
        if (message == null || (message = message.trim()).isEmpty()) {
            return;
        }

        String serverName = commandSender instanceof ProxiedPlayer ? ((ProxiedPlayer) commandSender).getServer().getInfo().getName() : null;
        String playerName = commandSender.getName();

        // Send message to staff chat via RedisBungee
        if (redisBungeeHook.isHooked()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("username", playerName);
            jsonObject.addProperty("rank-prefix", Utils.getUserPrefix(commandSender));
            jsonObject.addProperty("server-name", serverName);
            jsonObject.addProperty("message", message);
            jsonObject.addProperty("server-id", redisBungeeHook.getAPI().getServerId());
            redisBungeeHook.sendPubSubMessage(RedisBungeeHook.Channel.STAFF_CHAT, jsonObject.toString());
        }

        handleStaffChat(playerName, Utils.getUserPrefix(commandSender), serverName, message);
    }

    public static void handleStaffChat(String username, String rankPrefix, String serverName, String message) {
        KaCore kaCore = KaCore.getInstance();
        if (serverName == null) {
            serverName = "PROXY";
        }

        String playerNameWithPrefix = rankPrefix + username;
        message = Utils.color(message);
        String formattedMessage = String.format("§c§l[STAFF] §7[%s] §7%s§6: §a%s", serverName.toUpperCase(), playerNameWithPrefix, message);

        for (ProxiedPlayer proxiedPlayer : Utils.getPlayers()) {
            if (!proxiedPlayer.hasPermission("kacore.staff")) {
                continue;
            }

            proxiedPlayer.sendMessage(formattedMessage);
        }

        kaCore.getProxy().getConsole().sendMessage(formattedMessage);
    }
}