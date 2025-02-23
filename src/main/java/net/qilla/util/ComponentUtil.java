package net.qilla.util;

import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ComponentUtil {

    public static @NotNull String clean(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static @NotNull Component getItemAmountAndType(@NotNull ItemStack itemStack) {
        Component itemName = itemStack.get(ItemComponent.ITEM_NAME);
        return MiniMessage.miniMessage().deserialize("x" + itemStack.amount() + " ").append(itemName);
    }
}
