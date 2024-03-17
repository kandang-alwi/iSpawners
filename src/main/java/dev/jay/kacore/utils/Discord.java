package dev.jay.kacore.utils;

import dev.jay.kacore.KaCore;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.awt.*;
import java.util.logging.Level;

public class Discord {
    private static final String FOOTER = "KaCore StaffChat";
    private static final String FOOTER_URL = "https://i.hypera.dev/assets/hypera-icon-white.png";
    private static boolean enabled;
    private static String hookURL;

    public static void setup() {
        Configuration config = KaCore.getInstance().getConfig();
        if (!config.getBoolean("discord-enabled"))
            return;
        if (config.getString("discord-webhook") == null || config.getString("discord-webhook").contains("XXXXXXXXXXXXXXXXXX")) {
            KaCore.getInstance().getLogger().log(Level.SEVERE, "Error: Discord messages are enabled but the webhook URL has not been configured!");
            return;
        }

        hookURL = config.getString("discord-webhook");
        enabled = true;
    }


    public static void broadcastDiscordJoin(ProxiedPlayer player) {
        if (!isEnabled())
            return;
        KaCore.getInstance().getProxy().getScheduler().runAsync(KaCore.getInstance(), () -> {
            try {
                DiscordWebhook hook = getHook();

                if (!KaCore.getInstance().getConfig().getBoolean("discord-embed")) {
                    hook.setContent(player.getName() + " joined the server.");
                    hook.execute();
                    return;
                }

                DiscordWebhook.EmbedObject embed = createEmbed();
                embed.setColor(Color.GREEN);
                embed.setDescription(player.getName() + " joined the server.");
                hook.addEmbed(embed);
                hook.execute();
            } catch (Exception e) {
                KaCore.getInstance().getLogger().log(Level.SEVERE, e.toString());
            }
        });
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static String joinLeavePlaceholders(String string, ProxiedPlayer player) {
        return escape(string.replaceAll("\\{player}", player.getName()));
    }

    private static DiscordWebhook getHook() {
        DiscordWebhook hook = new DiscordWebhook(hookURL);
        hook.setAvatarUrl(KaCore.getInstance().getConfig().getString("discord-image"));
        hook.setUsername(KaCore.getInstance().getConfig().getString("discord-username"));
        return hook;
    }

    private static DiscordWebhook.EmbedObject createEmbed() {
        return new DiscordWebhook.EmbedObject().setFooter(FOOTER, FOOTER_URL);
    }

    private static String escape(String input) {
        return input.replaceAll("\"", "\\\\\"");
    }
}