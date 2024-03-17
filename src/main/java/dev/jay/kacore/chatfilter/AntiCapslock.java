package dev.jay.kacore.chatfilter;

import dev.jay.kacore.objects.Config;

import java.util.List;
import java.util.stream.Collectors;

public class AntiCapslock {

    public boolean check(String message) {
        if (!Config.ANTI_CAPSLOCK_ENABLED || message.length() <= 3) {
            return true;
        }

        double threshold = (double) Config.ANTI_CAPSLOCK_PERCENT * 0.01;
        long uppercaseCount = countUppercaseLetters(message);

        return uppercaseCount / (double) message.length() <= threshold;
    }

    public String capitalize(String message) {
        return message.substring(0, 1).toUpperCase() + message.substring(1).toLowerCase();
    }

    private long countUppercaseLetters(String message) {
        List<Character> uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        return message.chars()
                .mapToObj(c -> (char) c)
                .filter(uppercaseLetters::contains)
                .count();
    }
}
