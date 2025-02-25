package net.qilla.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EntityStatuses;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeathCombatEventPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import net.qilla.data.PlayerPunishment;
import net.qilla.entity.QItemEntity;
import net.qilla.util.MsgFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public final class QPlayer extends Player {

    private static final PDRegistry PD_REGISTRY = PDRegistry.getInstance();
    private final PlayerData playerData;

    public QPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);

        this.playerData = PD_REGISTRY.get(super.getUuid());

        super.setRespawnPoint(new Pos(0, 153, 0));
        super.setPermissionLevel(playerData.getPermissionLevel());
    }

    public Pos getEyePos() {
        return super.getPosition().add(0, super.getEyeHeight(), 0);
    }

    @Override
    public void kill() {
        super.kill();
        super.respawn();
    }

    @Override
    public boolean dropItem(@NotNull ItemStack item) {
        if (item.isAir()) return false;
        ItemDropEvent itemDropEvent = new ItemDropEvent(this, item);
        EventDispatcher.call(itemDropEvent);

        if(itemDropEvent.isCancelled()) return false;

        QItemEntity itemEntity = new QItemEntity(item, this);
        itemEntity.setInstance(super.getInstance(), this.getEyePos().sub(0, 0.3, 0));

        return true;
    }

    public void kick(@Nullable Player kickedBy, @NotNull String reason) {
        UUID kickedByUuid = kickedBy == null ? null : kickedBy.getUuid();
        PlayerPunishment punishment = playerData.setKicked(kickedByUuid, reason, System.currentTimeMillis());

        super.getPlayerConnection().kick(MsgFormat.initialKick(punishment));
    }

    public void blacklist(@Nullable Player blacklistedBy, @NotNull String reason, long expiration) {
        UUID blacklistedByUUID = blacklistedBy == null ? null : blacklistedBy.getUuid();
        PlayerPunishment punishment = playerData.setBlackListed(blacklistedByUUID, reason, System.currentTimeMillis(), expiration);
        Component msg = punishment.isPermanent() ? MsgFormat.initialBlacklistPerm(punishment) : MsgFormat.initialBlacklist(punishment);

        super.getPlayerConnection().kick(msg);
    }

    public void mute(@Nullable Player mutedBy, @NotNull String reason, long expiration) {
        UUID mutedByUUID = mutedBy == null ? null : mutedBy.getUuid();
        PlayerPunishment punishment = playerData.setMuted(mutedByUUID, reason, System.currentTimeMillis(), expiration);
        Component msg = punishment.isPermanent() ? MsgFormat.initialMutePerm(punishment) : MsgFormat.initialMute(punishment);

        super.sendMessage(msg);
    }

    public void addWhitelist() {
        if(playerData.isWhitelisted()) return;

        playerData.setWhitelisted(true);
        super.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been added to the server whitelist!"));
    }

    public void removeWhitelist() {
        if(!playerData.isWhitelisted()) return;

        playerData.setWhitelisted(false);
        super.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been removed from the server whitelist!"));
    }

    public boolean isOnWhitelist() {
        return playerData.isWhitelisted();
    }

    public @NotNull PlayerData getData() {
        return playerData;
    }

    @Override
    public void setSkin(@Nullable PlayerSkin skin) {
        playerData.setSkin(skin);
        super.setSkin(skin);
    }

    public void lockSkin(@Nullable PlayerSkin skin) {
        playerData.lockSkin(skin);
        super.setSkin(skin);
    }
}