package dev.lone.pocketmobs.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;

public class LocaleUtils
{
    public static String getEntityTranslationKey(EntityType type)
    {
        try
        {
            return type.translationKey();
        }
        catch (Throwable e)
        {
            return "entity.minecraft." + type.name().toLowerCase();
        }
    }

    public static Component getEntityTranslatable(EntityType type)
    {
        return Component.translatable(getEntityTranslationKey(type));
    }
}
