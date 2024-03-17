package dev.jay.kacore.commands.messages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.List;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.plugin.Command;


public class IgnoreCMD extends Command {
    public IgnoreCMD() {
        super("ignore");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(Config.PREFIX + "§cYou need to be a player to execute this command!");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        PlayerProfile.getOrCreateProfile(player).whenComplete((playerProfile, throwable) -> {
            if (args.length == 0) {
                player.sendMessage(Config.PREFIX + "§cUsage: /ignore [add/remove/list]");
            } else {
                String subCommand = args[0].toLowerCase();

                switch (subCommand) {
                    case "add":
                        handleIgnoreAdd(playerProfile, player, args);
                        break;
                    case "remove":
                        handleIgnoreRemove(playerProfile, player, args);
                        break;
                    case "list":
                        handleIgnoreList(playerProfile, player);
                        break;
                    default:
                        player.sendMessage(Config.PREFIX + "§cUsage: /ignore [add/remove/list] [player]");
                }
            }
        });
    }

    private void handleIgnoreAdd(PlayerProfile playerProfile, ProxiedPlayer player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Config.PREFIX + "§cUsage: /ignore add <player>");
            return;
        }

        String targetPlayer = args[1];

        if (!Utils.isValidUsername(targetPlayer)) {
            player.sendMessage(Config.PREFIX + "§cInvalid username!");
            return;
        }

        if (playerProfile.hasIgnored(targetPlayer)) {
            player.sendMessage(Config.PREFIX + "§cYou've already ignored this player before!");
            return;
        }

        try {
            playerProfile.addToIgnore(targetPlayer).join();
            player.sendMessage(Config.PREFIX + "§aAdded " + targetPlayer + " to your ignore list!");
        } catch (Exception exception) {
            player.sendMessage("§cAn error has occurred while doing this, please report this to the developer(s)!");
            exception.printStackTrace();
        }
    }

    private void handleIgnoreRemove(PlayerProfile playerProfile, ProxiedPlayer player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Config.PREFIX + "§cUsage: /ignore remove <player>");
            return;
        }

        String targetPlayer = args[1];

        if (!Utils.isValidUsername(targetPlayer)) {
            player.sendMessage(Config.PREFIX + "§cInvalid username!");
            return;
        }

        if (!playerProfile.hasIgnored(targetPlayer)) {
            player.sendMessage(Config.PREFIX + "§cYou never ignored §d" + targetPlayer + "§c!");
            return;
        }

        try {
            playerProfile.removeFromIgnore(targetPlayer).join();
            player.sendMessage(Config.PREFIX + "§aRemoved " + args[1] + " from your ignore list!");
        } catch (Exception exception) {
            player.sendMessage("§cAn error has occurred while doing this, please report this to the developer(s)!");
            exception.printStackTrace();
        }
    }

    private void handleIgnoreList(PlayerProfile playerProfile, ProxiedPlayer player) {
        List<String> ignoredPlayers = playerProfile.getIgnoredPlayers();
        int count = 0;

        player.sendMessage(Config.PREFIX + "§dIgnore list (" + ignoredPlayers.size() + "):");

        for (String ignoredPlayer : ignoredPlayers) {
            player.sendMessage("§7(§5" + ++count + "§7) §a" + ignoredPlayer);
        }
    }
}