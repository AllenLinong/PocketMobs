package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GetCmd
{
    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (!(sender instanceof Player player))
            return true;

        if (!player.hasPermission(Constants.Permissions.ADMIN_GET))
        {
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + Constants.Permissions.ADMIN_GET);
            return true;
        }

        if (args.length <= 1)
        {
            player.sendMessage(Settings.lang.getColored("wrong-command-usage") + ChatColor.AQUA + "/pocketmob get <ball>");
            return true;
        }

        if (!CommandUtils.validateBallName(player, args[1]))
            return true;

        int amount = args.length == 3 ? CommandUtils.parseAmount(player, args[2]) : 1;

        String itemName = CommandUtils.giveBalls(player, args[1], amount);

        player.sendMessage(Settings.lang.getColored("obtained")
                .replace("{amount}", String.valueOf(amount))
                .replace("{item}", itemName));
        return true;
    }
}
