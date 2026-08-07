package dev.lone.pocketmobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProtectionUtils
{
    // Fire the cancellable event and honour the verdict. Firing a Bukkit event is
    // cheap, so there is no cache: any protection plugin that decides on live state
    // (sneaking, region flags, claim/time changes) always sees a fresh answer.
    public static boolean canCatchMob(@NotNull Player player, @NotNull Entity target, @NotNull Location location)
    {
        MobCatchEvent catchEvent = new MobCatchEvent(player, target, location);
        Bukkit.getPluginManager().callEvent(catchEvent);
        return !catchEvent.isCancelled();
    }

    public static boolean canReleaseMob(@NotNull Player player, @NotNull Location location)
    {
        MobReleaseEvent releaseEvent = new MobReleaseEvent(player, location);
        Bukkit.getPluginManager().callEvent(releaseEvent);
        return !releaseEvent.isCancelled();
    }

    public static class MobCatchEvent extends Event implements Cancellable
    {
        private static final HandlerList handlers = new HandlerList();
        private boolean cancelled;

        private final Player player;
        private final Entity target;
        private final Location location;

        public MobCatchEvent(@NotNull Player player, @NotNull Entity target, @NotNull Location location)
        {
            this.player = player;
            this.target = target;
            this.location = location;
        }

        @NotNull
        public Player getPlayer()
        {
            return player;
        }

        @NotNull
        public Entity getTarget()
        {
            return target;
        }

        @NotNull
        public Location getLocation()
        {
            return location;
        }

        @Override
        public boolean isCancelled()
        {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel)
        {
            this.cancelled = cancel;
        }

        @NotNull
        @Override
        public HandlerList getHandlers()
        {
            return handlers;
        }

        @NotNull
        public static HandlerList getHandlerList()
        {
            return handlers;
        }
    }

    public static class MobReleaseEvent extends Event implements Cancellable
    {
        private static final HandlerList handlers = new HandlerList();
        private boolean cancelled;

        private final Player player;
        private final Location location;

        public MobReleaseEvent(@NotNull Player player, @NotNull Location location)
        {
            this.player = player;
            this.location = location;
        }

        @NotNull
        public Player getPlayer()
        {
            return player;
        }

        @NotNull
        public Location getLocation()
        {
            return location;
        }

        @Override
        public boolean isCancelled()
        {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel)
        {
            this.cancelled = cancel;
        }

        @NotNull
        @Override
        public HandlerList getHandlers()
        {
            return handlers;
        }

        @NotNull
        public static HandlerList getHandlerList()
        {
            return handlers;
        }
    }

}
