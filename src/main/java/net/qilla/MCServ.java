package net.qilla;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.*;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.server.network.packet.client.play.ClientPickItemFromEntityPacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import net.qilla.command.*;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import net.qilla.event.PlayerPickBlockEvent;
import net.qilla.file.PlayerDataFile;
import net.qilla.instance.MainGeneration;
import net.qilla.listener.PickBlockListener;
import net.qilla.listener.PickEntityListener;
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

        eventHandler.addListener(PlayerPickBlockEvent.class, event -> {
            Player player = event.getPlayer();

            if(player.getGameMode() != GameMode.CREATIVE) return;

            Material clickedMaterial = event.getMaterial();
            PlayerInventory inventory = player.getInventory();

            for(int i = 0; i < 9; i++) {
                if(clickedMaterial.equals(inventory.getItemStack(i).material())) {
                    player.setHeldItemSlot((byte) i);
                    return;
                }
            }
            player.setItemInMainHand(ItemStack.of(event.getMaterial()));
        });

        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            final PlayerData playerData = PDRegistry.getInstance().get(player.getUuid());

            event.setSpawningInstance(mainInstance);
            player.setPermissionLevel(playerData.getPermissionLevel());
            PlayerDataFile.getInstance().save();
            player.setRespawnPoint(new Pos(0, 153, 0));
        });

        eventHandler.addListener(PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setFormattedMessage(MiniMessage.miniMessage().deserialize("<gold>" + player.getUsername() + "<white>: " + event.getRawMessage()));
        });

        eventHandler.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            final PlayerData playerData = PDRegistry.getInstance().get(player.getUuid());

            player.setSkin(playerData.getSkin());
            player.setGameMode(GameMode.SURVIVAL);
        });

        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            connectionMan.getOnlinePlayers().forEach(instance -> {
                instance.sendMessage(MiniMessage.miniMessage().deserialize("<green><yellow>" + event.getPlayer().getUsername() + "</yellow> has disconnected."));
            });
        });

        eventHandler.addListener(PlayerSpawnEvent.class, event -> {
            connectionMan.getOnlinePlayers().forEach(instance -> {
                instance.sendMessage(MiniMessage.miniMessage().deserialize("<green><yellow>" + event.getPlayer().getUsername() + "</yellow> has connected."));
            });
        });

        this.customListeners();
        this.loadCommands();
        this.minecraftServer.start(address, port);
    }

    public void shutdown() {
        MinecraftServer.stopCleanly();
        PlayerDataFile.getInstance().save();
    }

    private void customListeners() {
        packetListenerMan.setPlayListener(ClientPickItemFromBlockPacket.class, PickBlockListener::pickBlockListener);
        packetListenerMan.setPlayListener(ClientPickItemFromEntityPacket.class, PickEntityListener::pickEntityListener);
    }

    private void loadCommands() {
        MinecraftServer.getCommandManager().register(new ShutdownCommand(this));
        MinecraftServer.getCommandManager().register(new SaveCommand(instanceMan));
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new InstanceCommand(instanceMan));
        MinecraftServer.getCommandManager().register(new KillCommand());
        MinecraftServer.getCommandManager().register(new SummonCommand());
        MinecraftServer.getCommandManager().register(new TeleportCommand());
    }
}