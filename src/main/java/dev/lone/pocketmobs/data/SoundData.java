package dev.lone.pocketmobs.data;

import dev.lone.pocketmobs.Main;
import org.bukkit.Sound;

public class SoundData
{
    private static final float MIN_VOLUME = 0.0F;
    private static final float MAX_VOLUME = 10.0F;
    private static final float MIN_PITCH = 0.5F;
    private static final float MAX_PITCH = 2.0F;
    
    public Sound sound;
    public float volume;
    public float pitch;

    public SoundData(String name, float volume, float pitch)
    {
        try
        {
            this.sound = Sound.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            Main.inst.getLogger().warning("Invalid sound name '" + name + "', using the default sound instead.");
            this.sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
        this.volume = Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, volume));
        this.pitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, pitch));
    }
}
