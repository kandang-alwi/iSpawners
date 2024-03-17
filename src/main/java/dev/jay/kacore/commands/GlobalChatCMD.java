package dev.jay.kacore.commands;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.redis.handlers.GlobalChatHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.TimeUnit;

public class GlobalChatCMD extends Command {
    private final KaCore plugin;
    private boolean globalChatEnabled = false;

    public GlobalChatCMD(KaCore kaCore) {
        super("globalchat", "", "gc");
        this.plugin = kaCore;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(Config.PREFIX + "§cYou need to be a player to execute this command!");
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (!commandSender.hasPermission("kacore.globalchat")) {
            commandSender.sendMessage(Config.PREFIX + Config.GLOBALCHAT_DENY);
            return;
        }
        PlayerProfile.getOrCreateProfile(player).whenComplete((profile, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            if (args.length == 0) {
                commandSender.sendMessage(Config.PREFIX + "§cUsage: /globalchat <message>");
                return;
            }
            switch (args[0].toLowerCase()) {
                case "$enable": {
                    if (this.globalChatEnabled) {
                        player.sendMessage(Config.PREFIX + "§eGlobal Chat is already §aenabled§d!");
                        return;
                    }
                    this.globalChatEnabled = true;
                    this.plugin.getProxy().broadcast(Config.PREFIX + " §aGlobal Chat has been enabled by administrator!");
                    return;
                }
                case "$disable": {
                    if (!this.globalChatEnabled) {
                        player.sendMessage(Config.PREFIX + "§eGlobal Chat is already §cdisabled§d!");
                        return;
                    }
                    this.globalChatEnabled = false;
                    this.plugin.getProxy().broadcast(Config.PREFIX + "§cGlobal Chat has been disabled by administrator!");
                    return;
                }
                default: {
                    if (!this.globalChatEnabled && !player.hasPermission("kacore.staff")) {
                        player.sendMessage(Config.PREFIX + "§cGlobal Chat is currently disabled!");
                        return;
                    }

                    if (!profile.isUseGlobalChat()) {
                        player.sendMessage(Config.PREFIX + "§cGlobal Chat is currently disabled!");
                        return;
                    }
                    Long lastGlobalChatTime = this.plugin.globalChatMap.getOrDefault(player.getUniqueId(), 0L);
                    if (lastGlobalChatTime != 0L && !player.hasPermission("kacore.staff")) {
                        long cooldownTime = player.hasPermission("karank.donate") ? 60L : 120L;
                        cooldownTime = player.hasPermission("karank.supporter") || player.hasPermission("karank.donator") ? 30L : cooldownTime;
                        long elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastGlobalChatTime);
                        if (elapsedTime < cooldownTime) {
                            player.sendMessage(Config.PREFIX + "§cPlease wait §c§l" + (cooldownTime - elapsedTime) + " §cseconds §cto global chat again!");
                            return;
                        }
                    }
                    String message = String.join(" ", args);
                    try {
                        GlobalChatHandler.sendGlobalChat(player, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Config.PREFIX + "§cAn error occurred while sending your message. Please try again later!");
                        return;
                    }
                    if (!player.hasPermission("kacore.staff")) {
                        this.plugin.globalChatMap.put(player.getUniqueId(), System.currentTimeMillis());
                    }
                    return;
                }
            }
        });
    }
}