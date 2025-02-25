package net.qilla.entity;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.time.TimeUnit;
import net.qilla.player.QPlayer;
import org.jetbrains.annotations.NotNull;

public final class QItemEntity extends ItemEntity {

    private static final int DESPAWN_TIME = 18000;
    private int flashRate = 80;
    private int lifetime = 0;

    public QItemEntity(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    public QItemEntity(@NotNull ItemStack itemStack, @NotNull QPlayer player) {
        this(itemStack);

        super.setVelocity(player.getPosition().direction().mul(5));
        super.setPickupDelay(10, TimeUnit.SERVER_TICK);
    }

    @Override
    public void tick(long time) {
        if (lifetime >= DESPAWN_TIME) {
            super.remove();
            return;
        }
        if((lifetime > DESPAWN_TIME / 8) && lifetime % flashRate == 0) {
            super.setGlowing(!super.isGlowing());
            if(flashRate > 10) flashRate = Math.max(10, flashRate - 5);
        }
        super.tick(time);

        lifetime++;
    }
}