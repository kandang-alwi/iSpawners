package dev.jay.kacore.commands;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.redis.handlers.StaffChatHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import java.util.Set;

public class StaffChatCMD extends Command {
    private final KaCore plugin;

    public StaffChatCMD(KaCore plugin) {
        super("staffchat", "", "sc");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] stringArray) {
        if (!commandSender.hasPermission("kacore.staff")) {
            commandSender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
            return;
        }

        Set<String> disabledStaffChat = this.plugin.disabledStaffChat;
        Set<String> toggledStaffChat = this.plugin.toggledStaffChat;
        String playerName = commandSender.getName();

        if (stringArray.length == 0 || stringArray[0].equalsIgnoreCase("$help")) {
            commandSender.sendMessage(Config.PREFIX + "§7Usage: §d/staffchat [$disable/$enable]");
            return;
        }

        switch (stringArray[0].toLowerCase()) {
            case "$enable":
                toggledStaffChat.add(playerName);
                disabledStaffChat.remove(playerName);
                commandSender.sendMessage(Config.PREFIX + "§aYou've enabled staff chat!");
                return;
            case "$disable":
                toggledStaffChat.remove(playerName);
                disabledStaffChat.add(playerName);
                commandSender.sendMessage(Config.PREFIX + "§cYou've disabled staff chat!");
                return;
            case "$help":
                commandSender.sendMessage(Config.PREFIX + "§7Usage:");
                commandSender.sendMessage("§5* §d/staffchat [$enable/$disable]");
                commandSender.sendMessage("§5d* §d/staffchat <message>");
                return;
        }

        if (disabledStaffChat.contains(playerName)) {
            commandSender.sendMessage(Config.PREFIX + "§cYou've disabled staff chat!");
            return;
        }

        String message = String.join(" ", stringArray);
        StaffChatHandler.sendStaffChat(commandSender, message);
    }
}