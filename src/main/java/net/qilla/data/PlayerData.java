package net.qilla.data;

import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerData {

    private static final int SKIN_UPDATE_INTERVAL = 120;

    private final UUID uuid;
    private int permissionLevel;
    private PlayerSkin skin;
    private long lastSkinUpdate;

    public PlayerData(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.permissionLevel = 0;
        this.skin = null;
        this.lastSkinUpdate = 0;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public PlayerSkin getSkin() {
        if(System.currentTimeMillis() - lastSkinUpdate > (1000L * SKIN_UPDATE_INTERVAL)) {
            this.skin = PlayerSkin.fromUuid(String.valueOf(uuid));
            this.lastSkinUpdate = System.currentTimeMillis();
        }
        return skin;
    }

    public void setSkin(PlayerSkin skin) {
        this.skin = skin;
        this.lastSkinUpdate = System.currentTimeMillis();
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
}