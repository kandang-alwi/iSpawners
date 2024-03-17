package dev.jay.kacore.commands.messages;

import dev.jay.kacore.redis.handlers.PrivateMessageHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.Map;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.plugin.Command;

public class ReplyCMD extends Command {

    private final KaCore plugin;

    public ReplyCMD(KaCore kaCore) {
        super("reply", "", "r");
        this.plugin = kaCore;
    }

    @Override
    public void execute(CommandSender commandSender, String[] stringArray) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(Config.PREFIX + "§cYou need to be a player to execute this command!");
            return;
        }

        ProxiedPlayer sender = (ProxiedPlayer) commandSender;
        Map<String, String> messageData = plugin.getMessageData();

        if (stringArray.length == 0) {
            sender.sendMessage(Config.PREFIX + "§cUsage: /reply <message>");
        } else {
            String recipient = messageData.get(sender.getName());

            if (recipient == null) {
                sender.sendMessage(Config.PREFIX + "§cYou don't have anyone to reply to!");
                return;
            }

            String message = Utils.color(String.join(" ", stringArray));
            PrivateMessageHandler.sendPrivateMessage(sender, recipient, message, true)
                    .exceptionally(throwable -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                        return null;
                    });
        }
    }
}