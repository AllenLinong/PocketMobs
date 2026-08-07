package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DebugCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (player.hasPermission(Constants.Permissions.ADMIN_DEBUG))
        {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem == null || heldItem.getType() == Material.AIR)
            {
                player.sendMessage(ChatColor.YELLOW + "Hold an item to inspect its NBT data.");
                return true;
            }

            Bukkit.getConsoleSender().sendMessage(heldItem.toString());
            player.sendMessage(ChatColor.GREEN + "NBT data has been printed to the console.");
        }
        else
        {
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + Constants.Permissions.ADMIN_DEBUG);
        }
        return true;
    }
}
