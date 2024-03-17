package dev.jay.kacore.objects;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public final class Config {
    public static String PREFIX;
    public static String CHATFILTER_PREFIX;
    public static String NO_PERMISSION;
    public static String GLOBALCHAT_DENY;
    public static List<String> INFO_MESSAGES;
    public static List<String> DEVELOPER_TEAM;
    public static String CHATFILTER_MESSAGE;
    public static boolean USE_REDIS_BUNGEE;
    public static boolean SERVER_SWITCHER_ENABLED;
    public static boolean CHATFILTER_ENABLED;
    public static boolean ANTI_CAPSLOCK_ENABLED;
    public static int ANTI_CAPSLOCK_PERCENT;
    public static boolean ANTI_FLOOD_ENABLED;
    public static boolean ANTI_BADWORD_ENABLED;
    public static String ANTI_BADWORD_REPLACER;
    public static List<String> ANTI_BADWORD_LIST;
    public static boolean ANTI_SOCIALSPY_ENABLED;
    public static List<String> ANTI_SOCIALSPY_LIST;


    public static void init(KaCore kaCore) {
        Configuration configuration = kaCore.getConfig();
        PREFIX = Utils.color(configuration.getString("prefix"));
        CHATFILTER_PREFIX = Utils.color(configuration.getString("chatfilter-prefix"));
        NO_PERMISSION = Utils.color(configuration.getString("no-permission"));
        GLOBALCHAT_DENY = Utils.color(configuration.getString("globalchat-deny"));
        CHATFILTER_MESSAGE = Utils.color(configuration.getString("messages.chatfilter-message"));
        USE_REDIS_BUNGEE = configuration.getBoolean("use-redis-bungee");
        SERVER_SWITCHER_ENABLED = configuration.getBoolean("use-serverswitcher");

        INFO_MESSAGES = colorList(configuration.getStringList("messages.info-messages"));
        DEVELOPER_TEAM = colorList(configuration.getStringList("messages.dev-team"));

        CHATFILTER_ENABLED = configuration.getBoolean("chatfilter.enabled");
        ANTI_CAPSLOCK_ENABLED = configuration.getBoolean("chatfilter.anti-capslock.enabled");
        ANTI_CAPSLOCK_PERCENT = configuration.getInt("chatfilter.anti-capslock.percent");
        ANTI_FLOOD_ENABLED = configuration.getBoolean("chatfilter.anti-flood.enabled");
        ANTI_BADWORD_ENABLED = configuration.getBoolean("chatfilter.anti-badword.enabled");
        ANTI_BADWORD_REPLACER = Utils.color(configuration.getString("chatfilter.anti-badword.replacer"));

        ANTI_BADWORD_LIST = configuration.getStringList("chatfilter.anti-badword.list");
        ANTI_BADWORD_LIST.replaceAll(String::toLowerCase);

        ANTI_SOCIALSPY_ENABLED = configuration.getBoolean("anti-socialspy.enabled");
        ANTI_SOCIALSPY_LIST = configuration.getStringList("anti-socialspy.list");

    }

    private static List<String> colorList(List<String> list) {
        List<String> coloredList = new ArrayList<>();
        for (String string : list) {
            coloredList.add(Utils.color(string));
        }
        return coloredList;
    }
}