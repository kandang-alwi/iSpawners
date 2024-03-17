package com.boggy.ispawners.gui;

import com.boggy.ispawners.ISpawners;
import com.boggy.ispawners.pdc.SpawnerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class SpawnerUI extends GUIHolder {
    private ISpawners plugin;
    Player player;
    CreatureSpawner spawner;

    public SpawnerUI(Player player, CreatureSpawner spawner, ISpawners plugin) {
        super(plugin.getStackSize(spawner) + " " +
                spawner.getSpawnedType().toString().toLowerCase().substring(0, 1).toUpperCase() +
                spawner.getSpawnedType().toString().toLowerCase().substring(1) + " " +
                ((plugin.getStackSize(spawner) > 1) ? "Spawners" : "Spawner"), 27);

        this.plugin = plugin;
        this.player = player;
        this.spawner = spawner;

        HashMap<Player, CreatureSpawner> openedSpawners = plugin.getSpawnerUITracker();
        for (Player playerSpawnerOpen : openedSpawners.keySet()) {
            if (openedSpawners.get(playerSpawnerOpen).equals(spawner)) {
                if (playerSpawnerOpen.getOpenInventory().getTitle().contains("Spawner")) {
                    return;
                }
            }
        }
        if (!plugin.getSpawners().contains(spawner)) {
            player.closeInventory();
            return;
        }

        plugin.getSpawnerUITracker().remove(player, spawner);
        plugin.getSpawnerUITracker().put(player, spawner);

        int stackSize = plugin.getStackSize(spawner);
        String spawnerType = spawner.getSpawnedType().toString().toLowerCase();
        String spawnerText = (stackSize > 1) ? "Spawners" : "Spawner";
        String items = "0";

        if (plugin.getDrops(spawner) != null) {
            items = String.valueOf(plugin.getDrops(spawner).size());
        }


        ItemStack drops = new ItemStack(Material.CHEST);
        ItemMeta dropsMeta = drops.getItemMeta();
        assert dropsMeta != null;
        dropsMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#ffaa00") + "" + ChatColor.BOLD + "SPAWNER STORAGE");
        dropsMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#ffaa00") + items + ChatColor.WHITE + " Items"));
        dropsMeta.setLocalizedName("spawnerUI");
        drops.setItemMeta(dropsMeta);
        inventory.setItem(12, drops);

        ItemStack xp = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta xpMeta = xp.getItemMeta();
        assert xpMeta != null;
        xpMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of("#36ffa4") + "" + ChatColor.BOLD + "COLLECT XP");
        xpMeta.setLore(List.of(net.md_5.bungee.api.ChatColor.of("#36ffa4") + String.valueOf(plugin.getXP(spawner)) + ChatColor.WHITE + " XP Points"));
        xpMeta.setLocalizedName("spawnerUI");
        xp.setItemMeta(xpMeta);
        inventory.setItem(14, xp);

        player.openInventory(inventory);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        CreatureSpawner spawner = plugin.getSpawnerUITracker().get(player);

        if (item == null || item.getType().equals(Material.AIR)) {
            return;
        }

        if (item.getType().equals(Material.CHEST)) {
            player.closeInventory();
            new DropsUI(player, spawner, plugin, 1);
        } else if (item.getType().equals(Material.EXPERIENCE_BOTTLE)) {
            SpawnerUtils.handleXP(player, spawner, item, plugin);
        } else if (item.getType().equals(Material.GOLD_INGOT)) {
            SpawnerUtils.handleSell(player, spawner, true, plugin);
            new DropsUI(player, spawner, plugin, 1);
        }
    }
}