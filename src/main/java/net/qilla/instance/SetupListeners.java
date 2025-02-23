package net.qilla.instance;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.server.network.packet.client.play.ClientPickItemFromEntityPacket;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import net.qilla.data.PlayerPunishment;
import net.qilla.event.PlayerPickBlockEvent;
import net.qilla.listener.PickBlockListener;
import net.qilla.listener.PickEntityListener;
import net.qilla.util.MsgFormat;
import net.qilla.util.MsgUtil;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.UUID;

public final class SetupListeners {

    private static final Logger LOGGER = MinecraftServer.LOGGER;

    private SetupListeners() {
    }

    public static void initEvent(GlobalEventHandler eventHandler, InstanceManager instanceMan) {
        eventHandler.addListener(PlayerPickBlockEvent.class, event -> {
            final Player player = event.getPlayer();

            if(player.getGameMode() != GameMode.CREATIVE) return;

            final Material clickedMaterial = event.getMaterial();
            final PlayerInventory inventory = player.getInventory();

            for(int i = 0; i < 9; i++) {
                if(clickedMaterial.equals(inventory.getItemStack(i).material())) {
                    player.setHeldItemSlot((byte) i);
                    return;
                }
            }
            player.setItemInMainHand(ItemStack.of(event.getMaterial()));
        });

        eventHandler.addListener(PlayerChatEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setFormattedMessage(MiniMessage.miniMessage().deserialize("<gold>" + player.getUsername() + "<white> » <gray>" + event.getRawMessage()));
        });

        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            MsgUtil.send(MiniMessage.miniMessage().deserialize("<green><yellow>" + event.getPlayer().getUsername() + "</yellow> has disconnected."));
        });

        eventHandler.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            MsgUtil.send(MiniMessage.miniMessage().deserialize("<green><yellow>" + player.getUsername() + "</yellow> has connected."));
        });

        eventHandler.addListener(PlayerSkinInitEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerData playerData = PDRegistry.getInstance().get(player.getUuid());

            event.setSkin(playerData.getSkin());
        });

        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            UUID uuid = event.getGameProfile().uuid();
            PlayerData playerData = PDRegistry.getInstance().get(uuid);

            PlayerPunishment playerPunishment = playerData.lookupActive();
            if(playerPunishment != null) {
                event.getConnection().kick(PlayerPunishment.futureMsg(playerPunishment));
                return;
            }

            if(!playerData.isWhitelisted()) {
                event.getConnection().kick(MsgFormat.whitelisted());
                return;
            }
        });

        eventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();

            if(player.getPermissionLevel() < 2 ) event.setCancelled(true);
        });

        eventHandler.addListener(ClientPingServerEvent.class, event -> {
            SocketAddress address = event.getConnection().getRemoteAddress();
            LOGGER.info("Server received ping request from {}", address);
        });
    }

    public static void initPacket(PacketListenerManager packetListenerMan) {
        packetListenerMan.setPlayListener(ClientPickItemFromBlockPacket.class, PickBlockListener::pickBlockListener);
        packetListenerMan.setPlayListener(ClientPickItemFromEntityPacket.class, PickEntityListener::pickEntityListener);
    }
}