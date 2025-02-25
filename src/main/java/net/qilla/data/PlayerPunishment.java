package net.qilla.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public final class PlayerPunishment {

    public static final long PERMANENT = -1;
    public static final long KICK_LENGTH = 15000;

    private final PunishmentType type;
    private final long on;
    private final UUID by;
    private final String reason;
    private long expiration;
    private boolean forceExpired;

    public PlayerPunishment(@NotNull PunishmentType type, long on, @Nullable UUID by, @NotNull String reason, long expiration) {
        this.type = type;
        this.on = on;
        this.by = by;
        this.reason = reason;
        this.expiration = expiration < 0 ? -1 : Math.max(System.currentTimeMillis(), expiration);
    }

    public @NotNull PunishmentType getType() {
        return type;
    }

    public long getOn() {
        return on;
    }

    public @Nullable UUID getBy() {
        return by;
    }

    public @NotNull String getReason() {
        return reason;
    }

    public long getExpiration() {
        return expiration;
    }

    public boolean isPermanent() {
        return expiration == -1;
    }

    public boolean isExpired() {
        return !this.isPermanent() && System.currentTimeMillis() > expiration;
    }

    public boolean isForceExpired() {
        return forceExpired;
    }

    public void setExpiration(long expiration) {
        this.expiration = Math.max(System.currentTimeMillis(), expiration);
    }

    public void setExpired() {
        this.expiration = System.currentTimeMillis();
        this.forceExpired = true;
    }

    public enum PunishmentType {
        MUTE,
        KICK,
        BLACKLIST,
    }
}