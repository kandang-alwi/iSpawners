package com.boggy.ispawners.gui;

import com.boggy.ispawners.ISpawners;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DropsUI extends GUIHolder {
    ISpawners plugin;
    final int BACK_BUTTON_SLOT = 48;
    final int SELL_BUTTON_SLOT = 49;
    final int FORWARDS_BUTTON_SLOT = 50;
    private final int currentPage;

    public DropsUI(Player player, CreatureSpawner spawner, ISpawners plugin, int page) {
        super(plugin.getStackSize(spawner) + " " +
                spawner.getSpawnedType().toString().toLowerCase().substring(0, 1).toUpperCase() +
                spawner.getSpawnedType().toString().toLowerCase().substring(1) + " " +
                ((plugin.getStackSize(spawner) > 1) ? "Spawners" : "Spawner"), 54);

        this.plugin = plugin;
        this.currentPage = page;

        if (!plugin.getSpawners().contains(spawner)) {
            player.closeInventory();
            return;
        }

        ArrayList<Material> materials;
        List<ItemStack> allItems = new ArrayList<>();
        materials = plugin.getDrops(spawner);
        if (materials != null) {
            for (Material material : materials) {
                if (material != null) {
                    allItems.add(new ItemStack(material));
                }
            }
        }

        ItemStack left;
        if (page > 1) {
            left = getPageItem(true, "BACKWARDS");
        } else {
            left = getPageItem(false, "BACK");
        }
        ItemMeta leftMeta = left.getItemMeta();
        leftMeta.setLocalizedName(page + "");
        left.setItemMeta(leftMeta);
        inventory.setItem(BACK_BUTTON_SLOT, left);

        ItemStack right = getPageItem(PageUtil.isPageValid(allItems, page + 1, 45 * 64), "FORWARDS");
        ItemMeta rightMeta = right.getItemMeta();
        rightMeta.setLocalizedName(page + "");
        right.setItemMeta(rightMeta);
        inventory.setItem(FORWARDS_BUTTON_SLOT, right);

        if (!allItems.isEmpty() && page == 1) {
            ItemStack sell = new ItemStack(Material.GOLD_INGOT);
            ItemMeta sellMeta = sell.getItemMeta();
            sellMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "SELL");
            sell.setItemMeta(sellMeta);
            inventory.setItem(SELL_BUTTON_SLOT, sell);
        }

        for (ItemStack is : PageUtil.getPageItems(allItems, page, 45 * 64)) {
            inventory.addItem(is);
        }

        player.openInventory(inventory);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        CreatureSpawner spawner = plugin.getSpawnerUITracker().get(player);

        if (e.getRawSlot() == BACK_BUTTON_SLOT) {
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem != null && clickedItem.getType().equals(Material.ARROW)) {
                if (currentPage > 1) {
                    player.closeInventory();
                    new DropsUI(player, spawner, plugin, currentPage - 1).open(player);
                } else {
                    player.closeInventory();
                    new SpawnerUI(player, spawner, plugin).open(player);
                }
            }
        } else if (e.getRawSlot() == FORWARDS_BUTTON_SLOT) {
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem != null && clickedItem.getType().equals(Material.ARROW)) {
                int page = Integer.parseInt(clickedItem.getItemMeta().getLocalizedName());
                new DropsUI(player, spawner, plugin, page + 1).open(player);
            }
        } else if (e.getRawSlot() == SELL_BUTTON_SLOT) {
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem != null && clickedItem.getType().equals(Material.GOLD_INGOT)) {
                new SellOptionsUI(player, spawner, plugin).open(player);
            }
        }
    }

    private ItemStack getPageItem(boolean pageExists, String text) {
        ItemStack itemStack = new ItemStack(Material.ARROW);

        ItemMeta meta = itemStack.getItemMeta();
        ChatColor color = ChatColor.RED;
        meta.setDisplayName(color.toString() + ChatColor.BOLD + text);

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}