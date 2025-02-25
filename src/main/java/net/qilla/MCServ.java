package net.qilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.*;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.server.network.packet.client.play.ClientPickItemFromEntityPacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import net.qilla.command.*;
import net.qilla.data.PDRegistry;
import net.qilla.data.ServerSettings;
import net.qilla.file.PlayerDataFile;
import net.qilla.file.QFiles;
import net.qilla.file.ServerSettingsFile;
import net.qilla.instance.custom.MainGeneration;
import net.qilla.listener.PickBlockListener;
import net.qilla.listener.PickEntityListener;
import net.qilla.player.PlayerListeners;
import net.qilla.player.QPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class MCServ {

    private final Logger logger = MinecraftServer.LOGGER;
    private final MinecraftServer minecraftServer = MinecraftServer.init();
    private final String address;
    private final int port;

    private final InstanceManager instanceMan = MinecraftServer.getInstanceManager();
    private final PacketListenerManager packetListenerMan = MinecraftServer.getPacketListenerManager();
    private final GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
    private final ConnectionManager connectionMan = MinecraftServer.getConnectionManager();

    private final ServerSettingsFile ssFile = QFiles.SERVER_SETTINGS;
    private final PlayerDataFile pdFile = QFiles.PLAYER_DATA;
    private final PDRegistry pdRegistry = PDRegistry.getInstance();

    private final ServerSettings serverSettings = ServerSettings.init();

    public MCServ(@NotNull String address, int port) {
        System.setProperty("minestom.chunk-view-distance", "16");
        this.address = address;
        this.port = port;
    }

    public void init() {
        DynamicRegistry.Key<DimensionType> key = MinecraftServer.getDimensionTypeRegistry().register("main", MainGeneration.getDimension());
        Instance mainInstance = instanceMan.createInstanceContainer(key);

        mainInstance.enableAutoChunkLoad(true);
        mainInstance.setWorldBorder(new WorldBorder(128000, 0, 0, 4, 5));
        mainInstance.setGenerator(MainGeneration.get());

        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(mainInstance);
        });

        connectionMan.setPlayerProvider(QPlayer::new);
        MinecraftServer.setBrandName("QServer");

        this.initCommands();
        new PlayerListeners(this).register(eventHandler);

        this.minecraftServer.start(address, port);
    }

    public void shutdown() {
        MinecraftServer.stopCleanly();
        pdRegistry.getAll().forEach(playerData -> {
            QFiles.PLAYER_DATA.save(playerData);
        });
    }

    public void initCommands() {
        MinecraftServer.getCommandManager().register(new ShutdownCommand(this));
        MinecraftServer.getCommandManager().register(new SaveCommand(instanceMan, pdFile, pdRegistry, ssFile, serverSettings));
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new InstanceCommand(instanceMan));
        MinecraftServer.getCommandManager().register(new KillCommand());
        MinecraftServer.getCommandManager().register(new SummonCommand());
        MinecraftServer.getCommandManager().register(new TeleportCommand());
        MinecraftServer.getCommandManager().register(new RemoveCommand());
        MinecraftServer.getCommandManager().register(new BlacklistCommand(connectionMan, pdRegistry));
        MinecraftServer.getCommandManager().register(new GetCommand());
        MinecraftServer.getCommandManager().register(new ClearCommand());
        MinecraftServer.getCommandManager().register(new WhitelistCommand(serverSettings, connectionMan, pdRegistry));
        MinecraftServer.getCommandManager().register(new MuteCommand(pdRegistry));
        MinecraftServer.getCommandManager().register(new SkinCommand());
    }

    public @NotNull ServerSettingsFile getServerSettingsFile() {
        return this.ssFile;
    }

    public @NotNull PlayerDataFile getPlayerDataFile() {
        return this.pdFile;
    }

    public @NotNull ServerSettings getServerSettings() {
        return this.serverSettings;
    }

    public @NotNull PDRegistry getPDRegistry() {
        return this.pdRegistry;
    }

    public @NotNull InstanceManager getInstanceManager() {
        return this.instanceMan;
    }

    public @NotNull GlobalEventHandler getEventHandler() {
        return this.eventHandler;
    }

    public @NotNull ConnectionManager getConnectionManager() {
        return this.connectionMan;
    }

    public @NotNull PacketListenerManager getPacketListenerManager() {
        return this.packetListenerMan;
    }

    public @NotNull Logger getLogger() {
        return this.logger;
    }
}