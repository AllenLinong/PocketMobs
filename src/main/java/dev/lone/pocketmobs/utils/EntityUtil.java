package dev.lone.pocketmobs.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EntityUtil
{
    public static String getReadableEntityTypeName(EntityType type)
    {
        try
        {
            String translationKey = getTranslationKey(type);
            if (translationKey != null)
            {
                return formatTranslationKey(translationKey);
            }
        }
        catch (Exception ignored)
        {
        }

        return capitalizeWords(type.toString().replace("_", " "));
    }

    public static String getReadableEntityTypeName(Entity entity, boolean useCustomNameIfSet)
    {
        if (useCustomNameIfSet && entity.getCustomName() != null)
        {
            // Common health bar plugins append heart symbols to custom names.
            if (!entity.getCustomName().contains("\u2665") && !entity.getCustomName().contains("\u2764"))
            {
                return entity.getCustomName();
            }
        }

        try
        {
            Component name = entity.name();
            if (name instanceof TranslatableComponent translatable)
            {
                return formatTranslationKey(translatable.key());
            }
            if (name != null)
            {
                String text = LegacyComponentSerializer.legacySection().serialize(name);
                if (!text.isEmpty())
                {
                    return text;
                }
            }
        }
        catch (Exception ignored)
        {
        }

        return capitalizeWords(entity.getType().toString().replace("_", " "));
    }

    private static String getTranslationKey(EntityType type)
    {
        if (type == null)
        {
            return null;
        }
        return "entity.minecraft." + type.name().toLowerCase();
    }

    private static String formatTranslationKey(String key)
    {
        if (key == null || key.isEmpty())
        {
            return null;
        }

        if (key.startsWith("entity.minecraft."))
        {
            String name = key.substring("entity.minecraft.".length());
            return capitalizeWords(name.replace("_", " "));
        }

        return key;
    }

    /**
     * Title-cases each space-separated word. Replaces commons-lang2 WordUtils,
     * which Paper dropped from the 1.21.4 runtime classpath (a NoClassDefFoundError
     * that fired on the default config and stopped the plugin from enabling).
     */
    private static String capitalizeWords(String s)
    {
        StringBuilder out = new StringBuilder(s.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            out.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
            capitalizeNext = (c == ' ');
        }
        return out.toString();
    }
}
