package dev.lone.pocketmobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Detects mobs that are spawned and managed by pet plugins (MyPet & co.).
 * <p>
 * Those mobs are not plain vanilla mobs: their data lives in the owning plugin,
 * so catching one into a ball would remove it from the plugin and break (or
 * duplicate) the pet. Detection is reflection based so PocketMobs needs no
 * compile-time dependency; classes are resolved lazily and only while the
 * target plugin is actually enabled.
 */
public final class PetProtection
{
    // MyPet wraps every pet with a Bukkit entity implementing this interface
    // (it extends org.bukkit.entity.Creature). Present in MyPet 3.x.
    private static final String MYPET_INTERFACE = "de.Keyle.MyPet.api.entity.MyPetBukkitEntity";

    private static volatile Class<?> myPetInterface;
    private static volatile boolean myPetResolved;

    private PetProtection() { }

    /**
     * @return true if the entity was spawned and is managed by a pet plugin.
     */
    public static boolean isPetPluginMob(Entity entity)
    {
        if (entity == null)
            return false;

        if (Bukkit.getPluginManager().isPluginEnabled("MyPet"))
            return isMyPet(entity);

        return false;
    }

    private static boolean isMyPet(Entity entity)
    {
        if (!myPetResolved)
        {
            synchronized (PetProtection.class)
            {
                if (!myPetResolved)
                {
                    try
                    {
                        // Resolve through MyPet's own classloader: the interface is
                        // shipped inside the MyPet plugin, not inside PocketMobs.
                        Plugin myPet = Bukkit.getPluginManager().getPlugin("MyPet");
                        if (myPet != null)
                        {
                            myPetInterface = Class.forName(MYPET_INTERFACE, false, myPet.getClass().getClassLoader());
                        }
                    }
                    catch (Throwable ignored)
                    {
                        myPetInterface = null;
                    }
                    myPetResolved = true;
                }
            }
        }
        return myPetInterface != null && myPetInterface.isInstance(entity);
    }
}
