package dev.lone.pocketmobs;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;

final class CaughtMobHorseSupport
{
    private CaughtMobHorseSupport()
    {
    }

    /**
     * Captures horse appearance/state for ball lore only (CaughtMobDisplaySupport).
     * Full restore is native (SNBT snapshot on release), so there is no restore
     * method here anymore.
     */
    static void saveHorseData(CaughtMob mob, AbstractHorse horse)
    {
        try
        {
            StringBuilder data = new StringBuilder();
            data.append("tamed:").append(horse.isTamed()).append(";");
            data.append("saddled:").append(horse.getInventory().getSaddle() != null).append(";");

            if (horse instanceof Horse h)
            {
                data.append("color:").append(h.getColor().name()).append(";");
                data.append("style:").append(h.getStyle().name()).append(";");

                if (h.getInventory().getArmor() != null)
                {
                    data.append("armor:").append(h.getInventory().getArmor().getType().name()).append(";");
                }
            }

            AttributeInstance jumpAttr = horse.getAttribute(Attribute.JUMP_STRENGTH);
            if (jumpAttr != null)
            {
                data.append("jump:").append(jumpAttr.getBaseValue()).append(";");
            }

            mob.horseData = data.toString();
        }
        catch (Exception e)
        {
            Main.inst.getLogger().warning("Failed to save horse data: " + e.getMessage());
        }
    }
}
