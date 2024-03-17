package dev.jay.kacore.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PrefixConfig {
    private final KaCore plugin;
    private File file;
    private JsonObject config;
    public static final String PERMISSIONS_KEY = "permissions";
    public static final String PLAYERS_KEY = "players";

    public PrefixConfig(KaCore kaCore) {
        this.plugin = kaCore;
    }

    public void reloadConfig() {
        this.file = new File(this.plugin.getDataFolder(), "prefixes.json");

        if (!this.file.exists()) {
            try (InputStream resourceStream = this.plugin.getResourceAsStream("prefixes.json")) {
                Files.copy(resourceStream, this.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        try (Reader reader = new FileReader(this.file)) {
            this.config = new JsonParser().parse(reader).getAsJsonObject();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void saveConfig() {
        try (PrintWriter printWriter = new PrintWriter(this.file)) {
            printWriter.println(Utils.PRETTY_GSON.toJson(this.config));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @NotNull
    public String getPermissionPrefix(String permission) {
        JsonArray jsonArray = this.config.getAsJsonArray(PERMISSIONS_KEY);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return this.getDefaultPrefix();
        }

        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.get("permission").getAsString().equals(permission)) {
                return Utils.color(jsonObject.get("prefix").getAsString());
            }
        }

        return this.getDefaultPrefix();
    }

    @NotNull
    public String getPlayerPrefix(String playerName) {
        JsonArray jsonArray = this.config.getAsJsonArray(PLAYERS_KEY);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return this.getDefaultPrefix();
        }

        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.get("player").getAsString().equals(playerName)) {
                return Utils.color(jsonObject.get("prefix").getAsString());
            }
        }

        return this.getDefaultPrefix();
    }

    @NotNull
    public List<String> getPermissions() {
        JsonArray jsonArray = this.config.getAsJsonArray(PERMISSIONS_KEY);
        ArrayList<String> permissionsList = new ArrayList<>();

        if (jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                permissionsList.add(jsonObject.get("permission").getAsString());
            }
        }

        return permissionsList;
    }

    @NotNull
    public List<String> getPlayers() {
        JsonArray jsonArray = this.config.getAsJsonArray(PLAYERS_KEY);
        ArrayList<String> playersList = new ArrayList<>();

        if (jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                playersList.add(jsonObject.get("player").getAsString());
            }
        }

        return playersList;
    }

    @NotNull
    public String getDefaultPrefix() {
        return Utils.color(this.config.get("default").getAsString());
    }

    @NotNull
    public String getConsolePrefix() {
        return Utils.color(this.config.get("console").getAsString());
    }

    public JsonObject getConfig() {
        return this.config;
    }
}