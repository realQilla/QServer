package net.qilla.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EntityStatuses;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.network.packet.server.play.DeathCombatEventPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import net.qilla.data.PlayerPunishment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public final class QPlayer extends Player {

    private final PlayerData playerData;

    public QPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);

        this.playerData = PDRegistry.getInstance().get(super.getUuid());

        super.setRespawnPoint(new Pos(0, 153, 0));
        super.setPermissionLevel(playerData.getPermissionLevel());
    }

    @Override
    public void kill() {
        if(!isDead()) {
            Component deathText;
            Component chatMessage;

            {
                if(lastDamage != null) {
                    deathText = lastDamage.buildDeathScreenText(this);
                } else {
                    deathText = Component.empty();
                }
            }

            {
                if(lastDamage != null) {
                    chatMessage = lastDamage.buildDeathMessage(this);
                } else {
                    chatMessage = Component.empty();
                }
            }

            PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent(this, deathText, chatMessage);
            EventDispatcher.call(playerDeathEvent);

            deathText = playerDeathEvent.getDeathText();

            if(deathText != null) {
                sendPacket(new DeathCombatEventPacket(getEntityId(), deathText));
            }

            if(super.getInstance() != null)
                setDeathLocation(super.getInstance().getDimensionName(), getPosition());
        }

        {
            super.refreshIsDead(true);
            super.triggerStatus((byte) EntityStatuses.LivingEntity.PLAY_DEATH_SOUND);
            super.setPose(EntityPose.DYING);
            super.setHealth(0);

            this.velocity = Vec.ZERO;

            if(hasPassenger()) {
                getPassengers().forEach(this::removePassenger);
            }

            EntityDeathEvent entityDeathEvent = new EntityDeathEvent(this);
            EventDispatcher.call(entityDeathEvent);
        }

        super.respawn();
    }

    @Override
    public void kick(@NotNull Component component) {
        this.kick(null, "");
    }

    public void kick(@Nullable Player kickedBy, @NotNull String reason) {
        UUID kickedByUuid = kickedBy == null ? null : kickedBy.getUuid();

        PlayerPunishment punishment = playerData.setKicked(kickedByUuid, reason, System.currentTimeMillis());
        super.getPlayerConnection().kick(PlayerPunishment.initialMsg(punishment));
    }

    public void blacklist(@Nullable Player blacklistedBy, @NotNull String reason, long expiration) {
        UUID blacklistedByUUID = blacklistedBy == null ? null : blacklistedBy.getUuid();

        PlayerPunishment punishment = playerData.setBlackListed(blacklistedByUUID, reason, System.currentTimeMillis(), expiration);
        super.getPlayerConnection().kick(PlayerPunishment.initialMsg(punishment));
    }

    public @NotNull PlayerData getData() {
        return playerData;
    }
}