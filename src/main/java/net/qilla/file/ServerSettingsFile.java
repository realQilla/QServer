package net.qilla.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.MinecraftServer;
import net.qilla.data.ServerSettings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ServerSettingsFile {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_DIRECTORY = Path.of("server_settings.json");
    private static final Path OLD_DIRECTORY = FILE_DIRECTORY.resolve("old");
    private final Logger logger = MinecraftServer.LOGGER;

    ServerSettingsFile() {
    }

    public @NotNull ServerSettings load() {
        try(BufferedReader reader = Files.newBufferedReader(FILE_DIRECTORY, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, ServerSettings.class);
        } catch(IOException e) {
            this.reset();
            return ServerSettings.getDefault();
        }
    }

    public void createFile() {
        if(Files.exists(FILE_DIRECTORY)) return;

        try {
            Files.createFile(FILE_DIRECTORY);
            BufferedWriter writer = Files.newBufferedWriter(FILE_DIRECTORY, StandardCharsets.UTF_8);

            GSON.toJson(ServerSettings.getDefault(), ServerSettings.class, writer);
        } catch(IOException e) {
            logger.error("Failed to create Server Settings file", e);
        }
    }

    public void reset() {
        try {
            if(Files.exists(FILE_DIRECTORY)) {
                Files.move(FILE_DIRECTORY, OLD_DIRECTORY, StandardCopyOption.REPLACE_EXISTING);

                BufferedWriter writer = Files.newBufferedWriter(FILE_DIRECTORY, StandardCharsets.UTF_8);
                GSON.toJson(ServerSettings.getDefault(), ServerSettings.class, writer);
            }
            this.createFile();
        } catch(IOException e) {
            logger.error("Failed to reset Server Settings file", e);
        }
    }

    public void save(@NotNull ServerSettings serverSettings) {
        if(!Files.exists(FILE_DIRECTORY)) this.createFile();

        try(BufferedWriter writer = Files.newBufferedWriter(FILE_DIRECTORY, StandardCharsets.UTF_8)) {
            GSON.toJson(serverSettings, ServerSettings.class, writer);
            logger.info("Successfully saved Server Settings!");
        } catch(IOException e) {
            logger.error("Failed to save Server Settings", e);
        }
    }

    public boolean exists() {
        return Files.exists(FILE_DIRECTORY);
    }
}