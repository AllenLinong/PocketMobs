package dev.lone.pocketmobs.utils;

import dev.lone.pocketmobs.Main;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Optional Dominion integration without a compile-time dependency.
 */
public final class DominionProtectionUtils
{
    private static final String DOMINION_PLUGIN = "Dominion";
    private static final String API_CLASS = "cn.lunadeer.dominion.api.DominionAPI";
    private static final String PRI_FLAG_CLASS = "cn.lunadeer.dominion.api.dtos.flag.PriFlag";
    private static final String FLAGS_CLASS = "cn.lunadeer.dominion.api.dtos.flag.Flags";

    private static Method getInstanceMethod;
    private static Method getDominionMethod;
    private static Method getPrivilegeMethod;
    private static Field animalKillingFlag;
    private static Field monsterKillingFlag;
    private static Field villagerKillingFlag;
    private static boolean apiResolved;
    private static boolean errorLogged;

    private DominionProtectionUtils()
    {
    }

    /**
     * Uses Dominion's killing privilege for the mob category being captured.
     */
    public static boolean canCatchMob(Player player, Entity target, Location location)
    {
        Plugin dominion = Main.inst.getServer().getPluginManager().getPlugin(DOMINION_PLUGIN);
        if (dominion == null || !dominion.isEnabled())
            return true;

        try
        {
            resolveApi();
            if (getInstanceMethod == null || getDominionMethod == null || getPrivilegeMethod == null)
                return true;

            Object api = getInstanceMethod.invoke(null);
            Object dominionAtLocation = getDominionMethod.invoke(api, location);
            if (dominionAtLocation == null)
                return true;

            Field flagField = target instanceof Villager
                    ? villagerKillingFlag
                    : target instanceof Monster ? monsterKillingFlag : animalKillingFlag;
            Object flag = flagField.get(null);
            return (boolean) getPrivilegeMethod.invoke(api, location, flag, player);
        }
        catch (ReflectiveOperationException | ClassCastException | LinkageError exception)
        {
            logIntegrationError(exception);
            return true;
        }
    }

    private static synchronized void resolveApi() throws ReflectiveOperationException
    {
        if (apiResolved)
            return;

        apiResolved = true;
        Class<?> apiClass = Class.forName(API_CLASS);
        getInstanceMethod = apiClass.getMethod("getInstance");
        getDominionMethod = apiClass.getMethod("getDominion", Location.class);
        getPrivilegeMethod = apiClass.getMethod("checkPrivilegeFlag", Location.class,
                Class.forName(PRI_FLAG_CLASS), Player.class);

        Class<?> flagsClass = Class.forName(FLAGS_CLASS);
        animalKillingFlag = flagsClass.getField("ANIMAL_KILLING");
        monsterKillingFlag = flagsClass.getField("MONSTER_KILLING");
        villagerKillingFlag = flagsClass.getField("VILLAGER_KILLING");
    }

    private static void logIntegrationError(Throwable exception)
    {
        if (!errorLogged)
        {
            errorLogged = true;
            Main.inst.getLogger().warning("Dominion integration is unavailable: " + exception.getMessage());
        }
    }
}