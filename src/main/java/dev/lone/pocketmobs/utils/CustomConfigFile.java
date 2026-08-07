package dev.lone.pocketmobs.utils;

import dev.lone.pocketmobs.Settings;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class CustomConfigFile
{
    private FileConfiguration config;
    private final Plugin plugin;
    private final String fileName;
    private final File configFile;
    private final boolean needsToBeUpdatedIfDifferentFromResourceFile;
    private final boolean announceCustomLanguage;

    public CustomConfigFile(Plugin plugin, String fileName, boolean needsToBeUpdatedIfDifferentFromResourceFile, boolean announceCustomLanguage)
    {
        this.plugin = plugin;
        this.fileName = fileName;
        this.needsToBeUpdatedIfDifferentFromResourceFile = needsToBeUpdatedIfDifferentFromResourceFile;
        this.announceCustomLanguage = announceCustomLanguage;
        this.configFile = new File(plugin.getDataFolder(), this.fileName + ".yml");

        config = YamlConfiguration.loadConfiguration(configFile);
        update();
    }

    public void reloadFromFile()
    {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void update()
    {
        try
        {
            if (!configFile.exists())
            {
                try (InputStream resource = plugin.getResource(this.fileName + ".yml"))
                {
                    if (resource == null)
                    {
                        throw new IOException("Bundled resource not found: " + this.fileName + ".yml");
                    }
                    // lang files live in a lang/ subdir, so create the parent dir before
                    // copying — Files.copy (unlike the old helper) won't create it.
                    Files.createDirectories(configFile.getParentFile().toPath());
                    Files.copy(resource, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            else if (needsToBeUpdatedIfDifferentFromResourceFile)
            {
                InputStream resource = plugin.getResource(this.fileName + ".yml");
                if (resource == null)
                {
                    plugin.getLogger().warning("Bundled resource not found for " + fileName + ".yml, skipping automatic update.");
                }
                else
                {
                    try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8))
                    {
                        FileConfiguration bundled = YamlConfiguration.loadConfiguration(reader);
                        for (String key : bundled.getKeys(true))
                        {
                            if (!config.contains(key))
                            {
                                config.set(key, bundled.get(key));
                            }
                        }
                        config.save(configFile);
                    }
                }
            }

            config.load(configFile);
        }
        catch (Exception e)
        {
            plugin.getLogger().severe("Failed to load config file: " + fileName + ".yml");
            plugin.getLogger().severe("Error details: " + e.getMessage());
            logDebugStacktrace("Config load failed for " + fileName + ".yml", e);

            if (announceCustomLanguage && Settings.lang != null)
            {
                String msg = Settings.lang.getColored("using-custom-language").replace("{file}", fileName);
                Bukkit.getServer().getConsoleSender().sendMessage(org.bukkit.ChatColor.YELLOW + msg);
            }
        }
    }

    // Lang values may use EITHER legacy '&' codes OR MiniMessage tags. renderColors
    // normalizes them to a legacy §-string (which the client interprets everywhere), so
    // every call site keeps receiving a plain colored String.
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern MINIMESSAGE_TAG = Pattern.compile("<[#/!]?[a-zA-Z0-9_:.#-]+>");

    private static String renderColors(String raw)
    {
        if (raw == null)
        {
            return "";
        }
        // MiniMessage 4.x THROWS if fed a '§' string, so pick the serializer by format:
        // a value containing a <tag> is parsed as MiniMessage; anything else is treated
        // as legacy '&' codes. On any MiniMessage parse error, fall back to legacy.
        if (MINIMESSAGE_TAG.matcher(raw).find())
        {
            try
            {
                return LEGACY.serialize(MINI.deserialize(raw));
            }
            catch (Exception ignored)
            {
            }
        }
        return legacyToSection(raw);
    }

    private static String legacyToSection(String raw)
    {
        char[] chars = raw.toCharArray();
        for (int i = 0; i + 1 < chars.length; i++)
        {
            if (chars[i] == '&' && "0123456789abcdefklmnorABCDEFKLMNOR".indexOf(chars[i + 1]) > -1)
            {
                chars[i] = '§';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    public String getLang(String name)
    {
        try
        {
            return renderColors(config.getString(name));
        }
        catch (NullPointerException exc)
        {
            plugin.getLogger().warning("Missing language key '" + name + "' in " + fileName + ".yml");
            logDebugStacktrace("Missing language key lookup failed for " + name, exc);

            String errorMsg = Settings.lang != null
                    ? Settings.lang.getColored("error-in-file").replace("{file}", fileName + ".yml")
                    : "[PocketMobs] Error in file " + fileName + ".yml";
            String keyMsg = Settings.lang != null
                    ? Settings.lang.getColored("key-not-found").replace("{key}", name)
                    : " '" + name + "' not found.";

            broadcastToConsoleAndOps("[PocketMobs] " + ChatColor.RED + errorMsg + keyMsg);
        }

        return ChatColor.RED + "ErrorInLangFile " + fileName + ".yml";
    }

    public String getColored(String name)
    {
        if (hasKey(name))
        {
            return renderColors(config.getString(name));
        }

        String errorMsg = ChatColor.RED + "[PocketMobs] Missing language key: " + name;
        Bukkit.getConsoleSender().sendMessage(errorMsg);
        return ChatColor.GRAY + "[" + name + "]";
    }

    public String getStripped(String name)
    {
        return ChatColor.stripColor(getColored(name));
    }

    public FileConfiguration getConfig()
    {
        return config;
    }

    public String getFileName()
    {
        return fileName;
    }

    public boolean existsOnDisk()
    {
        return configFile.exists();
    }

    public void set(String path, Object value)
    {
        this.config.set(path, value);
    }

    public void save()
    {
        try
        {
            this.config.save(configFile);
        }
        catch (IOException e)
        {
            plugin.getLogger().warning("Failed to save config file: " + fileName + ".yml");
            logDebugStacktrace("Config save failed for " + fileName + ".yml", e);
        }
    }

    public String getString(String path)
    {
        return config.getString(path);
    }

    public String getString(String path, String defaultValue)
    {
        if (!hasKey(path))
        {
            return defaultValue;
        }
        return config.getString(path);
    }

    public int getInt(String path)
    {
        return config.getInt(path);
    }

    public int getInt(String path, int defaultValue)
    {
        if (hasKey(path))
        {
            return config.getInt(path);
        }
        return defaultValue;
    }

    public double getDouble(String path, double defaultValue)
    {
        if (hasKey(path))
        {
            return config.getDouble(path);
        }
        return defaultValue;
    }

    public double getDouble(String path)
    {
        return config.getDouble(path);
    }

    public boolean getBoolean(String path)
    {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean defaultValue)
    {
        if (hasKey(path))
        {
            return config.getBoolean(path);
        }
        return defaultValue;
    }

    public boolean hasKey(String path)
    {
        return config.get(path) != null;
    }

    private void broadcastToConsoleAndOps(String message)
    {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (player.isOp())
            {
                player.sendMessage(message);
            }
        }
    }

    private void logDebugStacktrace(String context, Throwable throwable)
    {
        if (Settings.debug)
        {
            plugin.getLogger().log(Level.WARNING, context, throwable);
        }
    }
}
