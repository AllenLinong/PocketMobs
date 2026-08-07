package dev.lone.pocketmobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class CaughtMob
{
    // Structured PDC sub-container keys. The captured entity is stored ONCE, as the
    // full SNBT (K_NBT) produced by entity.getAsString(); release restores it natively
    // via EntitySnapshot.createEntity. The villager/horse/variant strings are kept
    // purely to render ball lore (CaughtMobDisplaySupport) without deserializing.
    private static final NamespacedKey K_TYPE = Constants.key("mob_type");
    private static final NamespacedKey K_NBT = Constants.key("mob_nbt");
    private static final NamespacedKey K_VILLAGER = Constants.key("mob_villager");
    private static final NamespacedKey K_HORSE = Constants.key("mob_horse");
    private static final NamespacedKey K_VARIANT = Constants.key("mob_variant");

    public EntityType type;
    public String nbtTagCompound;
    // Lore-only summaries (profession/type/level, horse color/style/..., variant).
    public String villagerData;
    public String horseData;
    public String variantData;
    private boolean degradedPlaceholder;

    private static void logSafe(String message)
    {
        if (Main.inst != null)
        {
            Main.inst.getLogger().warning(message);
        }
        else
        {
            Bukkit.getLogger().warning("[PocketMobs] " + message);
        }
    }

    static void debugLog(String message)
    {
        if (!Settings.debug)
        {
            return;
        }

        if (Main.inst != null)
        {
            Main.inst.getLogger().info(message);
        }
        else
        {
            Bukkit.getLogger().info("[PocketMobs] " + message);
        }
    }

    public CaughtMob(Entity entity)
    {
        type = entity.getType();

        // Single full-fidelity snapshot. entity.getAsString() captures everything
        // (base fields, subtype data, attributes, effects, PDC, custom name); it is
        // restored in one call by EntitySnapshot.createEntity on release.
        nbtTagCompound = entity.getAsString();

        // These lore summaries are the ONLY hand-extracted data left — the whole
        // manual restore layer is gone, replaced by the native snapshot restore.
        if (entity instanceof Villager)
        {
            CaughtMobVillagerSupport.saveVillagerData(this, (Villager) entity);
        }
        if (entity instanceof AbstractHorse)
        {
            CaughtMobHorseSupport.saveHorseData(this, (AbstractHorse) entity);
        }
        CaughtMobVariantSupport.saveVariantData(this, entity);
    }

    private CaughtMob(EntityType type, String nbtTagCompound, String villagerData, String horseData, String variantData)
    {
        this(type, nbtTagCompound, villagerData, horseData, variantData, false);
    }

    private CaughtMob(EntityType type, String nbtTagCompound, String villagerData, String horseData, String variantData, boolean degradedPlaceholder)
    {
        this.type = type;
        this.nbtTagCompound = nbtTagCompound;
        this.villagerData = villagerData;
        this.horseData = horseData;
        this.variantData = variantData;
        this.degradedPlaceholder = degradedPlaceholder;
    }

    public boolean isDegradedPlaceholder()
    {
        return degradedPlaceholder;
    }

    private static CaughtMob createPlaceholderPig(String reason)
    {
        logSafe("Falling back to placeholder pig: " + reason);
        return new CaughtMob(EntityType.PIG, null, null, null, null, true);
    }

    public static Entity spawnPlaceholderPig(Location location, String reason)
    {
        return createPlaceholderPig(reason).spawnEntity(location);
    }

    public double getLife()
    {
        if (nbtTagCompound == null || nbtTagCompound.isEmpty())
            return 20.0;
        double health = parseNBTDouble(nbtTagCompound, "Health");
        return health > 0 ? health : 20.0;
    }

    public String getDisplayName()
    {
        return CaughtMobDisplaySupport.getDisplayName(this);
    }

    public List<String> getDisplayLoreLines()
    {
        return CaughtMobDisplaySupport.getDisplayLoreLines(this);
    }

    public EntityType getType()
    {
        return type;
    }

    /**
     * Approximate stored payload size, used to reject mobs whose data would bloat
     * the item (and lag/kick clients).
     */
    public int approxDataSize()
    {
        return len(nbtTagCompound) + len(villagerData) + len(horseData) + len(variantData);
    }

    private static int len(String s)
    {
        return s == null ? 0 : s.length();
    }

    /**
     * Writes this captured mob into a PDC sub-container. Only non-empty fields are
     * stored; PDC values are binary-safe so the raw SNBT goes in verbatim.
     */
    public void writeTo(PersistentDataContainer sub)
    {
        sub.set(K_TYPE, PersistentDataType.STRING, type.name());
        putString(sub, K_NBT, nbtTagCompound);
        putString(sub, K_VILLAGER, villagerData);
        putString(sub, K_HORSE, horseData);
        putString(sub, K_VARIANT, variantData);
    }

    private static void putString(PersistentDataContainer sub, NamespacedKey key, String value)
    {
        if (value != null && !value.isEmpty())
        {
            sub.set(key, PersistentDataType.STRING, value);
        }
    }

    /**
     * Reconstructs a captured mob from a PDC sub-container written by
     * {@link #writeTo}. Returns null if the container is missing or has no valid
     * entity type, so the caller can fall back to a placeholder.
     */
    public static CaughtMob readFrom(PersistentDataContainer sub)
    {
        if (sub == null)
        {
            return null;
        }

        String typeName = sub.get(K_TYPE, PersistentDataType.STRING);
        if (typeName == null)
        {
            return null;
        }

        EntityType entityType;
        try
        {
            entityType = EntityType.valueOf(typeName);
        }
        catch (IllegalArgumentException e)
        {
            logSafe("Invalid stored entity type in ball data: " + typeName);
            return null;
        }

        return new CaughtMob(
                entityType,
                sub.get(K_NBT, PersistentDataType.STRING),
                sub.get(K_VILLAGER, PersistentDataType.STRING),
                sub.get(K_HORSE, PersistentDataType.STRING),
                sub.get(K_VARIANT, PersistentDataType.STRING));
    }

    public Entity spawnEntity(Location location)
    {
        if (location == null || location.getWorld() == null)
        {
            Main.inst.getLogger().warning("Cannot spawn stored mob because the target location or world is null.");
            return null;
        }

        Entity entity = null;

        if (nbtTagCompound != null && !nbtTagCompound.isEmpty())
        {
            entity = CaughtMobSpawnSupport.spawnFromSnapshot(this, location);
        }

        // Last resort: a bare entity of the right type (used only if the native
        // snapshot restore failed, e.g. corrupt SNBT). It has no restored state.
        if (entity == null)
        {
            entity = CaughtMobSpawnSupport.spawnEntityBasic(this, location);
        }

        if (entity != null)
        {
            // createEntity already restored the entity's full state. The only post-step
            // is the release-health policy (restore-full-health-on-free-mob / clamping).
            if (entity instanceof LivingEntity livingEntity)
            {
                Double desiredHealth = readStoredHealth();
                if (desiredHealth != null)
                {
                    restoreStoredHealth(livingEntity, desiredHealth);
                }
            }

            if (Settings.debug) Main.inst.getLogger().info("Spawned stored mob: " + entity.getType() + " UUID: " + entity.getUniqueId());
        }
        else
        {
            Main.inst.getLogger().warning("Failed to spawn stored mob.");
        }

        // If every spawn path failed (e.g. a region plugin refused this specific mob
        // type), return null so the caller keeps the ball loaded — rather than replacing
        // the captured mob with a placeholder pig and emptying the ball. Corrupt stored
        // data is handled upstream (readFrom == null -> placeholder pig in Ball.freeMob).
        return entity;
    }

    private Double readStoredHealth()
    {
        if (nbtTagCompound == null || nbtTagCompound.isEmpty())
        {
            return null;
        }

        double storedHealth = parseNBTDouble(nbtTagCompound, "Health");
        if (storedHealth <= 0)
        {
            return null;
        }
        return storedHealth;
    }

    private void restoreStoredHealth(LivingEntity livingEntity, double desiredHealth)
    {
        try
        {
            AttributeInstance maxHealthAttr = livingEntity.getAttribute(Attribute.MAX_HEALTH);
            double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0D;
            double clampedHealth = Settings.restoreFullHealthOnRelease ? maxHealth : Math.min(desiredHealth, maxHealth);
            if (clampedHealth <= 0)
            {
                clampedHealth = Math.min(1.0D, maxHealth);
            }
            livingEntity.setHealth(clampedHealth);
        }
        catch (Exception e)
        {
            Main.inst.getLogger().warning("Failed to restore stored health: " + e.getMessage());
        }
    }

    /**
     * Scans an SNBT string for the first "&lt;key&gt;:" numeric value. Small standalone
     * helper for getLife()/readStoredHealth().
     */
    private static double parseNBTDouble(String nbtString, String... keys)
    {
        try
        {
            for (String key : keys)
            {
                int index = nbtString.indexOf(key + ":");
                if (index != -1)
                {
                    int start = index + key.length() + 1;
                    int end = nbtString.indexOf(",", start);
                    if (end == -1)
                    {
                        end = nbtString.indexOf("}", start);
                    }
                    if (end != -1)
                    {
                        String value = nbtString.substring(start, end).trim();
                        value = value.replace("f", "").replace("F", "").replace("d", "").replace("D", "");
                        return Double.parseDouble(value);
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return -1;
    }
}
