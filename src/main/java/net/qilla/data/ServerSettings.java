package net.qilla.data;

import org.jetbrains.annotations.NotNull;

public class ServerSettings {

    private static ServerSettings INSTANCE;

    private boolean whitelistEnabled;
    private int maxPlayers;

    public static @NotNull ServerSettings setInstance(@NotNull ServerSettings settings) {
        return INSTANCE = settings;
    }

    public static @NotNull ServerSettings createNew() {
        return INSTANCE = new ServerSettings();
    }

    public static @NotNull ServerSettings getInstance() {
        if(INSTANCE == null) INSTANCE = new ServerSettings();
        return INSTANCE;
    }

    private ServerSettings() {
        this.whitelistEnabled = true;
        this.maxPlayers = 32;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setWhitelist(boolean whitelistEnabled) {
        this.whitelistEnabled = whitelistEnabled;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
