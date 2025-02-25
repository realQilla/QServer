package net.qilla.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.GameProfile;
import net.qilla.data.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class PlayerDataFile {

    private final Logger logger = MinecraftServer.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = TypeToken.get(PlayerData.class).getType();
    private static final Path FILE_DIRECTORY = Path.of("player_data");
    private static final Path OLD_DIRECTORY = FILE_DIRECTORY.resolve("old");

    PlayerDataFile() {
    }

    public @Nullable PlayerData load(@NotNull GameProfile gameProfile) {
        UUID uuid = gameProfile.uuid();
        if(!this.exists(uuid)) return null;

        logger.info("Loading Player Data for {}...", uuid);
        final Path filePath = FILE_DIRECTORY.resolve(uuid + ".json");

        try(BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            try {
                PlayerData prevPlayerData = GSON.fromJson(reader, TYPE);

                return new PlayerData(prevPlayerData, gameProfile);
            } catch(JsonSyntaxException exception) {
                logger.error("There was a formatting problem when attempting to read {}'s Player Data! File will be reset.", uuid);
                Files.createDirectories(OLD_DIRECTORY);
                Files.move(filePath, OLD_DIRECTORY.resolve(uuid + ".json"), StandardCopyOption.REPLACE_EXISTING);
                PlayerData newPlayerData = new PlayerData(gameProfile);
                this.save(newPlayerData);
                return newPlayerData;
            }
        } catch(IOException exception) {
            logger.error("There was a problem loading {}'s Player Data!", uuid, exception);
        }
        return null;
    }

    public boolean save(@NotNull PlayerData playerData) {
        final UUID uuid = playerData.getUUID();
        final Path filePath = FILE_DIRECTORY.resolve(uuid + ".json");

        try {
            if(!Files.exists(filePath)) Files.createFile(filePath);

            try(BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                try {
                    GSON.toJson(playerData, TYPE, writer);
                } catch(JsonIOException exception) {
                    logger.error("There was a formatting problem when attempting to save {}'s Player Data!", uuid, exception);
                    return false;
                }
            }
            logger.info("Successfully saved PlayerData for {}!", uuid);
            return true;
        } catch(IOException exception) {
            logger.error("Failed to save {}'s Player Data", uuid, exception);
            return false;
        }
    }

    public boolean exists(@NotNull UUID uuid) {
        final Path filePath = FILE_DIRECTORY.resolve(uuid + ".json");
        return Files.exists(filePath);
    }
}