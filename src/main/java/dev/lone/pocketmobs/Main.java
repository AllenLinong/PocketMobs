package dev.lone.pocketmobs;

import dev.lone.pocketmobs.commands.MainCommand;
import dev.lone.pocketmobs.utils.HologramUtil;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entry point.
 */
public final class Main extends JavaPlugin
{
    public static Main inst;

    public static Object economy;
    public BallsManager ballsManager;
    public static MainCommand mainCommand;
    private BallsEventsListener eventsListener;

    @Override
    public void onEnable()
    {
        inst = this;

        Settings.load();

        // Abort startup if configuration loading failed.
        if (!Settings.isLoaded())
        {
            getLogger().severe("Configuration loading failed, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy())
        {
            getLogger().info(getLangMessage("vault-not-found"));
        }

        mainCommand = new MainCommand(this);
        ballsManager = new BallsManager(this);

        eventsListener = new BallsEventsListener();
        getServer().getPluginManager().registerEvents(eventsListener, this);

        getLogger().info(getDescription().getFullName() + " " + getLangMessage("plugin-enabled"));
    }

    /**
     * Safely reads a localized message when language files are available.
     */
    private String getLangMessage(String key)
    {
        if (Settings.lang != null)
        {
            return Settings.lang.getColored(key);
        }
        return "[" + key + "]";
    }

    @Override
    public void onDisable()
    {
        if (eventsListener != null)
        {
            eventsListener.cleanupAll();
        }

        if (mainCommand != null)
        {
            mainCommand.cleanup();
        }

        if (ballsManager != null)
        {
            ballsManager.cleanup();
        }

        HologramUtil.clearAllCooldowns();

        economy = null;
        mainCommand = null;
        inst = null;

        getLogger().info(getDescription().getName() + " " + getLangMessage("plugin-disabled"));
    }

    private boolean setupEconomy()
    {
        try
        {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> economyProvider = this.getServer().getServicesManager().getRegistration(economyClass);
            if (economyProvider != null)
            {
                economy = economyProvider.getProvider();
            }

            return economy != null;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
