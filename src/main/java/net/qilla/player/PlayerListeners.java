package net.qilla.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.server.network.packet.client.play.ClientPickItemFromEntityPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.qilla.MCServ;
import net.qilla.data.*;
import net.qilla.event.PlayerPickBlockEvent;
import net.qilla.file.PlayerDataFile;
import net.qilla.listener.PickBlockListener;
import net.qilla.listener.PickEntityListener;
import net.qilla.util.MsgFormat;
import net.qilla.util.MsgUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.UUID;

public class PlayerListeners {

    private final Logger logger;
    private final ConnectionManager connectionMan;
    private final PDRegistry playerDataRegistry;
    private final PlayerDataFile playerDataFile;
    private final ServerSettings serverSettings;
    private final PacketListenerManager packetListenerMan;

    public PlayerListeners(MCServ mcServ) {
        this.logger = mcServ.getLogger();
        this.connectionMan = mcServ.getConnectionManager();
        this.playerDataRegistry = PDRegistry.getInstance();
        this.playerDataFile = mcServ.getPlayerDataFile();
        this.serverSettings = mcServ.getServerSettings();
        this.packetListenerMan = mcServ.getPacketListenerManager();
    }

    public void register(GlobalEventHandler eventHandler) {
        packetListenerMan.setPlayListener(ClientPickItemFromBlockPacket.class, PickBlockListener::pickBlockListener);
        packetListenerMan.setPlayListener(ClientPickItemFromEntityPacket.class, PickEntityListener::pickEntityListener);

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

        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            final PlayerData playerData = playerDataRegistry.get(player.getUuid());

            MsgUtil.send(MiniMessage.miniMessage().deserialize("<green><yellow>" + player.getUsername() + "</yellow> has disconnected."));
            playerData.updateLastLogout();
            playerData.saveData();
        });

        eventHandler.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            final PlayerData playerData = playerDataRegistry.get(player.getUuid());

            MsgUtil.send(MiniMessage.miniMessage().deserialize("<green><yellow>" + player.getUsername() + "</yellow> has connected."));
            playerData.updateLastLogin();
        });

        eventHandler.addListener(PlayerSkinInitEvent.class, event -> {
            final Player player = event.getPlayer();
            final PlayerData playerData = playerDataRegistry.get(player.getUuid());

            event.setSkin(playerData.getSkin());
        });

        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            final GameProfile gameProfile = event.getGameProfile();
            final UUID uuid = gameProfile.uuid();
            final PlayerConnection connection = event.getConnection();

            Player player = connectionMan.getOnlinePlayerByUuid(uuid);
            if(player != null) {
                player.kick(MsgFormat.overwriteConnection());
            }

            if(!playerRegistrationLogic(gameProfile)) {
                connection.kick(MsgFormat.registrationError());
                return;
            }

            final PlayerData playerData = playerDataRegistry.get(uuid);

            PlayerPunishment playerPunishment = playerData.lookupActive(PlayerPunishment.PunishmentType.KICK, PlayerPunishment.PunishmentType.BLACKLIST);
            if(playerPunishment != null) {
                if(playerPunishment.getType() == PlayerPunishment.PunishmentType.KICK) {
                    connection.kick(MsgFormat.kick());
                    return;
                } else if(playerPunishment.getType() == PlayerPunishment.PunishmentType.BLACKLIST) {
                    Component msg = playerPunishment.isPermanent() ? MsgFormat.blacklistPerm(playerPunishment) : MsgFormat.blacklist(playerPunishment);
                    connection.kick(msg);
                    return;
                }
            }

            if(serverSettings.isWhitelistEnabled() && !playerData.isWhitelisted()) {
                connection.kick(MsgFormat.whitelisted());
            }
        });

        eventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();

            if(player.getPermissionLevel() < 2) event.setCancelled(true);
        });

        eventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            final Player player = event.getPlayer();

            if(player.getPermissionLevel() < 2) event.setCancelled(true);
        });

        eventHandler.addListener(PickupItemEvent.class, event -> {
            final LivingEntity livingEntity = event.getLivingEntity();

            if(livingEntity instanceof Player player) {
                ItemStack itemStack = event.getItemStack();

                player.getInventory().addItemStack(itemStack);
            }
        });

        eventHandler.addListener(PlayerDeathEvent.class, event -> {
           event.setDeathText(null);
           event.setChatMessage(null);
        });

        eventHandler.addListener(ClientPingServerEvent.class, event -> {
            SocketAddress address = event.getConnection().getRemoteAddress();
            logger.info("Server received ping request from {}", address);
        });

        eventHandler.addListener(ServerListPingEvent.class, event -> {
            event.setResponseData(PrefilledUtils.getResponse(connectionMan.getOnlinePlayerCount()));
            logger.info("Server sent ping response to {}", event.getConnection().getRemoteAddress());
        });
    }

    private boolean playerRegistrationLogic(@NotNull GameProfile gameProfile) {
        UUID uuid = gameProfile.uuid();

        if(playerDataRegistry.has(uuid)) return true;

        if(playerDataFile.exists(uuid)) {
            PlayerData playerData = playerDataFile.load(gameProfile);
            if(playerData == null) return false;
            playerDataRegistry.set(playerData);
            return playerDataFile.save(playerData);
        } else {
            PlayerData playerData = new PlayerData(gameProfile);
            playerDataRegistry.set(playerData);
            return playerDataFile.save(playerData);
        }
    }
}
