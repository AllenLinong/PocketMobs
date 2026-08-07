package dev.lone.pocketmobs;

import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Wolf;

final class CaughtMobVariantSupport
{
    private CaughtMobVariantSupport()
    {
    }

    static void saveVariantData(CaughtMob mob, Entity entity)
    {
        try
        {
            StringBuilder data = new StringBuilder();

            if (entity instanceof Tameable tameable)
            {
                data.append("tamed:").append(tameable.isTamed()).append(";");
                if (tameable.getOwner() != null)
                {
                    data.append("owner:").append(tameable.getOwner().getUniqueId()).append(";");
                }
            }

            if (entity instanceof Cat cat)
            {
                try
                {
                    data.append("cat_type:").append(cat.getCatType().getKey().getKey()).append(";");
                }
                catch (Exception e)
                {
                    Main.inst.getLogger().warning("Failed to read cat type: " + e.getMessage());
                }
                try
                {
                    data.append("collar:").append(cat.getCollarColor().name()).append(";");
                }
                catch (Exception e)
                {
                    Main.inst.getLogger().warning("Failed to read cat collar color: " + e.getMessage());
                }
                data.append("sitting:").append(cat.isSitting()).append(";");
            }
            else if (entity instanceof Wolf wolf)
            {
                data.append("collar:").append(wolf.getCollarColor().name()).append(";");
                data.append("angry:").append(wolf.isAngry()).append(";");
                data.append("sitting:").append(wolf.isSitting()).append(";");
            }
            else if (entity instanceof Parrot parrot)
            {
                data.append("parrot_variant:").append(parrot.getVariant().name()).append(";");
                data.append("sitting:").append(parrot.isSitting()).append(";");
            }
            else if (entity instanceof Rabbit rabbit)
            {
                data.append("rabbit_type:").append(rabbit.getRabbitType().name()).append(";");
            }
            else if (entity instanceof Fox fox)
            {
                data.append("fox_type:").append(fox.getFoxType().name()).append(";");
            }
            else if (entity instanceof Panda panda)
            {
                data.append("main_gene:").append(panda.getMainGene().name()).append(";");
                data.append("hidden_gene:").append(panda.getHiddenGene().name()).append(";");
            }
            else if (entity instanceof TropicalFish fish)
            {
                try
                {
                    data.append("fish_pattern:").append(fish.getPattern()).append(";");
                }
                catch (Exception e)
                {
                    data.append("fish_pattern:").append(fish.getPattern().name()).append(";");
                }
                try
                {
                    data.append("body_color:").append(fish.getBodyColor()).append(";");
                }
                catch (Exception e)
                {
                    data.append("body_color:").append(fish.getBodyColor().name()).append(";");
                }
                try
                {
                    data.append("pattern_color:").append(fish.getPatternColor()).append(";");
                }
                catch (Exception e)
                {
                    data.append("pattern_color:").append(fish.getPatternColor().name()).append(";");
                }
            }
            else if (entity instanceof Axolotl axolotl)
            {
                try
                {
                    data.append("axolotl_variant:").append(axolotl.getVariant()).append(";");
                }
                catch (Exception e)
                {
                    data.append("axolotl_variant:").append(axolotl.getVariant().name()).append(";");
                }
            }
            else if (entity instanceof Frog frog)
            {
                try
                {
                    data.append("frog_variant:").append(frog.getVariant().getKey().getKey()).append(";");
                }
                catch (Exception e)
                {
                    Main.inst.getLogger().warning("Failed to read frog variant: " + e.getMessage());
                }
            }
            else if (entity instanceof Llama llama)
            {
                data.append("llama_color:").append(llama.getColor().name()).append(";");
            }

            if (data.length() > 0)
            {
                mob.variantData = data.toString();
                if (Settings.debug)
                {
                    Main.inst.getLogger().info("Saved variant data: " + mob.variantData);
                }
            }
        }
        catch (Exception e)
        {
            Main.inst.getLogger().warning("Failed to save variant data: " + e.getMessage());
        }
    }

}
