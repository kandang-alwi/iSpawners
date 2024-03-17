package com.boggy.ispawners.pdc;

import com.boggy.ispawners.ISpawners;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SpawnerUtils {

    public static int getPrice(ISpawners plugin, CreatureSpawner spawner) {
        ArrayList<Material> drops = plugin.getDrops(spawner);

        if (drops == null || drops.isEmpty()) {
            return 0;
        } else {
            int total = 0;
            for (Material drop : drops) {
                if (plugin.getConfig().getConfigurationSection("prices").contains(drop.name())) {
                    total += plugin.getConfig().getConfigurationSection("prices").getInt(drop.name());
                }
            }
            return total;
        }
    }

    public static double getFillPercent(ISpawners plugin, CreatureSpawner spawner) {
        double maxDrops = getMaxDrops(plugin, spawner);
        double currentDrops = 0;
        if (plugin.getDrops(spawner) != null) {
            currentDrops = plugin.getDrops(spawner).size();
        }
        double percentage = (currentDrops / maxDrops) * 100;
        percentage = ((double) ((int) (percentage * 100.0))) / 100.0;
        return percentage;
    }

    public static int getMaxDrops(ISpawners plugin, CreatureSpawner spawner) {
        int maxDrops = plugin.getConfig().getInt("max_items");
        int stackSize = plugin.getStackSize(spawner);
        double multiplier = plugin.getConfig().getDouble("max_items_multiplier");
        double finalMaxDrops = 0;
        for (int i = 0; i < stackSize; i++) {
            finalMaxDrops += maxDrops * multiplier;
        }
        return (int) finalMaxDrops;
    }

    public static String getType(ISpawners plugin, ItemStack im) {
        return im.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "spawnerType"), PersistentDataType.STRING);
    }

    public static String formatCurrency(double money) {
        NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        return fmt.format(money);
    }

    public static void handleXP(Player player, CreatureSpawner spawner, ItemStack item, ISpawners plugin) {
        if (!plugin.getSpawners().contains(spawner)) {
            player.closeInventory();
            return;
        }
        int xp = plugin.getXP(spawner);
        plugin.setXP(spawner, 0);
        player.giveExp(xp / 20);
        ItemMeta xpMeta = item.getItemMeta();
        xpMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#36ffa4") + "" + ChatColor.BOLD + "COLLECT XP");
        xpMeta.setLore(Arrays.asList(net.md_5.bungee.api.ChatColor.of("#36ffa4") + "0" + ChatColor.WHITE + " XP Points"));
        item.setItemMeta(xpMeta);
        if (xp > 0) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("notifications.sound_collect-exp")), 1, 1);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
        }
    }

    public static void handleSell(Player player, CreatureSpawner spawner, Boolean sound, ISpawners plugin) {
        if (!plugin.getSpawners().contains(spawner)) {
            player.closeInventory();
            return;
        }
        int price = SpawnerUtils.getPrice(plugin, spawner);
        if (price > 0) {
            plugin.clearDrops(plugin, spawner);
            ISpawners.getEcon().depositPlayer(player, price);

            // Send sell success message to player
            String successMessage = plugin.getConfig().getString("notifications.sell_success").replace("{price}", SpawnerUtils.formatCurrency(price));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', successMessage)));

            // Send custom sell message to player
            String customMessage = plugin.getConfig().getString("notifications.sell_message").replace("{price}", SpawnerUtils.formatCurrency(price));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix + customMessage));

            if (sound) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("notifications.sound_sell")), 1, 1);
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }
            }
        }
    }
}