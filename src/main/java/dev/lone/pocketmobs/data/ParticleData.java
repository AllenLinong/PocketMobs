package dev.lone.pocketmobs.data;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class ParticleData
{
    Particle particle;
    int amount;
    Color color;
    float size;
    float offsetX;
    float offsetY;
    float offsetZ;

    public ParticleData(String particleType, int amount) throws IllegalArgumentException
    {
        this.particle = Particle.valueOf(particleType.toUpperCase());
        this.amount = amount;
        this.color = null;
        this.size = 1.0f;
        this.offsetX = 0;
        this.offsetY = 0;
        this.offsetZ = 0;
    }

    public ParticleData(String particleType, int amount, String hexColor) throws IllegalArgumentException
    {
        this.particle = Particle.valueOf(particleType.toUpperCase());
        this.amount = amount;
        this.color = parseColor(hexColor);
        this.size = 1.0f;
        this.offsetX = 0;
        this.offsetY = 0;
        this.offsetZ = 0;
    }

    private Color parseColor(String hexColor)
    {
        if (hexColor == null || hexColor.isEmpty())
        {
            return null;
        }
        
        try
        {
            String hex = hexColor.replace("#", "").replace("0x", "");
            if (hex.length() == 6)
            {
                int red = Integer.parseInt(hex.substring(0, 2), 16);
                int green = Integer.parseInt(hex.substring(2, 4), 16);
                int blue = Integer.parseInt(hex.substring(4, 6), 16);
                // 验证颜色值范围
                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));
                return Color.fromRGB(red, green, blue);
            }
            else if (hex.length() == 8)
            {
                int alpha = Integer.parseInt(hex.substring(0, 2), 16);
                int red = Integer.parseInt(hex.substring(2, 4), 16);
                int green = Integer.parseInt(hex.substring(4, 6), 16);
                int blue = Integer.parseInt(hex.substring(6, 8), 16);
                // 验证颜色值范围
                alpha = Math.max(0, Math.min(255, alpha));
                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));
                return Color.fromARGB(alpha, red, green, blue);
            }
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        return null;
    }

    public void spawn(Location location)
    {
        World world = location.getWorld();
        if (world == null) return;
        
        if (hasColor())
        {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);

            world.spawnParticle(Particle.DUST, location, amount, offsetX, offsetY, offsetZ, dustOptions);
        }
        else if (particle.getDataType() == Void.class)
        {
            world.spawnParticle(particle, location, amount, offsetX, offsetY, offsetZ, 0);
        }
        // A data-requiring particle (DUST, BLOCK, ITEM, ...) configured without a color
        // has no data object to pass and would throw on 1.21.4 — skip it instead of crashing.
    }

    public boolean hasColor()
    {
        return color != null;
    }
}
