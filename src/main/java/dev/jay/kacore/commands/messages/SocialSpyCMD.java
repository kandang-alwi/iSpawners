package dev.jay.kacore.commands.messages;

import net.md_5.bungee.api.CommandSender;
import java.util.Set;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.KaCore;
import net.md_5.bungee.api.plugin.Command;

public class SocialSpyCMD extends Command {

    private final KaCore plugin;

    public SocialSpyCMD(KaCore kaCore) {
        super("socialspy", "", "spymessage", "spymsg", "spypm", "spy", "spysocial");
        this.plugin = kaCore;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("kacore.socialspy")) {
            commandSender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
            return;
        }

        if (Config.ANTI_SOCIALSPY_ENABLED && Config.ANTI_SOCIALSPY_LIST.contains(commandSender.getName())) {
            return;
        }

        Set<String> spyingPlayers = plugin.getSpyingPlayers();

        if (args.length == 0) {
            if (spyingPlayers.contains(commandSender.getName())) {
                spyingPlayers.remove(commandSender.getName());
                commandSender.sendMessage(Config.PREFIX + "§cYou have disabled social-spy!");
            } else {
                spyingPlayers.add(commandSender.getName());
                commandSender.sendMessage(Config.PREFIX + "§aYou have enabled social-spy!");
            }
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!commandSender.hasPermission("kacore.socialspy.list")) {
                commandSender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
                return;
            }

            commandSender.sendMessage("§dOnline Social Spy (" + spyingPlayers.size() + "):");
            for (String spy : spyingPlayers) {
                commandSender.sendMessage("");
                commandSender.sendMessage("§5* §7" + spy);
            }
            return;
        }

        commandSender.sendMessage(Config.PREFIX + "§cInvalid usage. §dTry /socialspy [list]");
    }
}