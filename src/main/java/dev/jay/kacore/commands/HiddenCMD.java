package dev.jay.kacore.commands;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.objects.Config;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HiddenCMD extends Command {
    private final KaCore plugin;

    public HiddenCMD(KaCore kaCore) {
        super("hidden", "");
        this.plugin = kaCore;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("kacore.hidden")) {
            commandSender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
            return;
        }

        Set<String> hiddenPlayers = this.plugin.hiddenPlayers;

        if (args.length == 0) {
            if (!(commandSender instanceof ProxiedPlayer)) {
                commandSender.sendMessage(Config.PREFIX + "§cYou need to be a player to execute this command!");
                return;
            }

            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            toggleHiddenMode(player, hiddenPlayers);
        } else {
            if (!commandSender.hasPermission("kacore.staff")) {
                commandSender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
                return;
            }

            showHiddenPlayersList(commandSender, hiddenPlayers);
        }
    }

    private void toggleHiddenMode(ProxiedPlayer player, Set<String> hiddenPlayers) {
        if (hiddenPlayers.contains(player.getName())) {
            hiddenPlayers.remove(player.getName());
            player.sendMessage(Config.PREFIX + "§cYou have disabled the hidden mode!");
        } else {
            hiddenPlayers.add(player.getName());
            player.sendMessage(Config.PREFIX + "§aYou have enabled the hidden mode!");
        }
    }

    private void showHiddenPlayersList(CommandSender commandSender, Set<String> hiddenPlayers) {
        List<String> hiddenList = new ArrayList<>(hiddenPlayers);

        if (hiddenList.isEmpty()) {
            commandSender.sendMessage(Config.PREFIX + "§bNo players are currently hidden.");
        } else {
            commandSender.sendMessage(Config.PREFIX + "§bHidden players (§a" + hiddenList.size() + "§b):");

            int index = 1;
            for (String hiddenPlayer : hiddenList) {
                commandSender.sendMessage("§d[" + index++ + "] " + hiddenPlayer);
            }
        }
    }
}
