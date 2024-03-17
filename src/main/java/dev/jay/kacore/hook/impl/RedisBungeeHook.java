package dev.jay.kacore.hook.impl;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.Hook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.redis.RedisMessageListener;
import dev.jay.kacore.redis.entries.PlayerEntry;
import dev.jay.kacore.redis.entries.StaffEntry;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedisBungeeHook extends Hook {
    public static final String PLAYER_KEY = "kacore:players";
    public static final String STAFF_KEY = "kacore:staffs";
    public static final String PING_KEY = "kacore:ping:";
    public static final String CHANNEL_PREFIX = "KaCore--";
    private final KaCore plugin;
    private final Map<String, Long> queuedPlayers = new ConcurrentHashMap<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private ExecutorService threadPool;

    public RedisBungeeHook(KaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "RedisBungee";
    }

    @Override
    public boolean isHooked() {
        return Config.USE_REDIS_BUNGEE && super.isHooked();
    }

    @Override
    public void initialize() {
        if (initialized.getAndSet(true)) return;

        queuedPlayers.clear();
        scheduler = Executors.newScheduledThreadPool(2);
        threadPool = Executors.newCachedThreadPool();
        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.registerListener(plugin, new RedisMessageListener(plugin));

        for (Channel channel : Channel.values()) {
            getAPI().registerPubSubChannels(channel.getChannelName());
        }

        startPlayerUpdateTask();
        initialized.set(true);
    }

    @Override
    public void shutdown() {
        scheduler.shutdownNow();
        threadPool.shutdownNow();
        JedisPool jedisPool = getJedisPool();
        if (jedisPool == null) return;
        try (Jedis jedis = jedisPool.getResource()) {
            for (ProxiedPlayer proxiedPlayer : Utils.getPlayers()) {
                jedis.hdel(PLAYER_KEY, proxiedPlayer.getName());
                jedis.del(PING_KEY + proxiedPlayer.getName());
            }
        } catch (Exception ignored) {
        }
        initialized.set(false);
    }

    public RedisBungeeAPI getAPI() {
        if (!isInitialized()) {
            initialize();
        }
        return RedisBungeeAPI.getRedisBungeeApi();
    }


    private void startPlayerUpdateTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try (Jedis jedis = getJedisPool().getResource()) {
                for (ProxiedPlayer proxiedPlayer : Utils.getPlayers()) {
                    if (queuedPlayers.containsKey(proxiedPlayer.getName())) {
                        long lastUpdate = queuedPlayers.get(proxiedPlayer.getName());
                        long timeElapsed = System.currentTimeMillis() - lastUpdate;
                        if (timeElapsed < 3000L) continue;
                        queuedPlayers.remove(proxiedPlayer.getName());
                    }
                    PlayerEntry playerEntry = new PlayerEntry(proxiedPlayer);
                    String json = Utils.DEFAULT_GSON.toJson(playerEntry);
                    jedis.hset(PLAYER_KEY, proxiedPlayer.getName(), json);
                    if (playerEntry.isStaff()) {
                        StaffEntry staffEntry = new StaffEntry(proxiedPlayer.getName(), proxiedPlayer.getUniqueId());
                        String staffJson = Utils.DEFAULT_GSON.toJson(staffEntry);
                        jedis.hset(STAFF_KEY, proxiedPlayer.getName(), staffJson);
                    } else {
                        jedis.hdel(STAFF_KEY, proxiedPlayer.getName());
                    }
                }
            } catch (Exception ignored) {
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    public int getGlobalCount() {
        try (Jedis jedis = getJedisPool().getResource()) {
            return Math.toIntExact(jedis.hlen(PLAYER_KEY));
        } catch (Exception ignored) {
            return 0;
        }
    }

    @Nullable
    public PlayerEntry getPlayer(String name) {
        try (Jedis jedis = getJedisPool().getResource()) {
            String json = jedis.hget(PLAYER_KEY, name);
            return Utils.DEFAULT_GSON.fromJson(json, PlayerEntry.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    @NotNull
    public Set<PlayerEntry> getStaffMembers() {
        Set<PlayerEntry> staffSet = new HashSet<>();
        try (Jedis jedis = getJedisPool().getResource()) {
            Set<String> staffNames = jedis.hkeys(STAFF_KEY);
            for (String name : staffNames) {
                String json = jedis.hget(PLAYER_KEY, name);
                PlayerEntry playerEntry = Utils.DEFAULT_GSON.fromJson(json, PlayerEntry.class);
                staffSet.add(playerEntry);
            }
        } catch (Exception ignored) {
        }
        return Collections.unmodifiableSet(staffSet);
    }

    public void sendPubSubMessage(Channel channel, String message) {
        if (!isHooked()) return;
        getAPI().sendChannelMessage(channel.getChannelName(), message);
    }

    @Nullable
    public JedisPool getJedisPool() {
        if (!isHooked()) return null;
        try {
            PluginManager pluginManager = plugin.getProxy().getPluginManager();
            RedisBungee redisBungee = (RedisBungee) pluginManager.getPlugin("RedisBungee");
            Field poolField = RedisBungee.class.getDeclaredField("pool");
            poolField.setAccessible(true);
            return (JedisPool) poolField.get(redisBungee);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean doesPlayerExists(String name) {
        try (Jedis jedis = getJedisPool().getResource()) {
            return jedis.hexists(PLAYER_KEY, name);
        } catch (Exception ignored) {
            return false;
        }
    }

    @NotNull
    public CompletableFuture<Integer> getPing(String name) {
        JedisPool jedisPool = getJedisPool();
        if (!doesPlayerExists(name) || jedisPool == null) {
            return CompletableFuture.completedFuture(-1);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String pingStr = jedis.get(PING_KEY + name);
                return pingStr != null ? Integer.parseInt(pingStr) : -1;
            } catch (Exception ignored) {
                return -1;
            }
        }, threadPool);
    }

    public void addToQueue(ProxiedPlayer player) {
        queuedPlayers.put(player.getName(), System.currentTimeMillis());
    }

    public CompletableFuture<Void> cleanUser(String name) {
        queuedPlayers.remove(name);
        JedisPool jedisPool = getJedisPool();
        if (jedisPool == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.hdel(PLAYER_KEY, name);
                jedis.hdel(STAFF_KEY, name);
                jedis.del(PING_KEY + name);
            } catch (Exception ignored) {
            }
        }, threadPool);
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public enum Channel {
        PING,
        ANNOUNCEMENT,
        STAFF_CHAT,
        GLOBAL_CHAT,
        BADWORD_VERBOSE,
        CLEAR_CHAT,
        PRIVATE_MESSAGE;

        public String getChannelName() {
            return RedisBungeeHook.CHANNEL_PREFIX + name();
        }

        public String toString() {
            return getChannelName();
        }

        @Nullable
        public static Channel fromString(String name) {
            for (Channel channel : values()) {
                if (channel.getChannelName().equals(name)) {
                    return channel;
                }
            }
            return null;
        }
    }
}