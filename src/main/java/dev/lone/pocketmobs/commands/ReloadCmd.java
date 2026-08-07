package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.utils.Sched;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReloadCmd
{
    private static final AtomicBoolean RELOADING = new AtomicBoolean(false);

    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (!player.hasPermission(Constants.Permissions.ADMIN_RELOAD))
            {
                player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + Constants.Permissions.ADMIN_RELOAD);
                return true;
            }
        }

        if (!RELOADING.compareAndSet(false, true))
        {
            sender.sendMessage(ChatColor.YELLOW + "PocketMobs is already reloading.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "PocketMobs reload started...");
        Sched.runGlobal(() -> {
            try
            {
                if (!Settings.reload())
                {
                    sender.sendMessage(ChatColor.RED + "PocketMobs reload failed. Check the server logs for details.");
                    return;
                }

                Main.inst.ballsManager.reload();
                sender.sendMessage(Settings.lang.getColored("reloaded"));
            }
            catch (Exception e)
            {
                sender.sendMessage(ChatColor.RED + "PocketMobs reload failed: " + e.getMessage());
                Main.inst.getLogger().severe("Failed to reload PocketMobs: " + e.getMessage());
            }
            finally
            {
                RELOADING.set(false);
            }
        });
        return true;
    }
}
