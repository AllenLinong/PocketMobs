package dev.lone.pocketmobs.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HologramUtil
{
    private static final long MESSAGE_COOLDOWN_MS = 250L;
    private static final Map<UUID, Long> lastSentAt = new ConcurrentHashMap<>();

    static void spawn(Location location, String message, int duration)
    {
        if (location == null || location.getWorld() == null)
        {
            return;
        }
        
        location.getWorld().spawn(location, AreaEffectCloud.class, newEntity -> {
            newEntity.setParticle(Particle.END_ROD);
            newEntity.setRadius(0.0F);

            // 使用customName()方法设置自定义名称（1.21+）
            Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            newEntity.customName(component);
            newEntity.setCustomNameVisible(true);

            newEntity.setWaitTime(0);
            newEntity.setDuration(duration);
        });
    }

    public static void send(Player player, String message, int duration)
    {
        if (player == null || player.getWorld() == null)
        {
            return;
        }

        long now = System.currentTimeMillis();
        long lastSent = lastSentAt.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastSent < MESSAGE_COOLDOWN_MS)
        {
            return;
        }
        lastSentAt.put(player.getUniqueId(), now);
        // On Folia this can be invoked from another region's event thread (e.g. a
        // projectile-hit handler when the shooter moved regions). The eye-location read
        // and the AreaEffectCloud spawn must run on the player's own region thread.
        Sched.runAtEntity(player, () ->
                spawn(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(2)).add(0, -1.0, 0), message, duration), null);
    }

    public static void clearPlayerCooldown(UUID playerId)
    {
        if (playerId == null)
        {
            return;
        }
        lastSentAt.remove(playerId);
    }

    public static void clearAllCooldowns()
    {
        lastSentAt.clear();
    }
}
