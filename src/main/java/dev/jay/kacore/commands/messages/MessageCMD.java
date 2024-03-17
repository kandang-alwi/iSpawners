package dev.jay.kacore.commands.messages;

import dev.jay.kacore.redis.handlers.PrivateMessageHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.plugin.Command;

public class MessageCMD extends Command {

    public MessageCMD() {
        super("message", "", "m", "msg", "pm", "w", "whisper");
    }

    @Override
    public void execute(CommandSender commandSender, String[] stringArray) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(Config.PREFIX + "§cYou need to be a player to execute this command!");
            return;
        }

        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if (stringArray.length < 2) {
            sender.sendMessage(Config.PREFIX + "§cUsage: /message <player> <message>");
        } else {
            List<String> messageList = new ArrayList<>(Arrays.asList(stringArray).subList(1, stringArray.length));
            String message = Utils.color(String.join(" ", messageList));

            PrivateMessageHandler.sendPrivateMessage(sender, stringArray[0], message, false)
                    .exceptionally(throwable -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                        return null;
                    });
        }
    }
}