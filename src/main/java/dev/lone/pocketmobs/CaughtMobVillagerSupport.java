package dev.lone.pocketmobs;

import org.bukkit.entity.Villager;

final class CaughtMobVillagerSupport
{
    private CaughtMobVillagerSupport()
    {
    }

    /**
     * Captures the villager's profession/type/level for ball lore only
     * (CaughtMobDisplaySupport). Everything else — trades, reputations, xp, restocks,
     * custom name — round-trips natively via the SNBT snapshot on release, so there
     * is no restore method here anymore.
     */
    static void saveVillagerData(CaughtMob mob, Villager villager)
    {
        try
        {
            StringBuilder data = new StringBuilder();
            try
            {
                data.append("profession:").append(villager.getProfession().getKey().getKey()).append(";");
            }
            catch (Exception e)
            {
                Main.inst.getLogger().warning("Failed to read villager profession: " + e.getMessage());
            }
            try
            {
                data.append("type:").append(villager.getVillagerType().getKey().getKey()).append(";");
            }
            catch (Exception e)
            {
                Main.inst.getLogger().warning("Failed to read villager type: " + e.getMessage());
            }
            data.append("level:").append(villager.getVillagerLevel()).append(";");
            mob.villagerData = data.toString();
        }
        catch (Exception e)
        {
            Main.inst.getLogger().warning("Failed to save villager data: " + e.getMessage());
        }
    }
}
