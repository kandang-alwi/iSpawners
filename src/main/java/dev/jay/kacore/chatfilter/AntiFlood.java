package dev.jay.kacore.chatfilter;

import dev.jay.kacore.objects.Config;

public class AntiFlood {

    public boolean check(String message) {
        if (!Config.ANTI_FLOOD_ENABLED) {
            return true;
        }

        char prevChar = '\0';
        char prevPrevChar = '\0';

        for (char currentChar : message.toCharArray()) {
            if (currentChar == prevChar && prevChar == prevPrevChar) {
                return false;
            }

            prevPrevChar = prevChar;
            prevChar = currentChar;
        }

        return true;
    }

    public String parse(String message) {
        char prevChar = 'Å';
        char prevPrevChar = '¤';
        char currentChar = 'a';

        StringBuilder parsedMessage = new StringBuilder(message.length());

        for (char character : message.toCharArray()) {
            if (character != prevChar || prevChar != prevPrevChar || currentChar != prevPrevChar) {
                parsedMessage.append(character);
            }

            prevPrevChar = prevChar;
            prevChar = character;
        }

        return parsedMessage.toString();
    }
}
