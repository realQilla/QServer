package net.qilla.data;

import net.qilla.file.QFiles;
import net.qilla.file.ServerSettingsFile;
import org.jetbrains.annotations.NotNull;

public class ServerSettings {

    private static final ServerSettingsFile ssFile = QFiles.SERVER_SETTINGS;

    private boolean whitelistEnabled;
    private int maxPlayers;
    private int chunkLoadDistance;

    public static @NotNull ServerSettings init() {
        if(!ssFile.exists()) ssFile.save(new ServerSettings());
        return ssFile.load();
    }

    private ServerSettings() {
        this.whitelistEnabled = true;
        this.maxPlayers = 32;
        this.chunkLoadDistance = 16;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getChunkLoadDistance() {
        return chunkLoadDistance;
    }

    public void setWhitelist(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setChunkLoadDistance(int chunkLoadDistance) {
        this.chunkLoadDistance = chunkLoadDistance;
    }

    public static ServerSettings getDefault() {
        return new ServerSettings();
    }
}