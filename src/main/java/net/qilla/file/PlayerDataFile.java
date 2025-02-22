package net.qilla.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class PlayerDataFile {

    private static PlayerDataFile INSTANCE;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type type = new TypeToken<List<PlayerData>>() {
    }.getType();
    private final String defaultLoc = "player_data.json";
    private final Path filePath = Path.of("player_data.json");
    private final Logger logger = MinecraftServer.LOGGER;

    public static PlayerDataFile getInstance() {
        if(INSTANCE == null) INSTANCE = new PlayerDataFile();
        return new PlayerDataFile();
    }

    public PlayerDataFile() {
    }

    public void load() {
        logger.info("Loading player permissions file from '{}'...", filePath);

        this.ensureFileExists();
        try(BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            List<PlayerData> list = gson.fromJson(reader, type);
            PDRegistry.getInstance().set(list);
        } catch(IOException e) {
            this.resetFile();
            logger.error("There was a problem loading '{}'. File will be reset!", filePath, e);
        }
        logger.info("Player permissions have been successfully loaded!");
    }

    public void clear() {
        PDRegistry.getInstance().clear();
    }

    private void ensureFileExists() {
        if(Files.exists(filePath)) return;

        logger.info("{} does not exist, creating new file.", filePath);
        try {
            URL resourceURL = getClass().getClassLoader().getResource(defaultLoc);
            if(resourceURL == null) throw new FileNotFoundException("Default resource not found: " + defaultLoc);

            try(InputStream inputStream = resourceURL.openStream()) {
                Files.copy(inputStream, filePath);
            }

        } catch(IOException e) {
            logger.error("Failed to create file at {}", filePath, e);
        }
    }

    public void resetFile() {
        try {
            if(Files.exists(filePath)) {
                Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".old");
                Files.move(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }
            ensureFileExists();
        } catch(IOException e) {
            logger.error("Failed to reset file at {}", filePath, e);
        }
    }

    public void save() {
        try(BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            List<PlayerData> registryList = PDRegistry.getInstance().getList();

            gson.toJson(registryList, type, writer);
            logger.info("Successfully saved data to {}", filePath);
        } catch(IOException e) {
            logger.error("Failed to save file in {}", filePath, e);
        }
    }
}