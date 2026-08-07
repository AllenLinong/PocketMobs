package dev.lone.pocketmobs.commands;

import dev.lone.pocketmobs.Main;
import dev.lone.pocketmobs.Settings;
import dev.lone.pocketmobs.Constants;
import dev.lone.pocketmobs.data.Ball;
import dev.lone.pocketmobs.utils.ActionBar;
import dev.lone.pocketmobs.utils.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeCmd implements Listener
{
    ConcurrentHashMap<UUID, Ball> buying = new ConcurrentHashMap<>();
    private final Set<UUID> openingConfirmGui = ConcurrentHashMap.newKeySet();

    public RecipeCmd()
    {
        Bukkit.getPluginManager().registerEvents(this, Main.inst);
    }

    public void cleanup()
    {
        HandlerList.unregisterAll(this);
        buying.clear();
        openingConfirmGui.clear();
    }

    public boolean handle(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        if (player.hasPermission(Constants.Permissions.USER_RECIPES))
            showRecipesView(player);
        else
            player.sendMessage(Settings.lang.getColored("no-permission") + ChatColor.WHITE + Constants.Permissions.USER_RECIPES);

        return true;
    }

    public static void showRecipesView(Player player)
    {
        Inventory gui = Bukkit.createInventory(null, 9*6, Settings.lang.getColored("balls"));
        for(Ball entry : Main.inst.ballsManager.balls)
            gui.addItem(entry.getItemStack());
        player.openInventory(gui);
    }

    public static void showConfirmGUI(Player player)
    {
        Inventory gui = Bukkit.createInventory(null, 9*6, Settings.lang.getColored("confirm-buy"));

        InvUtil.createDisplay(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), gui, 3 + 9 * 2,
                                 Settings.lang.getColored("confirm"), "");

        InvUtil.createDisplay(new ItemStack(Material.RED_STAINED_GLASS_PANE), gui, 5 + 9 * 2,
                                 Settings.lang.getColored("cancel"), "");

        player.openInventory(gui);
    }

    private static double getBalance(Player player)
    {
        Economy economy = (Economy) Main.economy;
        if (economy == null)
        {
            if (Settings.debug)
                Main.inst.getLogger().warning("Economy provider is unavailable, cannot query balance.");
            return -1;
        }
        return economy.getBalance(player);
    }

    private static boolean withdrawPlayer(Player player, double amount)
    {
        Economy economy = (Economy) Main.economy;
        if (economy == null)
        {
            if (Settings.debug)
                Main.inst.getLogger().warning("Economy provider is unavailable, cannot withdraw money.");
            return false;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    private static boolean depositPlayer(Player player, double amount)
    {
        Economy economy = (Economy) Main.economy;
        if (economy == null)
            return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    private static boolean canFitItem(Player player, ItemStack itemStack)
    {
        Inventory simulatedInventory = Bukkit.createInventory(null, player.getInventory().getSize());
        simulatedInventory.setContents(player.getInventory().getStorageContents().clone());
        return simulatedInventory.addItem(itemStack.clone()).isEmpty();
    }

    @EventHandler
    public void recipeGUIClick(InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CHEST)
            return;
        if (!e.getView().getTitle().equalsIgnoreCase(Settings.lang.getColored("balls")))
            return;
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        e.setCancelled(true);

        if(!InvUtil.hasClickedTop(e))
            return;

        Player player = (Player)e.getWhoClicked();
        Inventory recipeGUI = Bukkit.createInventory(null, 9*6, Settings.lang.getColored("recipe"));
        Ball ballConfig = Main.inst.ballsManager.byItemStack(e.getCurrentItem());
        if (ballConfig == null)
        {
            player.closeInventory();
            return;
        }

        for(int i=0;i<recipeGUI.getSize();i++)
            InvUtil.createDisplay(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE), recipeGUI, i,
                                     " ", "");

        List<ItemStack> items = Main.inst.ballsManager.getRecipeItems(e.getCurrentItem());
        if(items == null)
        {
            ItemStack black = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            InvUtil.createDisplay(black, recipeGUI, 1 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            InvUtil.createDisplay(black, recipeGUI, 2 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            InvUtil.createDisplay(black, recipeGUI, 3 + 9 * 1, Settings.lang.getColored("no-recipe"), "");

            InvUtil.createDisplay(black, recipeGUI, 10 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            InvUtil.createDisplay(black, recipeGUI, 11 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            InvUtil.createDisplay(black, recipeGUI, 12 + 9 * 1, Settings.lang.getColored("no-recipe"), "");

            InvUtil.createDisplay(black, recipeGUI, 19 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            InvUtil.createDisplay(black, recipeGUI, 20 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
            InvUtil.createDisplay(black, recipeGUI, 21 + 9 * 1, Settings.lang.getColored("no-recipe"), "");
        }
        else
        {
            for (int i = 0; i < Math.min(items.size(), 9); i++)
            {
                int slot = 1 + 9 * 1 + (i % 3) + (i / 3) * 9;
                recipeGUI.setItem(slot, items.get(i));
            }
        }

        recipeGUI.setItem(16+9*1, e.getCurrentItem());

        if(ballConfig.buyPrice != -1 && player.hasPermission("pocketmob.user.buy." + ballConfig.name))
        {
            InvUtil.createDisplay(new ItemStack(Material.EMERALD), recipeGUI, 49,
                                     Settings.lang.getColored("buy"),
                                     Settings.lang.getColored("buy-lore")
                                             .replace("{price}", ballConfig.buyPrice + "")
                                             .replace("{amount}", ballConfig.buyAmount + "")
            );
            buying.put(player.getUniqueId(), ballConfig);
        }

        InvUtil.createDisplay(new ItemStack(Material.SPIDER_EYE), recipeGUI, 53,
                                  Settings.lang.getColored("back"), "");

        player.openInventory(recipeGUI);
    }

    @EventHandler
    public void recipeGUIClick_preview(InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CHEST || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if (!e.getView().getTitle().equalsIgnoreCase(Settings.lang.getColored("recipe")))
            return;

        e.setCancelled(true);

        if(!e.getCurrentItem().hasItemMeta())
            return;

        if(matchGUIIconName(e.getCurrentItem(), "back"))
        {
            buying.remove(((Player) e.getWhoClicked()).getUniqueId());
            showRecipesView((Player)e.getWhoClicked());
        }
        else if(matchGUIIconName(e.getCurrentItem(), "buy"))
        {
            Player player = (Player) e.getWhoClicked();
            openingConfirmGui.add(player.getUniqueId());
            showConfirmGUI(player);
        }
    }

    @EventHandler
    public void buyClick(InventoryClickEvent e)
    {
        if(e.getInventory().getType() != InventoryType.CHEST || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if (!e.getView().getTitle().equalsIgnoreCase(Settings.lang.getColored("confirm-buy")))
            return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        if(matchGUIIconName(e.getCurrentItem(), "confirm"))
        {
            Ball ballConfig = buying.get(player.getUniqueId());
            if (ballConfig == null)
            {
                player.closeInventory();
                return;
            }

            if (Main.economy == null)
            {
                ActionBar.send(player, Settings.lang.getColored("economy-not-available"));
                player.closeInventory();
                buying.remove(player.getUniqueId());
                return;
            }

            double balance = getBalance(player);
            if(balance < 0)
            {
                ActionBar.send(player, Settings.lang.getColored("balance-query-failed"));
                player.closeInventory();
                buying.remove(player.getUniqueId());
                return;
            }

            if(balance >= ballConfig.buyPrice)
            {
                ItemStack toGive = ballConfig.getItemStack().clone();
                toGive.setAmount(ballConfig.buyAmount);

                if (!canFitItem(player, toGive))
                {
                    ActionBar.send(player, Settings.lang.getColored("inventory-full"));
                    player.closeInventory();
                    buying.remove(player.getUniqueId());
                    return;
                }

                if (withdrawPlayer(player, ballConfig.buyPrice))
                {
                    try
                    {
                        InvUtil.giveItem(player, toGive);
                        ActionBar.send(player, Settings.lang.getColored("successfully-bought").replace("{name}", ballConfig.displayName));
                    }
                    catch (Exception ex)
                    {
                        Main.inst.getLogger().warning("Failed to deliver purchased item: " + ex.getMessage());
                        if (!depositPlayer(player, ballConfig.buyPrice))
                        {
                            Main.inst.getLogger().warning("Failed to refund player after purchase delivery failure: " + player.getName());
                        }
                        ActionBar.send(player, Settings.lang.getColored("purchase-delivery-failed"));
                    }
                }
                else
                {
                    ActionBar.send(player, Settings.lang.getColored("withdraw-failed"));
                }
            }
            else
            {
                ActionBar.send(player, Settings.lang.getColored("insufficient-money"));
            }
            player.closeInventory();
            buying.remove(player.getUniqueId());
        }
        else if(matchGUIIconName(e.getCurrentItem(), "cancel"))
        {
            buying.remove(player.getUniqueId());
            showRecipesView(player);
        }
    }

    boolean matchGUIIconName(ItemStack itemStack, String configStr)
    {
        if (!itemStack.hasItemMeta() || itemStack.getItemMeta() == null)
            return false;
        return ChatColor.stripColor(itemStack.getItemMeta().getDisplayName())
                .equals(ChatColor.stripColor(Settings.lang.getColored(configStr)));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        buying.remove(e.getPlayer().getUniqueId());
        openingConfirmGui.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        if (!(e.getPlayer() instanceof Player player))
        {
            return;
        }

        String title = e.getView().getTitle();
        if (title.equalsIgnoreCase(Settings.lang.getColored("recipe")) && openingConfirmGui.remove(player.getUniqueId()))
        {
            return;
        }

        if (title.equalsIgnoreCase(Settings.lang.getColored("recipe")) || title.equalsIgnoreCase(Settings.lang.getColored("confirm-buy")))
        {
            buying.remove(player.getUniqueId());
        }
    }
}
