package com.boggy.ispawners.gui;

import com.boggy.ispawners.ISpawners;
import com.boggy.ispawners.pdc.SpawnerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SellOptionsUI extends GUIHolder {
    private final ISpawners plugin;
    private final CreatureSpawner spawner;
    private int currentPage;

    public SellOptionsUI(Player player, CreatureSpawner spawner, ISpawners plugin) {
        super("Sell Options", 27);
        this.plugin = plugin;
        this.spawner = spawner;
        this.currentPage = currentPage;

        populateSellOptions(player);
        player.openInventory(inventory);
    }

    private void populateSellOptions(Player player) {
        Inventory sellOptions = inventory;

        ItemStack sellOneItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta sellOneMeta = sellOneItem.getItemMeta();
        sellOneMeta.setDisplayName(ChatColor.GREEN + "Sell Item");
        sellOneItem.setItemMeta(sellOneMeta);
        sellOptions.setItem(13, sellOneItem);

        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm");
        confirm.setItemMeta(confirmMeta);
        sellOptions.setItem(19, confirm);

        ItemStack increaseOne = createItemStack(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+1", ChatColor.GRAY + "Click to increase by 1");
        ItemStack increaseTen = createItemStack(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+10", ChatColor.GRAY + "Click to increase by 10");
        ItemStack increaseMax = createItemStack(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "+64", ChatColor.GRAY + "Click to increase to max stack");

        sellOptions.setItem(15, increaseOne);
        sellOptions.setItem(16, increaseTen);
        sellOptions.setItem(17, increaseMax);

        ItemStack decreaseOne = createItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-1", ChatColor.GRAY + "Click to decrease by 1");
        ItemStack decreaseTen = createItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-10", ChatColor.GRAY + "Click to decrease by 10");
        ItemStack decreaseMax = createItemStack(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "-64", ChatColor.GRAY + "Click to decrease to 0");

        sellOptions.setItem(9, decreaseOne);
        sellOptions.setItem(10, decreaseTen);
        sellOptions.setItem(11, decreaseMax);

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "BACK");
        backButton.setItemMeta(backMeta);
        sellOptions.setItem(18, backButton);
    }

    private ItemStack createItemStack(Material material, String displayName, String... lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList(lore));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (clickedItem.getType() == Material.IRON_INGOT) {
            SpawnerUtils.handleSell(player, spawner, false, plugin);
        } else if (clickedItem.getType() == Material.LIME_DYE) {
            // Proses konfirmasi
        } else if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            // Proses penambahan item
        } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            // Proses pengurangan item
        } else if (clickedItem.getType() == Material.ARROW) {
            player.closeInventory();
            new DropsUI(player, spawner, plugin, 1);
        }
    }
}