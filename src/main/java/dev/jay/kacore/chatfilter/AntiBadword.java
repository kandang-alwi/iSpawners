package dev.jay.kacore.chatfilter;

import dev.jay.kacore.objects.Config;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiBadword {

    public boolean check(String message) {
        if (!Config.ANTI_BADWORD_ENABLED) {
            return true;
        }

        for (String badWord : Config.ANTI_BADWORD_LIST) {
            Pattern pattern = Pattern.compile(badWord, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return false;
            }
        }

        return true;
    }

    public String censor(String message, ProxiedPlayer player) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] words = message.split(" ");

        for (String word : words) {
            for (String badWord : Config.ANTI_BADWORD_LIST) {
                Pattern pattern = Pattern.compile(badWord, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(word);
                if (matcher.find()) {
                    String replacement = player.hasPermission("kacore.staff")
                            ? matcher.replaceAll(Config.ANTI_BADWORD_REPLACER + "§c")
                            : (player.hasPermission("karank.donate")
                            ? matcher.replaceAll(Config.ANTI_BADWORD_REPLACER + "§a")
                            : matcher.replaceAll(Config.ANTI_BADWORD_REPLACER + "§7"));

                    word = replacement;
                }
            }

            stringBuilder.append(word).append(" ");
        }

        return stringBuilder.toString().trim();
    }
}

