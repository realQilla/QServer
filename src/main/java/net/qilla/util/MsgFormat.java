package net.qilla.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MsgFormat {

    public static Component whitelisted() {
        return MiniMessage.miniMessage().deserialize("<red>The server is currently closed from the public!")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>Please come back later!"));
    }
}