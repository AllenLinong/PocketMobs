package dev.lone.pocketmobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

final class CaughtMobSpawnSupport
{
    private CaughtMobSpawnSupport()
    {
    }

    static boolean isSpawnUsable(Entity entity)
    {
        if (entity == null)
        {
            return false;
        }

        try
        {
            Entity liveEntity = Bukkit.getEntity(entity.getUniqueId());
            if (liveEntity == null || liveEntity.isDead())
            {
                CaughtMob.debugLog("Discarding spawned entity because it is not present in the world after creation.");
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            Main.inst.getLogger().warning("Failed to validate spawned entity state: " + e.getMessage());
            return false;
        }
    }

    static Entity spawnFromSnapshot(CaughtMob mob, Location location)
    {
        if (mob.nbtTagCompound == null || mob.nbtTagCompound.isEmpty())
        {
            return null;
        }

        try
        {
            // Native, complete restore from the captured SNBT: createEntity round-trips
            // base fields, subtype data, attributes, effects, variant, custom name and the
            // entity PDC in one call. prepareSpawnedEntity re-teleports to the exact target
            // so position never depends on the stored NBT Pos.
            EntitySnapshot snapshot = Bukkit.getEntityFactory().createEntitySnapshot(mob.nbtTagCompound);
            if (snapshot == null)
            {
                return null;
            }

            Entity entity = snapshot.createEntity(location);
            if (entity == null)
            {
                return null;
            }

            prepareSpawnedEntity(entity, location);
            CaughtMob.debugLog("Spawned entity from EntitySnapshot: " + entity.getType());
            if (isSpawnUsable(entity))
            {
                return entity;
            }

            CaughtMob.debugLog("EntitySnapshot spawn produced an unusable entity, falling back.");
            try
            {
                entity.remove();
            }
            catch (Exception ignored)
            {
            }
        }
        catch (Exception e)
        {
            CaughtMob.debugLog("EntitySnapshot spawn failed, falling back for this mob: " + e.getMessage());
        }
        return null;
    }

    static Entity spawnEntityBasic(CaughtMob mob, Location location)
    {
        CaughtMob.debugLog("Spawning entity using basic fallback path: " + mob.type.name());

        Class<? extends Entity> clazz = mob.type.getEntityClass();
        if (clazz == null)
        {
            Main.inst.getLogger().warning("Cannot spawn entity because its Bukkit class is unavailable: " + mob.type.name());
            return null;
        }

        Entity entity = location.getWorld().spawn(location, clazz, newEntity ->
                newEntity.setMetadata("SpawnedWithPB", new FixedMetadataValue(Main.inst, true)));

        entity.teleport(location);
        entity.setVelocity(new Vector(0, 0.1f, 0));

        if (isSpawnUsable(entity))
        {
            return entity;
        }

        CaughtMob.debugLog("Basic spawn path produced an unusable entity.");
        try
        {
            entity.remove();
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    private static void prepareSpawnedEntity(Entity entity, Location location)
    {
        entity.teleport(location);
        entity.setMetadata("SpawnedWithPB", new FixedMetadataValue(Main.inst, true));
        entity.setVelocity(new Vector(0, 0.1f, 0));
    }
}
