package dev.jay.kacore.commands.config;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class RedisConfigCMD extends Command {
    private final KaCore plugin;

    public RedisConfigCMD(KaCore kaCore) {
        super("redisconfig", "", "redisset");
        this.plugin = kaCore;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("kacore.staff")) {
            sender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Config.PREFIX + "§cUsage: /redisconfig [true/false] §7- Configures the redis availability");
            return;
        }

        if (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false")) {
            sender.sendMessage(Config.PREFIX + "§cInvalid argument, only accepting 'true' or 'false'!");
            return;
        }

        boolean useRedis = Boolean.parseBoolean(args[0]);
        updateRedisConfig(useRedis, sender);
    }

    private void updateRedisConfig(boolean useRedis, CommandSender sender) {
        plugin.getConfig().set("use-redis-bungee", useRedis);
        plugin.saveConfig();
        initializeRedisBungeeHook(sender);
        sender.sendMessage(Config.PREFIX + "§aSuccessfully set redis config to " + useRedis);
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
}
