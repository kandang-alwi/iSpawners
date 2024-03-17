package dev.jay.kacore;

import com.github.alviannn.sqlhelper.Results;
import com.github.alviannn.sqlhelper.SQLHelper;
import com.github.alviannn.sqlhelper.utils.Closer;
import dev.jay.kacore.commands.*;
import dev.jay.kacore.commands.config.KaCoreCMD;
import dev.jay.kacore.commands.config.RedisConfigCMD;
import dev.jay.kacore.commands.messages.IgnoreCMD;
import dev.jay.kacore.commands.messages.MessageCMD;
import dev.jay.kacore.commands.messages.ReplyCMD;
import dev.jay.kacore.commands.messages.SocialSpyCMD;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.listeners.ChatListener;
import dev.jay.kacore.listeners.ConnectionListener;
import dev.jay.kacore.listeners.ServerPingListener;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.objects.PrefixConfig;
import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class KaCore extends Plugin {
    private static KaCore instance;

    public final Map<UUID, Long> globalChatMap = new ConcurrentHashMap<>();
    public final Map<String, String> messageData = new ConcurrentHashMap<>();
    public final Set<String> hiddenPlayers = ConcurrentHashMap.newKeySet();
    public final Set<String> spyingPlayers = ConcurrentHashMap.newKeySet();
    public final Set<String> disabledStaffChat = ConcurrentHashMap.newKeySet();
    public final Set<String> toggledStaffChat = ConcurrentHashMap.newKeySet();

    private int playerCount;
    private long lastPlayerCount;

    private RedisBungeeHook redisBungeeHook;
    private File configFile;
    private Configuration config;
    private ConfigurationProvider provider;
    private TaskScheduler scheduler;
    private PrefixConfig prefixConfig;
    private SQLHelper helper;

    @Override
    public void onEnable() {
        // Initialize instance, scheduler, and configuration provider
        instance = this;
        this.scheduler = this.getProxy().getScheduler();
        this.provider = ConfigurationProvider.getProvider(YamlConfiguration.class);

        // Create data folder and config file if they don't exist
        this.getDataFolder().mkdirs();
        this.configFile = new File(this.getDataFolder(), "config.yml");

        // Load config and set default values if necessary
        this.loadConfig();
        if (!this.config.contains("use-redis-bungee")) {
            this.config.set("use-redis-bungee", false);
            this.saveConfig();
        }

        this.prefixConfig = new PrefixConfig(this);
        this.prefixConfig.reloadConfig();

        // Initialize SQLHelper
        this.helper = SQLHelper.newBuilder(SQLHelper.Type.MYSQL)
                .setHost(this.config.getString("mysql-ip"))
                .setPort("3306")
                .setDatabase(this.config.getString("mysql-database") + "?" +
                        this.config.getBoolean("mysql-usessl") +
                        "&serverTimezone=UTC&autoReconnect=true")
                .setUsername(this.config.getString("mysql-username"))
                .setPassword(this.config.getString("mysql-password"))
                .toSQL();

        // Connect to database and create necessary tables
        try {
            this.helper.connect();
            this.helper.executeQuery("CREATE TABLE IF NOT EXISTS serverstats (server TINYTEXT NOT NULL, totalstaff INT NOT NULL);");
            this.helper.executeQuery("CREATE TABLE IF NOT EXISTS profiles (uuid TINYTEXT NOT NULL, name TINYTEXT NOT NULL, ignored MEDIUMTEXT NOT NULL);");

            // Check if the 'globalchat' column exists in the 'profiles' table
            if (!doesColumnExist("profiles", "globalchat")) {
                this.helper.executeQuery("ALTER TABLE profiles ADD globalchat BOOL NOT NULL DEFAULT '0' AFTER ignored;");
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize SQLHelper or create database tables.");
            e.printStackTrace();
            // Disable the plugin if database connection fails
            getProxy().getPluginManager().unregisterListeners(this);
            getProxy().getPluginManager().unregisterCommands(this);
            return;
        }


        // Initialize PlayerProfile
        PlayerProfile.initialize();

        // Register commands and listeners
        PluginManager pluginManager = this.getProxy().getPluginManager();
        this.scheduler.schedule(this, () -> {
            this.registerCommands(pluginManager);
            this.registerListeners(pluginManager);

            // Initialize and start redisBungeeHook
            this.redisBungeeHook = new RedisBungeeHook(this);
            try {
                if (this.redisBungeeHook.isHooked()) {
                    this.redisBungeeHook.initialize();
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            // Send plugin info
            this.sendPluginInfo(this.getProxy().getConsole());

            // Schedule task to update server stats every 3 minutes
            this.scheduler.schedule(this, this::updateServerStats, 0L, 3L, TimeUnit.MINUTES);
        }, 5L, TimeUnit.SECONDS);
    }


    @Override
    public void onDisable() {
        // Clear collections
        this.globalChatMap.clear();
        this.hiddenPlayers.clear();
        this.spyingPlayers.clear();
        this.messageData.clear();
        this.disabledStaffChat.clear();

        // Shutdown PlayerProfile
        PlayerProfile.shutdown();

        // Disconnect SQLHelper
        try {
            this.helper.disconnect();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Shutdown redisBungeeHook
        try {
            this.redisBungeeHook.shutdown();
        } catch (Exception exception) {
            // ignore exceptions
        }

        // Cancel scheduled tasks
        this.scheduler.cancel(this);

        // Clean up references
        instance = null;
        this.scheduler = null;
        this.config = null;
    }

    private boolean doesColumnExist(String tableName, String columnName) throws SQLException {
        ResultSet resultSet = this.helper.getConnection().getMetaData().getColumns(null, null, tableName, columnName);
        return resultSet.next();
    }


    private void updateServerStats() {
        try {
            int staffCount = 0;
            String serverName = "kandangalwi";

            // Count online staff members
            for (ProxiedPlayer proxiedPlayer : Utils.getPlayers()) {
                if (proxiedPlayer.hasPermission("kacore.staff")) {
                    staffCount++;
                }
            }

            // Check if the server exists in the database
            if (!doesServerExists(serverName)) {
                // Insert new server stats if it doesn't exist
                this.helper.query("INSERT INTO serverstats (server, totalstaff) VALUES (?, ?);").execute(serverName, staffCount);
            } else {
                // Update server stats if it already exists
                this.helper.query("UPDATE serverstats SET totalstaff = ? WHERE server = ?;").execute(staffCount, serverName);
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to modify/check the 'serverstats' MYSQL database!");
            e.printStackTrace();
        }
    }


    public void sendPluginInfo(CommandSender commandSender) {
        int onlinePlayers = redisBungeeHook.isHooked() ? redisBungeeHook.getGlobalCount() : getProxy().getOnlineCount();

        commandSender.sendMessage(Utils.rainbowify("[*]-----------------------------------------[*]"));
        commandSender.sendMessage("§6 * §6KA-CORE §7- §cv" + getDescription().getVersion());
        commandSender.sendMessage(" ");
        commandSender.sendMessage("§6 * §dAuthor: §f" + getDescription().getAuthor());
        commandSender.sendMessage("§6 * §dOnline: §f" + onlinePlayers);
        commandSender.sendMessage("§6 * §dCurrent date: §f" + Utils.getDateFormat("dd/MM/yyyy - HH:mm:ss") + " (UTC+7)");
        commandSender.sendMessage("\n");

        commandSender.sendMessage("§aDevelopment Department Rooster");
        commandSender.sendMessage(" ");
        commandSender.sendMessage("§6 * §cAuthor: §2Jay§e");
        commandSender.sendMessage("§6 * §cExecutive: §axLqcy§e, §aDio§e, §aJay§e, §aNaileeL§e, §aFriraaa§e, §aKerdus§e, §aEellyciaa§e");
        commandSender.sendMessage("§6 * §cAdmin: §aVastAntelope§e, §aVurryCraft§e, §aXYNNZZZZ§e, §aUvoo§e");
        commandSender.sendMessage("§6 * §cModerator: §7-");
        commandSender.sendMessage("§6 * §cHelper: §aKanatasan523§e, §aAezteru_§e, §agloryxyz§e, §axyz_arya§e, §apyzxrell§e, §aEyzFarisSkuy§e, §aKazeXz§e, §aneuvie§e, §afrost_grizz§e, §aMutiaalyn§e, §aDaniksv§e, §aCogil§e, §aDikkSenpai§e, §aBloodCunaris§e, §azamzam09§e, §aXeonovy§e, §aKillerWolf4840§e");
        commandSender.sendMessage("§6 * §cBuilder: §aTROLLexyz§e");


        commandSender.sendMessage(Utils.rainbowify("[*]-----------------------------------------[*]"));
    }

    private boolean doesServerExists(String serverName) {
        boolean exists = false;
        try {
            try (Closer closer = new Closer()) {
                Results results = closer.add(this.helper.query("SELECT server FROM serverstats WHERE server = ?;").getResults(serverName));
                ResultSet resultSet = results.getResultSet();
                exists = resultSet.next();
            }
        } catch (Exception e) {
            // ignore
        }
        return exists;
    }

    private void registerCommands(PluginManager pluginManager) {
        // pluginManager.registerCommand(this, new GlobalChatCMD(this));
        pluginManager.registerCommand(this, new PingCMD());
        pluginManager.registerCommand(this, new StaffListCMD());
        pluginManager.registerCommand(this, new StaffChatCMD(this));
        pluginManager.registerCommand(this, new HiddenCMD(this));
        pluginManager.registerCommand(this, new SocialSpyCMD(this));
        pluginManager.registerCommand(this, new IgnoreCMD());
        pluginManager.registerCommand(this, new MessageCMD());
        pluginManager.registerCommand(this, new ReplyCMD(this));
        pluginManager.registerCommand(this, new KaCoreCMD(this));
        pluginManager.registerCommand(this, new RedisConfigCMD(this));
    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerListener(this, new ConnectionListener(this));
        pluginManager.registerListener(this, new ChatListener(this));
        pluginManager.registerListener(this, new ServerPingListener(this));
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            try (InputStream inputStream = getResourceAsStream("config.yml")) {
                Files.copy(inputStream, configFile.toPath());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        reloadConfig();
    }

    public void saveConfig() {
        try {
            provider.save(config, configFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Config.init(this);
    }

    public void reloadConfig() {
        try {
            config = provider.load(configFile);
            Config.init(this);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static KaCore getInstance() {
        return instance;
    }

    public RedisBungeeHook getRedisBungeeHook() {
        return redisBungeeHook;
    }

    public ConfigurationProvider getConfigProvider() {
        return provider;
    }

    public SQLHelper getHelper() {
        return helper;
    }

    public PrefixConfig getPrefixConfig() {
        return prefixConfig;
    }

    public TaskScheduler getScheduler() {
        return scheduler;
    }

    public Configuration getConfig() {
        return config;
    }

    public Map<String, String> getMessageData() {
        return this.messageData;
    }

    public Set<String> getSpyingPlayers() {
        return this.spyingPlayers;
    }

    public void setPlayerCount(int onlinePlayers) {
        playerCount = onlinePlayers;
        lastPlayerCount = System.currentTimeMillis();
    }

    public int getPlayerCount() {
        if (System.currentTimeMillis() - lastPlayerCount <= 5000L) {
            return playerCount;
        }

        RedisBungeeHook redisBungeeHook = getRedisBungeeHook();

        if (redisBungeeHook == null || !redisBungeeHook.isHooked()) {
            return getProxy().getOnlineCount();
        } else {
            int count = redisBungeeHook.getAPI().getPlayerCount();
            setPlayerCount(count);
            return count;
        }
    }
}