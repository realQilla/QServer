package net.qilla.util;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.ConnectionManager;

public final class MsgUtil {

    private static final ConnectionManager connectionMan = MinecraftServer.getConnectionManager();

    private MsgUtil() {
    }

    public static void send(Component component) {
        connectionMan.getOnlinePlayers().forEach(instance -> {
            instance.sendMessage(component);
        });
    }

    public static void sendInstance(Instance instance, Component component) {
        instance.getPlayers().forEach(player -> {
            instance.sendMessage(component);
        });
    }
}