package dev.jay.kacore.profiles;

import com.github.alviannn.sqlhelper.Results;
import com.github.alviannn.sqlhelper.SQLHelper;
import com.github.alviannn.sqlhelper.utils.Closer;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public class PlayerProfile implements Cloneable {
    private static Cache<UUID, PlayerProfile> profileCache;
    private final UUID uniqueId;
    private final String name;
    private final List<String> ignoredPlayers;
    private boolean useGlobalChat;

    public PlayerProfile(UUID uniqueId, String name, ArrayList<String> ignoredPlayers, boolean useGlobalChat) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.ignoredPlayers = ignoredPlayers;
        this.useGlobalChat = useGlobalChat;
    }

    public static void initialize() {
        profileCache = Cache2kBuilder.of(UUID.class, PlayerProfile.class)
                .name("profile_cache")
                .entryCapacity(Long.MAX_VALUE)
                .eternal(true)
                .build();
    }

    @NotNull
    public static CompletableFuture<PlayerProfile> getProfile(UUID uniqueId) {
        if (profileCache.containsKey(uniqueId)) {
            return CompletableFuture.completedFuture(profileCache.get(uniqueId));
        }
        return CompletableFuture.supplyAsync(() -> {
            SQLHelper sqlHelper = KaCore.getInstance().getHelper();
            PlayerProfile playerProfile = null;
            try {
                try (Closer closer = new Closer()) {
                    Results results = closer.add(sqlHelper.query("SELECT * FROM profiles WHERE uuid = ?;").getResults(uniqueId.toString()));
                    ResultSet resultSet = results.getResultSet();
                    if (resultSet.next()) {
                        String name = resultSet.getString("name");
                        boolean useGlobalChat = resultSet.getBoolean("globalchat");
                        String ignored = resultSet.getString("ignored");
                        ArrayList<String> ignoredPlayers = new ArrayList<>();
                        if (!ignored.isEmpty()) {
                            String[] ignoredArray = ignored.split(",");
                            ignoredPlayers.addAll(Arrays.asList(ignoredArray));
                        }
                        playerProfile = new PlayerProfile(uniqueId, name, ignoredPlayers, useGlobalChat);
                    }
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            if (playerProfile != null) {
                profileCache.put(uniqueId, playerProfile);
            }
            return playerProfile;
        });
    }

    @NotNull
    public static CompletableFuture<PlayerProfile> getProfile(String name) {
        for (CacheEntry<UUID, PlayerProfile> cacheEntry : profileCache.entries()) {
            PlayerProfile playerProfile = cacheEntry.getValue();
            if (playerProfile.name.equals(name)) {
                return CompletableFuture.completedFuture(playerProfile);
            }
        }
        return CompletableFuture.supplyAsync(() -> {
            SQLHelper sqlHelper = KaCore.getInstance().getHelper();
            PlayerProfile playerProfile = null;
            try {
                try (Closer closer = new Closer()) {
                    Results results = closer.add(sqlHelper.query("SELECT * FROM profiles WHERE name = ?;").results(name));
                    ResultSet resultSet = results.getResultSet();
                    if (resultSet.next()) {
                        UUID uniqueId = UUID.fromString(resultSet.getString("uuid"));
                        boolean useGlobalChat = resultSet.getBoolean("globalchat");
                        String ignored = resultSet.getString("ignored");
                        ArrayList<String> ignoredPlayers = new ArrayList<>();
                        if (!ignored.isEmpty()) {
                            String[] ignoredArray = ignored.split(",");
                            ignoredPlayers.addAll(Arrays.asList(ignoredArray));
                        }
                        playerProfile = new PlayerProfile(uniqueId, name, ignoredPlayers, useGlobalChat);
                    }
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            if (playerProfile != null) {
                profileCache.put(playerProfile.getUniqueId(), playerProfile);
            }
            return playerProfile;
        });
    }

    private UUID getUniqueId() {
        return this.uniqueId;
    }

    @NotNull
    public static CompletableFuture<PlayerProfile> getOrCreateProfile(ProxiedPlayer proxiedPlayer) {
        String name = proxiedPlayer.getName();
        UUID uniqueId = proxiedPlayer.getUniqueId();
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<PlayerProfile> completableFuture = PlayerProfile.getProfile(uniqueId);
            Supplier<PlayerProfile> supplier = () -> {
                PlayerProfile playerProfile = new PlayerProfile(uniqueId, name, new ArrayList<>(), false);
                profileCache.put(uniqueId, playerProfile);
                return playerProfile;
            };
            try {
                PlayerProfile playerProfile = completableFuture.join();
                return playerProfile == null ? supplier.get() : playerProfile;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return supplier.get();
            }
        });
    }

    public static void delete(UUID uniqueId, boolean bl) {
        profileCache.remove(uniqueId);
        if (!bl) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            SQLHelper sqlHelper = KaCore.getInstance().getHelper();
            try {
                sqlHelper.query("DELETE FROM profiles WHERE uuid = ?;").execute(uniqueId.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void shutdown() {
        for (Map.Entry<UUID, PlayerProfile> entry : profileCache.asMap().entrySet()) {
            try {
                entry.getValue().save().join();
            } catch (Exception e) {
                // empty catch block
            }
        }
        try {
            profileCache.clearAndClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static CompletableFuture<Void> saveProfile(PlayerProfile playerProfile) {
        return CompletableFuture.runAsync(() -> {
            KaCore kaCore = KaCore.getInstance();
            SQLHelper sqlHelper = kaCore.getHelper();
            try {
                try (Closer closer = new Closer()) {
                    Results results = closer.add(sqlHelper.query("SELECT uuid FROM profiles WHERE uuid = ?;").getResults(playerProfile.getUniqueId().toString()));
                    ResultSet resultSet = results.getResultSet();
                    boolean exists = resultSet.next();
                    if (exists) {
                        sqlHelper.query("UPDATE profiles SET ignored = ?, globalchat = ? WHERE uuid = ?;")
                                .execute(playerProfile.ignoresToString(), playerProfile.isUseGlobalChat(), playerProfile.getUniqueId().toString());
                    } else {
                        sqlHelper.query("INSERT INTO profiles (uuid, name, ignored, globalchat) VALUES (?, ?, ?, ?);")
                                .execute(playerProfile.getUniqueId().toString(), playerProfile.name, playerProfile.ignoresToString(), playerProfile.isUseGlobalChat());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }


    @NotNull
    public CompletableFuture<Void> addToIgnore(String name) {
        if (this.hasIgnored(name)) {
            return CompletableFuture.completedFuture(null);
        }
        this.ignoredPlayers.add(name);
        return this.save();
    }

    @NotNull
    public CompletableFuture<Void> removeFromIgnore(String name) {
        if (!this.hasIgnored(name)) {
            return CompletableFuture.completedFuture(null);
        }
        this.ignoredPlayers.remove(name);
        return this.save();
    }

    public boolean hasIgnored(String name) {
        return this.ignoredPlayers.contains(name);
    }

    @NotNull
    private String ignoresToString() {
        if (this.ignoredPlayers.isEmpty()) {
            return "";
        }
        return String.join(",", this.ignoredPlayers);
    }

    @NotNull
    public CompletableFuture<Void> setUseGlobalChat(boolean useGlobalChat) {
        this.useGlobalChat = useGlobalChat;
        return this.save();
    }

    @NotNull
    public CompletableFuture<Void> save() {
        return PlayerProfile.saveProfile(this);
    }

    @Nullable
    public ProxiedPlayer getPlayer() {
        return Utils.getPlayer(this.uniqueId);
    }

    public static Cache<UUID, PlayerProfile> getProfileCache() {
        return profileCache;
    }

    public boolean isUseGlobalChat() {
        return this.useGlobalChat;
    }

    public List<String> getIgnoredPlayers() {
        return this.ignoredPlayers;
    }
}