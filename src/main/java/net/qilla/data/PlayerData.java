package net.qilla.data;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.mojang.MojangUtils;
import net.qilla.file.PlayerDataFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public final class PlayerData {

    private static final int INFO_UPDATE_INTERVAL = 120 * 1000;
    private static final PlayerDataFile PLAYER_DATA_FILE = PlayerDataFile.getInstance();

    private final UUID uuid;
    private volatile int permissionLevel;
    private volatile PlayerSkin skin;
    private volatile String username;
    private volatile long lastUsernameUpdate;
    private volatile long lastSkinUpdate;
    private volatile long lastLogin;
    private volatile long lastLogout;
    private volatile boolean whitelisted;
    private final Set<PlayerPunishment> activePunishments;
    private final Set<PlayerPunishment> priorPunishments;

    public PlayerData(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.permissionLevel = 0;
        this.skin = null;
        this.lastSkinUpdate = 0;
        this.lastLogin = 0;
        this.lastLogout = 0;
        this.activePunishments = new HashSet<>();
        this.priorPunishments = new HashSet<>();
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public @NotNull String getUsername() {
        if(System.currentTimeMillis() - lastUsernameUpdate > INFO_UPDATE_INTERVAL) {
            try {
                username = MojangUtils.getUsername(uuid);
            } catch(IOException ignore) {
                username = uuid.toString();
            }
            lastUsernameUpdate = System.currentTimeMillis();
        }
        return username;
    }

    public @NotNull PlayerSkin getSkin() {
        if(System.currentTimeMillis() - lastSkinUpdate > INFO_UPDATE_INTERVAL) {
            skin = PlayerSkin.fromUuid(String.valueOf(uuid));
            lastSkinUpdate = System.currentTimeMillis();
        }
        return skin;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public long getLastLogout() {
        return lastLogout;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }

    public Set<PlayerPunishment> getPunishments() {
        synchronized(priorPunishments) {
            return activePunishments;
        }
    }

    public boolean hasPunishments() {
        synchronized(activePunishments) {
            return !activePunishments.isEmpty();
        }
    }

    public @Nullable PlayerPunishment lookupActive() {
        synchronized(activePunishments) {
            if(activePunishments.isEmpty()) return null;

            for(PlayerPunishment punishment : activePunishments) {
                if(!punishment.isPermanent() && punishment.isExpired()) {
                    synchronized(priorPunishments) {
                        priorPunishments.add(punishment);
                    }
                    activePunishments.remove(punishment);
                } else return punishment;
            }
            return null;
        }
    }

    public @NotNull List<PlayerPunishment> getAllActive() {
        List<PlayerPunishment> activePunishments = new ArrayList<>();

        for(PlayerPunishment punishment : this.activePunishments) {
            if(!punishment.isPermanent() && punishment.isExpired()) {
                synchronized(priorPunishments) {
                    priorPunishments.add(punishment);
                }
                activePunishments.remove(punishment);
            } else activePunishments.add(punishment);
        }
        return activePunishments;
    }

    public synchronized void setSkin(PlayerSkin skin) {
        this.skin = skin;
        lastSkinUpdate = System.currentTimeMillis();
        PLAYER_DATA_FILE.save(this);
    }

    public synchronized void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
        PLAYER_DATA_FILE.save(this);
    }

    public synchronized void updateLastLogin() {
        lastLogin = System.currentTimeMillis();
        PLAYER_DATA_FILE.save(this);
    }

    public synchronized void updateLastLogout() {
        lastLogout = System.currentTimeMillis();
        PLAYER_DATA_FILE.save(this);
    }

    public synchronized void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
        PLAYER_DATA_FILE.save(this);
    }

    public void setMuted(@Nullable UUID by, @NotNull String reason, long on, long expiration) {
        synchronized(activePunishments) {
            activePunishments.add(new PlayerPunishment(PlayerPunishment.PunishmentType.MUTE,
                    on, by, reason, expiration));
        }
        PLAYER_DATA_FILE.save(this);
    }

    public void setMuted(@Nullable UUID by, @NotNull String reason, long on) {
        synchronized(activePunishments) {
            activePunishments.add(new PlayerPunishment(PlayerPunishment.PunishmentType.MUTE,
                    on, by, reason, PlayerPunishment.PERMANENT));
        }
        PLAYER_DATA_FILE.save(this);
    }

    public @NotNull PlayerPunishment setKicked(@Nullable UUID by, @NotNull String reason, long on) {
        PlayerPunishment punishment = new PlayerPunishment(
                PlayerPunishment.PunishmentType.KICK, on, by, reason,
                System.currentTimeMillis() + PlayerPunishment.KICK_LENGTH);
        synchronized(activePunishments) {
            activePunishments.add(punishment);
        }
        PLAYER_DATA_FILE.save(this);
        return punishment;
    }

    public @NotNull PlayerPunishment setBlackListed(@Nullable UUID by, @NotNull String reason, long on, long expiration) {
        PlayerPunishment punishment = new PlayerPunishment(PlayerPunishment.PunishmentType.BLACKLIST,
                on, by, reason, expiration);
        synchronized(activePunishments) {
            activePunishments.add(punishment);
        }
        PLAYER_DATA_FILE.save(this);
        return punishment;
    }
}