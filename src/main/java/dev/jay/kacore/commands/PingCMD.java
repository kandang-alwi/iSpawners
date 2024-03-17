package dev.jay.kacore.commands;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PingCMD extends Command {
    public PingCMD() {
        super("ping", "", "p");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        RedisBungeeHook redisBungeeHook = KaCore.getInstance().getRedisBungeeHook();

        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(Config.PREFIX + "§cYou need to be a player to execute this command!");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        int playerPing = player.getPing();

        if (args.length == 0) {
            player.sendMessage(Config.PREFIX + "§7Your ping is " + Utils.getPingColor(playerPing) + playerPing + " ms");
        } else {
            ProxiedPlayer targetPlayer = Utils.getPlayer(args[0]);

            if (targetPlayer == null) {
                handlePingLookup(player, redisBungeeHook, args[0]);
            } else {
                int targetPing = targetPlayer.getPing();
                player.sendMessage(Config.PREFIX + "§d" + targetPlayer.getName() + "'s §7ping is " + Utils.getPingColor(targetPing) + targetPing + " ms");
            }
        }
    }

    private void handlePingLookup(ProxiedPlayer player, RedisBungeeHook redisBungeeHook, String targetPlayerName) {
        if (redisBungeeHook.isHooked()) {
            redisBungeeHook.sendPubSubMessage(RedisBungeeHook.Channel.PING, targetPlayerName);
            redisBungeeHook.getPing(targetPlayerName).whenComplete((ping, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                    return;
                }

                if (ping == -1) {
                    player.sendMessage(Config.PREFIX + "§cCannot find '§l" + targetPlayerName + "§c'!");
                } else {
                    player.sendMessage(Config.PREFIX + "§d" + targetPlayerName + "'s §7ping is " + Utils.getPingColor(ping) + ping + " ms");
                }
            });
        } else {
            player.sendMessage(Config.PREFIX + "§cCannot find '§l" + targetPlayerName + "§c'!");
        }
    }
}

