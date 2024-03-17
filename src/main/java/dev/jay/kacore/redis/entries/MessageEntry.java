package dev.jay.kacore.redis.entries;

public class MessageEntry {
    private final MessagePlayerEntry senderEntry;
    private final MessagePlayerEntry targetEntry;
    private final String message;
    private final String messageFormat = "§d[§r{sender} §d➨ §r{target}§d] §f{message}";
    private final String spyMessageFormat = "§7(SPY) §d[§7{sender} §d➨ §7{target}§d] §f{message}";

    public MessageEntry(MessagePlayerEntry senderEntry, MessagePlayerEntry targetEntry, String message) {
        this.senderEntry = senderEntry;
        this.targetEntry = targetEntry;
        this.message = message;
    }

    public MessagePlayerEntry getSenderEntry() {
        return senderEntry;
    }

    public MessagePlayerEntry getTargetEntry() {
        return targetEntry;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageFormat() {
        return messageFormat
                .replace("{sender}", senderEntry.getPrefix() + senderEntry.getUsername())
                .replace("{target}", targetEntry.getPrefix() + targetEntry.getUsername())
                .replace("{message}", message);
    }

    public String getSpyMessageFormat() {
        return spyMessageFormat
                .replace("{sender}", String.valueOf(senderEntry.getUsername()))
                .replace("{target}", String.valueOf(targetEntry.getUsername()))
                .replace("{message}", message);
    }
}

