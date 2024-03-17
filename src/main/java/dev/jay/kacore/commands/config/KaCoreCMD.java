package dev.jay.kacore.commands.config;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.objects.PrefixConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class KaCoreCMD extends Command {
    private KaCore plugin;
    PrefixConfig prefixConfig;

    public KaCoreCMD(KaCore kaCore) {
        super("kacore", "", "kandangalwicore");
        this.plugin = kaCore;
        this.prefixConfig = plugin.getPrefixConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Config.PREFIX + "§7This server is running §d§lKACORE-BETA-RELEASE");
        } else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("kacore.admin")) {
            reloadConfig(sender, args);
            initializeRedisBungeeHook(sender);
        } else if (args[0].equalsIgnoreCase("help")) {
            displayHelpCommand(sender);
        } else {
            sender.sendMessage(Config.PREFIX + "§7This server is running §d§lKACORE-BETA-RELEASE");
        }
    }

    private void reloadConfig(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (args[1].equalsIgnoreCase("config")) {
                    plugin.reloadConfig();
                    sender.sendMessage(Config.PREFIX + "§aConfig has been reloaded!");
                } else if (args[1].equalsIgnoreCase("prefix")) {
                    if (prefixConfig != null) { // Check if prefixConfig is not null
                        try {
                            prefixConfig.reloadConfig();
                            sender.sendMessage(Config.PREFIX + "§aPrefix has been reloaded!");
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            sender.sendMessage("§cFailed to reload prefix config!");
                        }
                    } else {
                        sender.sendMessage("§cPrefix config is not enabled!");
                    }
                } else {
                    sender.sendMessage(Config.PREFIX + "§cInvalid argument.");
                }
            } else {
                sender.sendMessage(Config.PREFIX + "§cToo many arguments.");
            }
        }
    }

    private void initializeRedisBungeeHook(CommandSender sender) {
        RedisBungeeHook redisBungeeHook = plugin.getRedisBungeeHook();

        if (redisBungeeHook.isHooked() && !redisBungeeHook.isInitialized()) {
            try {
                redisBungeeHook.initialize();
            } catch (Exception exception) {
                exception.printStackTrace();
                sender.sendMessage("§cFailed to initialize redis bungeehook!");
            }
        }
    }

    private void displayHelpCommand(CommandSender sender) {
        sender.sendMessage("§d/kacore help §f- shows the help command");
        sender.sendMessage("§d/kacore reload §7[config/prefix] §f- reload the configurations");
    }
}