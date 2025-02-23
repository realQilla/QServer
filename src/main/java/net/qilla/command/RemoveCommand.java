package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.qilla.player.QPlayer;
import net.qilla.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class RemoveCommand extends Command {

    private static final String NAME = "remove";
    private static final String[] ALIASES = {"kick", "rm"};

    private static final String ARG_PLAYER = "player";
    private static final String ARG_REASON = "reason";

    public RemoveCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        ArgumentEntity argPlayer = ArgumentType.Entity(ARG_PLAYER).onlyPlayers(true);
        ArgumentStringArray argReason = ArgumentType.StringArray(ARG_REASON);

        super.addSyntax((sender, context) -> {
            Player kickedBy = (Player) sender;
            EntityFinder playerFinder = context.get(argPlayer);
            List<QPlayer> players = playerFinder.find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();
            String reason = String.join(" ", context.get(argReason));

            if(players.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                playerTarget.kick(kickedBy, reason);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have removed <yellow>" + playerTarget.getUsername() + "</yellow> for <yellow>" + reason + "!"));
                return;
            }
            List<String> strList = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                playerTarget.kick(kickedBy, reason);
                strList.add(playerTarget.getUsername());
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have removed <yellow>" + StringUtil.toLimitedNameList(strList, ", ", 1) + " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow> for <yellow>" + reason + "!"));
        }, argPlayer, argReason);
    }
}