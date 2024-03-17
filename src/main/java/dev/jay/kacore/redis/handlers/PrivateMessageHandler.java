package dev.jay.kacore.redis.handlers;

import com.google.common.base.Preconditions;
import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.profiles.PlayerProfile;
import dev.jay.kacore.redis.entries.MessageEntry;
import dev.jay.kacore.redis.entries.MessagePayload;
import dev.jay.kacore.redis.entries.MessagePlayerEntry;
import dev.jay.kacore.redis.entries.PlayerEntry;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PrivateMessageHandler {

    @NotNull
    public static CompletableFuture<Void> sendPrivateMessage(ProxiedPlayer sender, String targetUsername, String message, boolean reply) {
        CompletableFuture<Void> completedFuture = CompletableFuture.completedFuture(null);
        Preconditions.checkNotNull(sender);
        Preconditions.checkNotNull(targetUsername);

        String trimmedMessage = message.trim();
        if (trimmedMessage.isEmpty()) {
            sender.sendMessage(Config.PREFIX + "§cMessage cannot be empty!");
            return completedFuture;
        }

        KaCore kaCore = KaCore.getInstance();
        RedisBungeeHook redisBungeeHook = kaCore.getRedisBungeeHook();
        ProxiedPlayer targetPlayer = Utils.getPlayer(targetUsername);
        PlayerEntry targetPlayerEntry = (targetPlayer != null) ? new PlayerEntry(targetPlayer) : null;

        if (targetPlayerEntry == null || (targetPlayerEntry.isHidden() && !sender.hasPermission("kacore.staff"))) {
            if (reply) {
                sender.sendMessage(Config.PREFIX + "§cYou don't have anyone to reply to!");
            } else {
                sender.sendMessage(Config.PREFIX + "§cCannot find §d" + targetUsername + "§c!");
            }
            return completedFuture;
        }

        String coloredMessage = Utils.color(trimmedMessage);
        PlayerEntry senderEntry = new PlayerEntry(sender);
        PlayerEntry targetEntry = targetPlayerEntry;

        return CompletableFuture.runAsync(() -> {
            PlayerProfile.getProfile(sender.getUniqueId()).thenAccept(senderProfile -> {
                if (senderProfile != null && senderProfile.hasIgnored(targetUsername)) {
                    sender.sendMessage(Config.PREFIX + "§cYou can't message someone you've ignored!");
                    return;
                }

                PlayerProfile.getProfile(targetPlayer.getUniqueId()).thenAccept(targetProfile -> {
                    if (targetProfile != null && targetProfile.hasIgnored(sender.getName()) && !sender.hasPermission("kacore.staff")) {
                        return;
                    }

                    MessagePlayerEntry senderMessageEntry = new MessagePlayerEntry(senderEntry);
                    MessagePlayerEntry targetMessageEntry = new MessagePlayerEntry(targetEntry);
                    MessageEntry messageEntry = new MessageEntry(senderMessageEntry, targetMessageEntry, coloredMessage);
                    PrivateMessageHandler.handlePrivateMessage(messageEntry);
                });
            });
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public static void handlePrivateMessage(MessageEntry messageEntry) {
        KaCore kaCore = KaCore.getInstance();
        Map<String, String> messageData = kaCore.messageData;
        RedisBungeeHook redisBungeeHook = kaCore.getRedisBungeeHook();
        MessagePlayerEntry senderEntry = messageEntry.getSenderEntry();
        MessagePlayerEntry targetEntry = messageEntry.getTargetEntry();
        String formattedMessage = messageEntry.getMessageFormat()
                .replace("{sender}", String.valueOf(senderEntry.getFormattedName()))
                .replace("{target}", String.valueOf(targetEntry.getFormattedName()))
                .replace("{message}", messageEntry.getMessage());

        String spyMessage = messageEntry.getSpyMessageFormat()
                .replace("{sender}", String.valueOf(senderEntry.getFormattedName()))
                .replace("{target}", String.valueOf(targetEntry.getFormattedName()))
                .replace("{message}", messageEntry.getMessage());

        ProxiedPlayer sender = senderEntry.getPlayer();
        if (sender == null) {
            return;
        }

        ProxiedPlayer target = targetEntry.getPlayer();
        if (target != null) {
            messageData.put(sender.getName(), targetEntry.getUsername());
            messageData.put(target.getName(), senderEntry.getUsername());
            sender.sendMessage(formattedMessage);
            target.sendMessage(formattedMessage);
        } else {
            if (redisBungeeHook.isHooked()) {
                messageData.put(sender.getName(), targetEntry.getUsername());
                MessagePayload messagePayload = new MessagePayload(sender.getName(), targetEntry.getUsername(), formattedMessage, spyMessage, sender.hasPermission("kacore.staff"));
                String jsonMessage = Utils.DEFAULT_GSON.toJson(messagePayload);
                sender.sendMessage(formattedMessage);
                redisBungeeHook.sendPubSubMessage(RedisBungeeHook.Channel.PRIVATE_MESSAGE, jsonMessage);
            }
        }

        for (String spyingPlayer : kaCore.spyingPlayers) {
            ProxiedPlayer spyingProxiedPlayer = Utils.getPlayer(spyingPlayer);
            if (spyingProxiedPlayer == null) {
                kaCore.spyingPlayers.remove(spyingPlayer);
                continue;
            }
            spyingProxiedPlayer.sendMessage(spyMessage);
        }
    }
}