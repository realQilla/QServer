package net.qilla.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PDRegistry {

    private static PDRegistry INSTANCE;

    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    private PDRegistry() {
    }

    public static @NotNull PDRegistry getInstance() {
        if(INSTANCE == null) INSTANCE = new PDRegistry();
        return INSTANCE;
    }

    public @NotNull PlayerData get(@NotNull UUID uuid) {
        return playerData.compute(uuid, (k, v) -> {
            if(v == null) v = new PlayerData(uuid);
            return v;
        });
    }

    public void set(@NotNull PlayerData playerData) {
        this.playerData.put(playerData.getUUID(), playerData);
    }

    public boolean has(UUID uuid) {
        return playerData.containsKey(uuid);
    }

    public @NotNull List<PlayerData> getAll() {
        return List.copyOf(playerData.values());
    }
}