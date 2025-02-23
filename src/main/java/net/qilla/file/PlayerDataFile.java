package net.qilla.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class PlayerDataFile {

    private static PlayerDataFile INSTANCE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<PlayerData>() {
    }.getType();
    private static final String DEFAULT_FILE = "player_data.json";
    private static final Path DIRECTORY = Path.of("player_data");
    private static final Path OLD_DIRECTORY = DIRECTORY.resolve("old");
    private final Logger logger = MinecraftServer.LOGGER;

    public static PlayerDataFile getInstance() {
        if(INSTANCE == null) INSTANCE = new PlayerDataFile();
        return new PlayerDataFile();
    }

    public PlayerDataFile() {
    }

    public void load(@NotNull UUID uuid) {
        logger.info("Loading PlayerData file for {}...", uuid);
        final Path filePath = DIRECTORY.resolve(uuid + ".json");

        try(BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            PlayerData playerData = GSON.fromJson(reader, TYPE);
            PDRegistry.getInstance().set(playerData);
        } catch(IOException e) {
            this.reset(uuid);
            logger.error("There was a problem loading '{}', now creating new file.", filePath, e);
        }
        logger.info("PlayerData for {} has been successfully loaded!", uuid);
    }

    private void createFile(@NotNull UUID uuid) {
        final Path filePath = DIRECTORY.resolve(uuid + ".json");

        if(Files.exists(filePath)) return;

        logger.info("PlayerData for {} does not exist, creating new.", uuid);
        try {
            Files.createDirectories(filePath.getParent());
            URL resourceURL = getClass().getClassLoader().getResource(DEFAULT_FILE);
            if(resourceURL == null) throw new FileNotFoundException("Uh oh... default resource does not exist:\n" + DEFAULT_FILE);

            try(InputStream inputStream = resourceURL.openStream()) {
                Files.copy(inputStream, filePath);
            }
        } catch(IOException e) {
            logger.error("Failed to create {}", filePath, e);
        }
    }

    public void reset(@NotNull UUID uuid) {
        final Path filePath = DIRECTORY.resolve(uuid + ".json");
        try {
            if(Files.exists(filePath)) {
                Files.move(filePath, OLD_DIRECTORY, StandardCopyOption.REPLACE_EXISTING);
            }
            this.createFile(uuid);
        } catch(IOException e) {
            logger.error("Failed to reset PlayerData for {}", uuid, e);
        }
    }

    public void save(@NotNull PlayerData playerData) {
        final Path filePath = DIRECTORY.resolve(playerData.getUUID() + ".json");
        if(!Files.exists(filePath)) this.createFile(playerData.getUUID());

        final UUID uuid = playerData.getUUID();
        try(BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            GSON.toJson(playerData, TYPE, writer);
            logger.info("Successfully saved PlayerData for {}!", uuid);
        } catch(IOException e) {
            logger.error("Failed to save {}'s PlayerData", uuid, e);
        }
    }

    public boolean exists(@NotNull UUID uuid) {
        final Path filePath = DIRECTORY.resolve(uuid + ".json");
        return Files.exists(filePath);
    }
}