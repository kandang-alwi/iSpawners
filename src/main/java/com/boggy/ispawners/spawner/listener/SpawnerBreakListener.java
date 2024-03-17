package com.boggy.ispawners.spawner.listener;

import com.boggy.ispawners.ISpawners;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerBreakListener implements Listener {
    private final ISpawners plugin;

    public SpawnerBreakListener(ISpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled()) return;

        if (!(event.getBlock().getState() instanceof CreatureSpawner)) return;

        CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (!handItem.hasItemMeta() || !handItem.getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH)) {
            if (!event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("PICKAXE")) {
                event.setCancelled(true);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You need silk touch to pick up spawners!"));
                return;
            }
        } else {
            String actionBarMessage = plugin.getConfig().getString("break.actionbar");
            String chatMessage = plugin.getConfig().getString("break.message");

            if (!actionBarMessage.isEmpty()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', actionBarMessage.replace("{type}", spawner.getSpawnedType().toString()))));
            }
            if (!chatMessage.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage.replace("{type}", spawner.getSpawnedType().toString())));
            }
            return;
        }

        String spawnedType = spawner.getSpawnedType().toString().toLowerCase();
        spawnedType = spawnedType.substring(0, 1).toUpperCase() + spawnedType.substring(1);

        ItemStack spawnerDrop = new ItemStack(Material.SPAWNER);
        ItemMeta spawnerMeta = spawnerDrop.getItemMeta();
        spawnerMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + spawnedType + " spawner");
        spawnerMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "spawnerType"), PersistentDataType.STRING, spawnedType);
        spawnerDrop.setItemMeta(spawnerMeta);

        int stackSize = plugin.getStackSize(spawner);

        if (player.isSneaking()) {
            if ((stackSize - 64) <= 0) {
                plugin.removeSpawner(spawner);
                event.getBlock().setType(Material.AIR);
                spawnerDrop.setAmount(stackSize);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerDrop);
            } else {
                plugin.updateStackSize(spawner, -64);
                spawnerDrop.setAmount(64);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerDrop);
            }
            event.setCancelled(true);
        } else {
            if ((stackSize - 1) <= 0) {
                plugin.removeSpawner(spawner);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerDrop);
            } else {
                plugin.updateStackSize(spawner, -1);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawnerDrop);
            }
            event.setCancelled(true);
        }
    }
}