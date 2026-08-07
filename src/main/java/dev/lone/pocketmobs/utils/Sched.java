package dev.lone.pocketmobs.utils;

import dev.lone.pocketmobs.Main;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Thin wrapper over Paper's region-thread schedulers.
 * <p>
 * These APIs ship with paper-api and are implemented on both ordinary Paper
 * (everything runs on the main thread) and Folia (tasks are routed to the
 * owning region/entity thread). Routing every scheduled action through here
 * keeps the rest of the plugin free of the legacy {@code Bukkit.getScheduler()}
 * calls that throw {@code UnsupportedOperationException} on Folia.
 */
public final class Sched
{
    private Sched()
    {
    }

    /** Run a one-shot task on the global region (next tick). */
    public static void runGlobal(Runnable task)
    {
        Bukkit.getGlobalRegionScheduler().run(Main.inst, t -> task.run());
    }

    /**
     * Run a repeating task on the global region.
     *
     * @return the scheduled task handle, so callers can cancel it on disable.
     */
    public static ScheduledTask runGlobalTimer(Runnable task, long delayTicks, long periodTicks)
    {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.inst, t -> task.run(), delayTicks, periodTicks);
    }

    /**
     * Run a task on the thread that owns {@code entity}.
     *
     * @param run     executed if the entity is still alive on its owning thread
     * @param retired executed instead if the entity has been removed (may be null)
     */
    public static void runAtEntity(Entity entity, Runnable run, Runnable retired)
    {
        entity.getScheduler().execute(Main.inst, run, retired, 0L);
    }

    /** Run a task on the thread that owns the region containing {@code location}. */
    public static void runAtRegion(Location location, Runnable task)
    {
        Bukkit.getRegionScheduler().run(Main.inst, location, t -> task.run());
    }
}
