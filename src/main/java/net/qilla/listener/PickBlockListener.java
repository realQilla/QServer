package net.qilla.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.qilla.event.PlayerPickBlockEvent;

public class PickBlockListener {

    public static void pickBlockListener(ClientPickItemFromBlockPacket packet, Player player) {
        Block block = player.getInstance().getBlock(packet.pos(), Block.Getter.Condition.NONE);
        Material material = block.registry().material();

        PlayerPickBlockEvent playerPickBlockEvent = new PlayerPickBlockEvent(player, block, material);

        EventDispatcher.call(playerPickBlockEvent);
    }
}