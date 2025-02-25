package net.qilla.command;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.qilla.util.ComponentUtil;
import net.qilla.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class GetCommand extends Command {

    private static final String NAME = "get";
    private static final String[] ALIASES = {"i", "give"};

    private static final String ARG_ITEMSTACK = "itemstack";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_TARGET = "target";

    public GetCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        ArgumentItemStack argItemStack = ArgumentType.ItemStack(ARG_ITEMSTACK);
        ArgumentNumber<Integer> argAmount = ArgumentType.Integer(ARG_AMOUNT).min(0);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;
            ItemStack itemStack = context.get(argItemStack);
            final int amount = Math.min(itemStack.maxStackSize() * 36, context.get(ARG_AMOUNT));

            this.giveItem(player, itemStack, amount);
        }, argItemStack, argAmount);

        ArgumentEntity argTarget = ArgumentType.Entity(ARG_TARGET).onlyPlayers(true);

        super.addSyntax((sender, context) -> {
            final List<Player> playerTargets = context.get(argTarget).find(sender).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).toList();
            final ItemStack itemStack = context.get(argItemStack);
            final int amount = Math.min(itemStack.maxStackSize() * 36, context.get(ARG_AMOUNT));


            if(playerTargets.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(playerTargets.size() == 1) {
                Player target = playerTargets.getFirst();
                this.giveItem(target, itemStack, amount);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have given <yellow>")
                        .append(ComponentUtil.getItemAmountAndType(itemStack).color(NamedTextColor.YELLOW))
                        .append(MiniMessage.miniMessage().deserialize(" to <yellow>" + target.getUsername() + "</yellow>!")));
                return;
            }
            List<String> strList = new ArrayList<>();

            for(Player targetPlayer : playerTargets) {
                this.giveItem(targetPlayer, itemStack, amount);
                strList.add(targetPlayer.getUsername());
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have given <yellow>")
                    .append(ComponentUtil.getItemAmountAndType(itemStack).color(NamedTextColor.YELLOW))
                    .append(MiniMessage.miniMessage().deserialize(" to <yellow>" + StringUtil.toLimitedNameList(strList, ", ", 1) +
                            " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow>!")));
        }, argItemStack, argAmount, argTarget);
    }

    private void giveItem(Player player, ItemStack itemStack, int amount) {
        player.getInventory().addItemStack(itemStack.withAmount(amount));
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have received ")
                .append(ComponentUtil.getItemAmountAndType(itemStack).color(NamedTextColor.YELLOW))
                .append(MiniMessage.miniMessage().deserialize("!")));
    }
}