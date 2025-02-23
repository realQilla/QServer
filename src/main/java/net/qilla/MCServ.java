package net.qilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.*;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import net.qilla.command.*;
import net.qilla.file.PlayerDataFile;
import net.qilla.instance.SetupListeners;
import net.qilla.instance.custom.MainGeneration;
import net.qilla.player.QPlayer;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public final class MCServ {

    private final Logger logger = MinecraftServer.LOGGER;
    private final MinecraftServer minecraftServer;
    private final String address;
    private final int port;
    private final InstanceManager instanceMan;
    private final PacketListenerManager packetListenerMan;
    private final GlobalEventHandler eventHandler;
    private final ConnectionManager connectionMan;

    public MCServ(String address, int port) {
        System.setProperty("minestom.chunk-view-distance", "16");

        this.minecraftServer = MinecraftServer.init();
        this.address = address;
        this.port = port;
        this.instanceMan = MinecraftServer.getInstanceManager();
        this.packetListenerMan = MinecraftServer.getPacketListenerManager();
        this.eventHandler = MinecraftServer.getGlobalEventHandler();
        this.connectionMan = MinecraftServer.getConnectionManager();
    }

    public void init() {
        connectionMan.setPlayerProvider(QPlayer::new);
        PlayerDataFile.getInstance().load();

        DimensionType mainDimension = DimensionType.builder()
                .minY(0)
                .height(384)
                .ambientLight(15)
                .build();
        DynamicRegistry.Key<DimensionType> key = MinecraftServer.getDimensionTypeRegistry().register("main", mainDimension);

        Instance mainInstance = instanceMan.createInstanceContainer(key);

        MinecraftServer.setBrandName("QServer");

        mainInstance.enableAutoChunkLoad(true);
        mainInstance.setWorldBorder(new WorldBorder(128000, 0, 0, 4, 5));
        mainInstance.setGenerator(MainGeneration.get());

        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkRange.chunksInRange(0, 0, 12, (x, z) -> chunks.add(mainInstance.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(mainInstance, mainInstance.getChunks());
        });

        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(mainInstance);
        });

        SetupListeners.initEvent(eventHandler, instanceMan);
        SetupListeners.initPacket(packetListenerMan);
        this.loadCommands();
        this.minecraftServer.start(address, port);
    }

    public void shutdown() {
        MinecraftServer.stopCleanly();
        PlayerDataFile.getInstance().save();
    }

    private void loadCommands() {
        MinecraftServer.getCommandManager().register(new ShutdownCommand(this));
        MinecraftServer.getCommandManager().register(new SaveCommand(instanceMan));
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new InstanceCommand(instanceMan));
        MinecraftServer.getCommandManager().register(new KillCommand());
        MinecraftServer.getCommandManager().register(new SummonCommand());
        MinecraftServer.getCommandManager().register(new TeleportCommand());
        MinecraftServer.getCommandManager().register(new RemoveCommand());
        MinecraftServer.getCommandManager().register(new BlacklistCommand(connectionMan));
        MinecraftServer.getCommandManager().register(new GetCommand());
        MinecraftServer.getCommandManager().register(new ClearCommand());
    }
}