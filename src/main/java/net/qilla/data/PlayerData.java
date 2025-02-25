package net.qilla.data;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.player.GameProfile;
import net.qilla.file.PlayerDataFile;
import net.qilla.file.QFiles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public final class PlayerData {

    private static final int INFO_UPDATE_INTERVAL = 120 * 1000;
    private static final PlayerDataFile PLAYER_DATA_FILE = QFiles.PLAYER_DATA;

    private final UUID uuid;
    private final String username;
    private volatile String displayName;
    private volatile int permissionLevel;
    private volatile PlayerSkin skin;
    private volatile long lastSkinUpdate;
    private volatile long lastLogin;
    private volatile long lastLogout;
    private volatile boolean whitelisted;
    private final List<PlayerPunishment> activePunishments;
    private final List<PlayerPunishment> priorPunishments;

    public PlayerData(@NotNull GameProfile gameProfile) {
        this.uuid = gameProfile.uuid();
        this.username = gameProfile.name();
        this.displayName = this.username;
        this.permissionLevel = 0;
        this.skin = this.getSkin();
        this.lastSkinUpdate = 0;
        this.lastLogin = 0;
        this.lastLogout = 0;
        this.activePunishments = new ArrayList<>();
        this.priorPunishments = new ArrayList<>();
    }

    public PlayerData(@NotNull PlayerData playerData, @NotNull GameProfile gameProfile) {
        this.uuid = gameProfile.uuid();
        this.username = gameProfile.name();
        this.displayName = playerData.displayName;
        this.permissionLevel = playerData.permissionLevel;
        this.skin = this.getSkin();
        this.lastSkinUpdate = playerData.lastSkinUpdate;
        this.lastLogin = playerData.lastLogin;
        this.lastLogout = playerData.lastLogout;
        this.activePunishments = playerData.activePunishments;
        this.priorPunishments = playerData.priorPunishments;
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public @NotNull String getDisplayName() {
        return this.displayName;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public @NotNull PlayerSkin getSkin() {
        if(lastSkinUpdate != -1) {
            if(System.currentTimeMillis() - lastSkinUpdate > INFO_UPDATE_INTERVAL) {
                skin = PlayerSkin.fromUuid(String.valueOf(uuid));
                lastSkinUpdate = System.currentTimeMillis();
            }
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

    public List<PlayerPunishment> getPunishments() {
        synchronized(priorPunishments) {
            return activePunishments;
        }
    }

    public boolean hasPunishments() {
        synchronized(activePunishments) {
            return !activePunishments.isEmpty();
        }
    }

    public @Nullable PlayerPunishment lookupActive(PlayerPunishment.PunishmentType... type) {
        final Set<PlayerPunishment.PunishmentType> typeSet = new HashSet<>(Arrays.asList(type));

        synchronized(activePunishments) {
            Iterator<PlayerPunishment> iterator = activePunishments.iterator();

            while(iterator.hasNext()) {
                PlayerPunishment punishment = iterator.next();

                if(!punishment.isPermanent() && punishment.isExpired()) {
                    this.movePunishment(punishment);
                    iterator.remove();
                } else if(typeSet.contains(punishment.getType())) {
                    return punishment;
                }
            }
        }
        return null;
    }

    public @NotNull List<PlayerPunishment> lookupAllActive() {
        List<PlayerPunishment> currentPunishments = new ArrayList<>();

        synchronized(activePunishments) {
            Iterator<PlayerPunishment> iterator = activePunishments.iterator();

            while(iterator.hasNext()) {
                PlayerPunishment punishment = iterator.next();

                if(!punishment.isPermanent() && punishment.isExpired()) {
                    this.movePunishment(punishment);
                    iterator.remove();
                } else currentPunishments.add(punishment);
            }
        }
        return currentPunishments;
    }

    private void movePunishment(@NotNull PlayerPunishment punishment) {
        synchronized(priorPunishments) {
            priorPunishments.add(punishment);
        }
    }

    public synchronized void setSkin(@Nullable PlayerSkin skin) {
        this.skin = skin;
        lastSkinUpdate = System.currentTimeMillis();
    }

    public synchronized void lockSkin(@Nullable PlayerSkin skin) {
        this.skin = skin;
        lastSkinUpdate = -1;
    }

    public synchronized void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public synchronized void updateLastLogin() {
        lastLogin = System.currentTimeMillis();
    }

    public synchronized void updateLastLogout() {
        lastLogout = System.currentTimeMillis();
    }

    public synchronized void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public @NotNull PlayerPunishment setMuted(@Nullable UUID by, @NotNull String reason, long on, long expiration) {
        PlayerPunishment punishment = new PlayerPunishment(
                PlayerPunishment.PunishmentType.MUTE,
                on, by, reason, expiration);
        synchronized(activePunishments) {
            activePunishments.add(punishment);
        }
        PLAYER_DATA_FILE.save(this);
        return punishment;
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

    public void saveData() {
        PLAYER_DATA_FILE.save(this);
    }
}