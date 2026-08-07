package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter
{
    private static final List<String> SUBCOMMANDS = Arrays.asList("get", "give", "debug", "recipes", "reload");

    private final Main plugin;
    GetCmd getCommand = new GetCmd();
    GiveCmd giveCommand = new GiveCmd();
    DebugCmd debugCommand = new DebugCmd();
    RecipeCmd recipeCommand = new RecipeCmd();
    ReloadCmd reloadCmd = new ReloadCmd();

    public MainCommand(Main plugin)
    {
        this.plugin = plugin;
        if (plugin.getCommand("pocketmob") != null)
        {
            plugin.getCommand("pocketmob").setExecutor(this);
            plugin.getCommand("pocketmob").setTabCompleter(this);
        }
    }

    public void cleanup()
    {
        recipeCommand.cleanup();
        if (plugin.getCommand("pocketmob") != null)
        {
            plugin.getCommand("pocketmob").setExecutor(null);
            plugin.getCommand("pocketmob").setTabCompleter(null);
        }
    }

    private List<String> getAvailableSubcommands(CommandSender sender)
    {
        List<String> commands = new ArrayList<>();
        if (sender.hasPermission(Constants.Permissions.ADMIN_GET))
        {
            commands.add("get");
        }
        if (sender.hasPermission(Constants.Permissions.ADMIN_GIVE))
        {
            commands.add("give");
        }
        if (sender.hasPermission(Constants.Permissions.ADMIN_DEBUG))
        {
            commands.add("debug");
        }
        if (sender.hasPermission(Constants.Permissions.USER_RECIPES))
        {
            commands.add("recipes");
        }
        if (sender.hasPermission(Constants.Permissions.ADMIN_RELOAD))
        {
            commands.add("reload");
        }
        return commands;
    }

    private List<String> getAvailableSubcommands(CommandSender sender, String partialWord)
    {
        List<String> list = new ArrayList<>();
        String normalized = partialWord == null ? "" : partialWord.toLowerCase();
        for (String subcommand : getAvailableSubcommands(sender))
        {
            if (subcommand.startsWith(normalized))
            {
                list.add(subcommand);
            }
        }
        return list;
    }

    private void sendUsage(CommandSender sender)
    {
        List<String> commands = getAvailableSubcommands(sender);
        if (commands.isEmpty())
        {
            sender.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + "pocketmob");
            return;
        }
        sender.sendMessage(Settings.lang.getColored("wrong-command-usage") + ChatColor.AQUA + " /pocketmob <" + String.join("|", commands) + ">");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (plugin.ballsManager == null)
        {
            sender.sendMessage(ChatColor.RED + "PocketMobs is still loading.");
            return true;
        }

        if (args.length == 0)
        {
            sendUsage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("get"))
        {
            getCommand.handle(sender, command, label, args);
            return true;
        }
        else if (args[0].equalsIgnoreCase("give"))
        {
            giveCommand.handle(sender, command, label, args);
            return true;
        }
        else if (args[0].equalsIgnoreCase("debug"))
        {
            debugCommand.handle(sender, command, label, args);
            return true;
        }
        else if (args[0].equalsIgnoreCase("recipes"))
        {
            recipeCommand.handle(sender, command, label, args);
            return true;
        }
        else if (args[0].equalsIgnoreCase("reload"))
        {
            reloadCmd.handle(sender, command, label, args);
            return true;
        }

        sendUsage(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (!plugin.isEnabled() || plugin.ballsManager == null)
        {
            return new ArrayList<>();
        }

        if (args.length <= 1)
        {
            String partialWorld = "";
            if (args.length > 0)
                partialWorld = args[0];
            return getAvailableSubcommands(sender, partialWorld);
        }

        if (args[0].equalsIgnoreCase("get"))
        {
            if (!sender.hasPermission(Constants.Permissions.ADMIN_GET))
            {
                return new ArrayList<>();
            }

            if (args.length > 2)
            {
                return Arrays.asList("1", "5", "15", "16", "32", "64");
            }
            if (args.length == 2)
            {
                List<String> names = plugin.ballsManager.getKeys(args[1]);
                if (!names.isEmpty())
                    return names;
                return Arrays.asList(ChatColor.RED + Settings.lang.getStripped("item-not-found").replace("{item}", ""));
            }
            return new ArrayList<>();
        }
        else if (args[0].equalsIgnoreCase("give"))
        {
            if (!sender.hasPermission(Constants.Permissions.ADMIN_GIVE))
            {
                return new ArrayList<>();
            }

            if (args.length == 2)
            {
                List<String> players = new ArrayList<>();
                for (Player online : Bukkit.getServer().getOnlinePlayers())
                {
                    players.add(online.getName());
                }
                return players;
            }
            else if (args.length == 3)
            {
                List<String> names = plugin.ballsManager.getKeys(args[2]);
                if (!names.isEmpty())
                    return names;
                else
                    return Arrays.asList(ChatColor.RED + Settings.lang.getStripped("item-not-found").replace("{item}", ""));
            }
            else if (args.length == 4)
            {
                return Arrays.asList("1", "5", "15", "16", "32", "64");
            }
        }

        return new ArrayList<>();
    }
}
