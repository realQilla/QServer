package net.qilla.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.qilla.data.ServerSettings;
import org.slf4j.Logger;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ServerSettingsFile {

    private static ServerSettingsFile INSTANCE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<ServerSettings>() {
    }.getType();
    private static final String DEFAULT_FILE = "server_settings.json";
    private static final Path FILE_DIRECTORY = Path.of("server_settings.json");
    private static final Path OLD_DIRECTORY = FILE_DIRECTORY.resolve("old");
    private final Logger logger = MinecraftServer.LOGGER;

    public static ServerSettingsFile getInstance() {
        if(INSTANCE == null) INSTANCE = new ServerSettingsFile();
        return new ServerSettingsFile();
    }

    public ServerSettingsFile() {
    }

    public void load() {
        logger.info("Loading Server Settings file ...");

        try(BufferedReader reader = Files.newBufferedReader(FILE_DIRECTORY, StandardCharsets.UTF_8)) {
            ServerSettings serverSettings = GSON.fromJson(reader, TYPE);
            ServerSettings.setInstance(serverSettings);
        } catch(IOException e) {
            this.reset();
            logger.error("There was a problem loading the Server Settings, now creating new file.", e);
        }
        logger.info("Server Settings have been successfully loaded!");
    }

    private void createFile() {

        if(Files.exists(FILE_DIRECTORY)) return;

        logger.info("Server Settings does not exist, creating new.");
        try {
            URL resourceURL = getClass().getClassLoader().getResource(DEFAULT_FILE);
            if(resourceURL == null) throw new FileNotFoundException("Uh oh... default resource does not exist:\n" + DEFAULT_FILE);

            try(InputStream inputStream = resourceURL.openStream()) {
                Files.copy(inputStream, FILE_DIRECTORY);
            }
        } catch(IOException e) {
            logger.error("Failed to create {}", FILE_DIRECTORY, e);
        }
    }

    public void reset() {
        try {
            if(Files.exists(FILE_DIRECTORY)) {
                Files.move(FILE_DIRECTORY, OLD_DIRECTORY, StandardCopyOption.REPLACE_EXISTING);
            }
            this.createFile();
        } catch(IOException e) {
            logger.error("Failed to reset Server Settings", e);
        }
    }

    public void save() {
        if(!Files.exists(FILE_DIRECTORY)) this.createFile();

        try(BufferedWriter writer = Files.newBufferedWriter(FILE_DIRECTORY, StandardCharsets.UTF_8)) {
            GSON.toJson(ServerSettings.getInstance(), TYPE, writer);
            logger.info("Successfully saved Server Settings!");
        } catch(IOException e) {
            logger.error("Failed to save Server Settings", e);
        }
    }

    public boolean exists() {
        return Files.exists(FILE_DIRECTORY);
    }
}