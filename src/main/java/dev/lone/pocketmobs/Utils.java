package dev.lone.pocketmobs;

import org.bukkit.entity.Entity;

import java.util.concurrent.ThreadLocalRandom;

public class Utils
{
    // ThreadLocalRandom avoids sharing a mutable Random across threads.
    public static int getRandomInt(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int parseInt(String number, int defaultValue)
    {
        try
        {
            return Integer.parseInt(number);
        }
        catch (Exception ignored) { }
        return defaultValue;
    }

    /**
     * Hides {@code entity} from every client for the rest of its life.
     * <p>
     * Uses Paper's server-tracker visibility flag
     * ({@link Entity#setVisibleByDefault(boolean)}) instead of a one-shot packet.
     * The entity tracker consults this flag <em>before</em> it ever sends a spawn
     * packet, so the entity is suppressed for all players — including those who
     * enter tracking range later while it is moving — with no ProtocolLib
     * dependency and no spawn/destroy ordering race. The entity stays live
     * server-side, so projectile hit detection ({@code ProjectileHitEvent}) is
     * unaffected. Safe to call on the region thread that just spawned the entity,
     * which is exactly where the ball projectiles are launched (Folia).
     */
    public static void hideEntity(Entity entity)
    {
        if (entity == null)
        {
            return;
        }
        entity.setVisibleByDefault(false);
    }
}
