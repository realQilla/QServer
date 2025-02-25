package net.qilla.data;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.utils.identity.NamedAndIdentified;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PrefilledUtils {

    public static @NotNull ResponseData getResponse(int onlinePlayers) {
        ResponseData response = new ResponseData();
        response.setDescription(MiniMessage.miniMessage().deserialize("<gold>Work in progress server. \n<gray>github.com/realQilla/QServer"));
        response.setOnline(onlinePlayers);
        response.setMaxPlayer(onlinePlayers + 1);
        response.setProtocol(MinecraftServer.PROTOCOL_VERSION);
        response.setVersion(MinecraftServer.VERSION_NAME);
        response.setPlayersHidden(false);
        response.addEntries(new ArrayList<>(List.of(
                NamedAndIdentified.named(MiniMessage.miniMessage().deserialize("<yellow>Contact @qilla if you need anything."))
        )));

        return response;
    }
}