package dev.jay.kacore.redis.entries;

public class MessagePayload {
    private final String senderName;
    private final String targetName;
    private final String message;
    private final String spyMessage;
    private final boolean senderStaff;

    public MessagePayload(String senderName, String targetName, String message, String spyMessage, boolean senderStaff) {
        this.senderName = senderName;
        this.targetName = targetName;
        this.message = message;
        this.spyMessage = spyMessage;
        this.senderStaff = senderStaff;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean isSenderStaff() {
        return senderStaff;
    }

    public String getMessage() {
        return message;
    }

    public String getSpyMessage() {
        return spyMessage;
    }
}