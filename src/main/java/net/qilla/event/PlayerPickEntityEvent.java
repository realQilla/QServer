package net.qilla.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPickEntityEvent implements PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private Entity entity;
    private boolean cancelled;

    public PlayerPickEntityEvent(@NotNull Player player, @NotNull Entity entity) {
        this.player = player;
        this.entity = entity;
    }

    public @NotNull Entity getClickedEntity() {
        return entity;
    }

    public void setClickedEntity(@NotNull Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
