package dev.lone.pocketmobs.utils;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InvUtil
{
    public static boolean hasClickedTop(InventoryClickEvent event)
    {
        return event.getRawSlot() < event.getView().getTopInventory().getSize();
    }

    public static void setItemStackLore(ItemStack item, List<String> lore)
    {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static void decrementAmountMainHand(Player player)
    {
        if (player == null)
        {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR || mainHand.getAmount() <= 0)
        {
            return;
        }

        mainHand.setAmount(mainHand.getAmount() - 1);
    }

    public static void giveItem(Player player, ItemStack itemStack)
    {
        if (player == null || itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0)
        {
            return;
        }

        player.getInventory().addItem(itemStack).forEach((index, overflow) -> {
            if (overflow == null || overflow.getType() == Material.AIR || overflow.getAmount() <= 0)
            {
                return;
            }

            Item item = player.getWorld().dropItem(player.getLocation(), overflow);
            try
            {
                item.setOwner(player.getUniqueId());
            }
            catch (NoSuchMethodError e)
            {
            }
            item.setPickupDelay(0);
        });
    }

    public static void createDisplay(ItemStack itemStack, Inventory inventory, int slot, String name, String lore)
    {
        if (itemStack == null || inventory == null)
        {
            return;
        }

        ItemStack item = itemStack.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
        {
            inventory.setItem(slot, item);
            return;
        }

        meta.setDisplayName(name);
        ArrayList<String> loreLines = new ArrayList<>();
        loreLines.add(lore);
        meta.setLore(loreLines);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
}
