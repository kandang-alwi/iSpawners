package dev.jay.kacore.redis;

import com.github.alviannn.sqlhelper.utils.Closer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.redis.entries.MessagePayload;
import dev.jay.kacore.redis.handlers.GlobalChatHandler;
import dev.jay.kacore.redis.handlers.StaffChatHandler;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RedisMessageListener implements Listener {
    private final KaCore plugin;
    private final JsonParser parser = new JsonParser();

    public RedisMessageListener(KaCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRedisMessage(PubSubMessageEvent pubSubMessageEvent1) {
        RedisBungeeHook hook = this.plugin.getRedisBungeeHook();
        if (!hook.isHooked()) {
            return;
        }

        String channel = pubSubMessageEvent1.getChannel();
        if (!channel.startsWith("KaCore--")) {
            return;
        }

        RedisBungeeHook.Channel subChannel = RedisBungeeHook.Channel.fromString(channel);
        if (subChannel == null) {
            return;
        }

        switch (subChannel) {
            case STAFF_CHAT: {
                JsonObject json = this.parser.parse(pubSubMessageEvent1.getMessage()).getAsJsonObject();
                String username = json.get("username").getAsString();
                String rankPrefix = json.get("rank-prefix").getAsString();
                String serverName = json.get("server-name").getAsString();
                String message = json.get("message").getAsString();
                String serverId = json.get("server-id").getAsString();

                if (hook.getAPI().getServerId().equals(serverId)) {
                    return;
                }

                StaffChatHandler.handleStaffChat(username, rankPrefix, serverName, message);
                return;
            }

            case GLOBAL_CHAT: {
                JsonObject json = this.parser.parse(pubSubMessageEvent1.getMessage()).getAsJsonObject();
                String username = json.get("username").getAsString();
                String prefixFormat = json.get("prefix-format").getAsString();
                String message = json.get("message").getAsString();
                String serverId = json.get("server-id").getAsString();

                if (hook.getAPI().getServerId().equals(serverId)) {
                    return;
                }

                GlobalChatHandler.handleGlobalChat(username, prefixFormat, message);
                return;
            }

            case BADWORD_VERBOSE: {
                JsonObject json = this.parser.parse(pubSubMessageEvent1.getMessage()).getAsJsonObject();
                String username = json.get("username").getAsString();
                String message = json.get("message").getAsString();
                String serverId = json.get("server-id").getAsString();

                if (hook.getAPI().getServerId().equals(serverId)) {
                    return;
                }

                GlobalChatHandler.verboseBlockedWord(username, message);
                return;
            }

            case PING: {
                String playerName = pubSubMessageEvent1.getMessage();
                ProxiedPlayer player = Utils.getPlayer(playerName);

                if (player == null) {
                    return;
                }

                JedisPool jedisPool = hook.getJedisPool();
                if (jedisPool == null) {
                    return;
                }

                try (Closer closer = new Closer()) {
                    Jedis jedis = closer.add(jedisPool.getResource());
                    Pipeline pipeline = jedis.pipelined();
                    pipeline.set("kacore:ping:" + player.getName(), "" + player.getPing());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            case CLEAR_CHAT: {
                JsonObject json = this.parser.parse(pubSubMessageEvent1.getMessage()).getAsJsonObject();
                String serverId = json.get("server-id").getAsString();

                if (hook.getAPI().getServerId().equals(serverId)) {
                    return;
                }

                ArrayList<String> spaces = new ArrayList<String>();
                for (int i = 0; i < 200; i++) {
                    spaces.add(" ");
                }
                String clearMessage = String.join("\\n", spaces);

                for (ProxiedPlayer player : Utils.getPlayers()) {
                    player.sendMessage(clearMessage);
                }

                this.plugin.getProxy().broadcast("§cGlobal Chat has been cleared by administrator!");
                return;
            }

            case PRIVATE_MESSAGE: {
                String jsonMessage = pubSubMessageEvent1.getMessage();
                MessagePayload payload = Utils.DEFAULT_GSON.fromJson(jsonMessage, MessagePayload.class);
                ProxiedPlayer targetPlayer = Utils.getPlayer(payload.getTargetName());

                if (targetPlayer == null) {
                    return;
                }

                CompletableFuture.runAsync(() -> {
                    PlayerProfile playerProfile = PlayerProfile.getProfile(targetPlayer.getName()).join();
                    if (playerProfile != null && playerProfile.hasIgnored(payload.getSenderName()) && !payload.isSenderStaff()) {
                        return;
                    }

                    // Check if sender is on anti-socialspy list
                    if (Config.ANTI_SOCIALSPY_ENABLED && Config.ANTI_SOCIALSPY_LIST.contains(payload.getSenderName())) {
                        return;
                    }

                    plugin.messageData.put(targetPlayer.getName(), payload.getSenderName());
                    targetPlayer.sendMessage(payload.getMessage());
                    for (String spy : plugin.spyingPlayers) {
                        ProxiedPlayer spyPlayer = Utils.getPlayer(spy);
                        if (spyPlayer == null) {
                            plugin.spyingPlayers.remove(spy);
                            continue;
                        }
                        // Check if sender is on anti-socialspy list
                        if (Config.ANTI_SOCIALSPY_ENABLED && Config.ANTI_SOCIALSPY_LIST.contains(payload.getSenderName())) {
                            continue;
                        }
                        spyPlayer.sendMessage(payload.getSpyMessage());
                    }
                });
                return;
            }
        }
    }
}