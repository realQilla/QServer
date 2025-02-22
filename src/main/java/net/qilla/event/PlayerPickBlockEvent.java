package net.qilla.event;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class PlayerPickBlockEvent implements PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private Block block;
    private Material material;
    private boolean cancelled;

    public PlayerPickBlockEvent(@NotNull Player player, @NotNull Block block, @NotNull Material material) {
        this.player = player;
        this.material = material;
        this.block = block;
    }

    public @NotNull Block getBlock() {
        return block;
    }

    public Material getMaterial() {
        return material;
    }

    public void setBlock(@NotNull Block block) {
        this.block = block;
    }

    public void setMaterial(@NotNull Material material) {
        this.material = material;
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