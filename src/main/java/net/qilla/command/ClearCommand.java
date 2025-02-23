package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.qilla.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class ClearCommand extends Command {

    private static final String NAME = "clear";
    private static final String[] ALIASES = {};

    private static final String ARG_TARGET = "target";

    public ClearCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;

            this.clearInventory(player);
        });

        ArgumentEntity argTarget = ArgumentType.Entity(ARG_TARGET).onlyPlayers(true);

        super.addSyntax((sender, context) -> {
            final List<Player> playerTargets = context.get(argTarget).find(sender).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).toList();

            if(playerTargets.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(playerTargets.size() == 1) {
                Player target = playerTargets.getFirst();
                this.clearInventory(target);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have cleared the inventory of <yellow>" + target.getUsername() + "</yellow>!"));
                return;
            }
            List<String> strList = new ArrayList<>();

            for(Player targetPlayer : playerTargets) {
                this.clearInventory(targetPlayer);
                strList.add(targetPlayer.getUsername());
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have cleared the inventory of <yellow>" + StringUtil.toLimitedNameList(strList, ", ", 1) +
                    " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow>!"));
        }, argTarget);
    }

    private void clearInventory(Player player) {
        player.getInventory().clear();
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your inventory has been cleared!"));
    }
}