package net.qilla.data;

import net.qilla.file.PlayerDataFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PDRegistry {

    private static PDRegistry INSTANCE;

    private final PlayerDataFile playerDataFile = PlayerDataFile.getInstance();
    private final Map<UUID, PlayerData> persistentData = new ConcurrentHashMap<>();

    private PDRegistry() {
    }

    public static @NotNull PDRegistry getInstance() {
        if(INSTANCE == null) INSTANCE = new PDRegistry();
        return INSTANCE;
    }

    public @NotNull PlayerData get(UUID uuid) {
        return persistentData.compute(uuid, (k, v) -> {
            if(v == null) v = new PlayerData(uuid);
            return v;
        });
    }

    public void set(List<PlayerData> list) {
        this.clear();
        for(PlayerData data : list) {
            persistentData.put(data.getUuid(), data);
        }
    }

    public @NotNull List<PlayerData> getList() {
        return List.copyOf(persistentData.values());
    }

    public void clear() {
        persistentData.clear();
    }
}