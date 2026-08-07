package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!sender.hasPermission(Constants.Permissions.ADMIN_GIVE))
        {
            sender.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + Constants.Permissions.ADMIN_GIVE);
            return true;
        }

        if (args.length <= 2)
        {
            sender.sendMessage(Settings.lang.getColored("wrong-command-usage") + ChatColor.AQUA + "/pocketmob give <player> <ball>");
            return true;
        }

        if (!CommandUtils.validateBallName(sender, args[2]))
            return true;

        int amount = args.length == 4 ? CommandUtils.parseAmount(sender, args[3]) : 1;

        Player playerToGive = Bukkit.getPlayer(args[1]);
        if (playerToGive == null)
        {
            sender.sendMessage(Settings.lang.getColored("offline-player"));
            return true;
        }

        String itemName = CommandUtils.giveBalls(playerToGive, args[2], amount);

        sender.sendMessage(Settings.lang.getColored("given-item")
                .replace("{item}", itemName)
                .replace("{player}", playerToGive.getName()));

        playerToGive.sendMessage(Settings.lang.getColored("obtained")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", itemName));
        return true;
    }
}
