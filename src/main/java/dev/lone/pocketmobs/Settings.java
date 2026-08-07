package dev.lone.pocketmobs;

import dev.lone.pocketmobs.utils.CustomConfigFile;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Settings
{
    private static final int CURRENT_CONFIG_VERSION = 3;

    // These are read from Folia region threads (event handlers) while reload()
    // writes them on another thread, so they are volatile for safe publication.
    // Each is a single reference/primitive; readers see either the old or the
    // new value, never a torn one. `worlds` is only ever reassigned wholesale
    // (never mutated in place), so a volatile reference gives readers a
    // consistent snapshot to iterate.
    public static volatile CustomConfigFile lang;
    public static volatile CustomConfigFile config;
    public static volatile List<String> worlds;
    public static volatile boolean returnToInvFree;
    public static volatile boolean returnToInvCatch;
    public static volatile boolean dropperSpawnMob;
    public static volatile boolean reduceUsagesOnMiss;
    public static volatile boolean restoreFullHealthOnRelease;
    public static volatile boolean keepEntityUuid;
    public static volatile boolean debug;
    private static volatile boolean loaded = false;

    public static boolean isLoaded()
    {
        return loaded;
    }

    public static void load()
    {
        try
        {
            config = new CustomConfigFile(Main.inst, "config", true, false);

            // Apply config migrations before reading runtime values.
            migrateConfig();

            loadLanguageFile(true);
            readRuntimeSettings();

            loaded = true;
            Main.inst.getLogger().info("Configuration loaded successfully.");
        }
        catch (Exception e)
        {
            loaded = false;
            Main.inst.getLogger().severe("Failed to load configuration: " + e.getMessage());
            Main.inst.getLogger().severe("PocketMobs may not work correctly until the configuration is fixed.");

            // Initialize safe defaults to reduce follow-up failures.
            worlds = new ArrayList<>();
            returnToInvFree = false;
            returnToInvCatch = false;
            dropperSpawnMob = false;
            reduceUsagesOnMiss = true;
            restoreFullHealthOnRelease = false;
            keepEntityUuid = false;
            debug = false;
        }
    }

    public static boolean reload()
    {
        try
        {
            if (config == null)
            {
                load();
                return loaded;
            }

            config.reloadFromFile();
            loadLanguageFile(false);
            readRuntimeSettings();
            loaded = true;
            Main.inst.getLogger().info("Configuration reloaded successfully.");
            return true;
        }
        catch (Exception e)
        {
            loaded = false;
            Main.inst.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            return false;
        }
    }

    private static void loadLanguageFile(boolean allowUpdateFromResource)
    {
        String langFile = config.getString("lang");
        if (langFile == null || langFile.isEmpty())
        {
            langFile = "en";
            Main.inst.getLogger().warning("Language file not configured, defaulting to en.");
        }

        CustomConfigFile candidate = createLanguageConfig(langFile, allowUpdateFromResource);
        if (candidate == null || (!candidate.existsOnDisk() && !"en".equalsIgnoreCase(langFile)))
        {
            Main.inst.getLogger().warning("Language file '" + langFile + "' is unavailable, falling back to en.");
            candidate = createLanguageConfig("en", allowUpdateFromResource);
        }

        if (candidate == null)
        {
            throw new IllegalStateException("No valid language file could be loaded.");
        }

        lang = candidate;
    }

    private static CustomConfigFile createLanguageConfig(String langFile, boolean allowUpdateFromResource)
    {
        String targetFile = "lang/" + langFile;
        if (lang != null && lang.getFileName().equals(targetFile))
        {
            lang.reloadFromFile();
            return lang;
        }
        return new CustomConfigFile(Main.inst, targetFile, allowUpdateFromResource, true);
    }

    private static void readRuntimeSettings()
    {
        worlds = config.getConfig().getStringList("worlds");
        if (worlds == null || worlds.isEmpty())
        {
            worlds = new ArrayList<>();
            Main.inst.getLogger().info("No world restrictions configured; PocketMobs is enabled in all worlds.");
        }

        returnToInvFree = config.getBoolean("logic.ball-behaviour.return-to-inventory.on-free-mob", false);
        returnToInvCatch = config.getBoolean("logic.ball-behaviour.return-to-inventory.on-catch-mob", false);
        dropperSpawnMob = config.getBoolean("logic.ball-behaviour.dropper-spawns-mob", false);
        reduceUsagesOnMiss = config.getBoolean("logic.ball-behaviour.reduce-usages.miss-target", true);
        restoreFullHealthOnRelease = config.getBoolean("logic.ball-behaviour.restore-full-health.on-free-mob", false);
        keepEntityUuid = config.getBoolean("keep-entity-uuid", false);
        debug = config.getBoolean("debug", false);
    }

    /**
     * Migrates older config files to the current version.
     */
    private static void migrateConfig()
    {
        FileConfiguration cfg = config.getConfig();
        int version = cfg.getInt("config-version", 1);

        if (version < CURRENT_CONFIG_VERSION)
        {
            Main.inst.getLogger().info("Detected old config version v" + version + ", migrating to v" + CURRENT_CONFIG_VERSION + "...");

            // Version 1 -> 2: add the new ball-behaviour options.
            if (version < 2)
            {
                if (!cfg.contains("logic.ball-behaviour.return-to-inventory.on-free-mob"))
                {
                    cfg.set("logic.ball-behaviour.return-to-inventory.on-free-mob", false);
                }
                if (!cfg.contains("logic.ball-behaviour.return-to-inventory.on-catch-mob"))
                {
                    cfg.set("logic.ball-behaviour.return-to-inventory.on-catch-mob", false);
                }
                if (!cfg.contains("logic.ball-behaviour.dropper-spawns-mob"))
                {
                    cfg.set("logic.ball-behaviour.dropper-spawns-mob", false);
                }
                if (!cfg.contains("logic.ball-behaviour.reduce-usages.miss-target"))
                {
                    cfg.set("logic.ball-behaviour.reduce-usages.miss-target", true);
                }
            }

            // Version 2 -> 3: add the full-health release toggle.
            if (version < 3)
            {
                if (!cfg.contains("logic.ball-behaviour.restore-full-health.on-free-mob"))
                {
                    cfg.set("logic.ball-behaviour.restore-full-health.on-free-mob", false);
                }
            }

            // Update the stored config version.
            cfg.set("config-version", CURRENT_CONFIG_VERSION);

            // Save migrated config values back to disk.
            try
            {
                config.getConfig().save(new File(Main.inst.getDataFolder(), "config.yml"));
                Main.inst.getLogger().info("Config migration completed. Upgraded to v" + CURRENT_CONFIG_VERSION + ".");
            }
            catch (Exception e)
            {
                Main.inst.getLogger().warning("Failed to save migrated config: " + e.getMessage());
            }
        }
    }
}
