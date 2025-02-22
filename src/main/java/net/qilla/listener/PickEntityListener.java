package net.qilla.listener;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.network.packet.client.play.ClientPickItemFromEntityPacket;
import net.qilla.event.PlayerPickEntityEvent;

public class PickEntityListener {

    public static void pickEntityListener(ClientPickItemFromEntityPacket packet, Player player) {
        Entity entity = player.getInstance().getEntityById(packet.entityId());

        PlayerPickEntityEvent playerPickEntityEvent = new PlayerPickEntityEvent(player, entity);

        EventDispatcher.call(playerPickEntityEvent);
    }
}