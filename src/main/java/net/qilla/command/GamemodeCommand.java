package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.qilla.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class GamemodeCommand extends Command {

    private static final String NAME = "gamemode";
    private static final String[] ALIASES = {"gm"};

    private static final String ARG_GAMEMODE = "gamemode";
    private static final String ARG_TARGET = "target";

    public GamemodeCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        ArgumentEnum<GameMode> argGamemode = ArgumentType.Enum(ARG_GAMEMODE, GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            this.setPlayerGamemode((Player) sender, context.get(ARG_GAMEMODE));
        }, argGamemode);

        ArgumentEntity argumentEntity = ArgumentType.Entity(ARG_TARGET).onlyPlayers(true);

        super.addSyntax((sender, context) -> {
            EntityFinder finder = context.get(argumentEntity);
            List<Player> players = finder.find(sender).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).toList();
            GameMode gameMode = context.get(ARG_GAMEMODE);

            if(players.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(players.size() == 1) {
                Player target = players.getFirst();
                this.setPlayerGamemode(target, gameMode);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have set <yellow>" +
                        target.getUsername() + "</yellow>'s gamemode to " + gameMode.name() + "!"));
                return;
            }
            List<String> strList = new ArrayList<>();

            for(Player targetPlayer : players) {
                this.setPlayerGamemode(targetPlayer, gameMode);
                strList.add(targetPlayer.getUsername());
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have set <yellow>" +
                    StringUtil.toLimitedNameList(strList, ", ", 1) +
                    " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow> gamemode to " + gameMode.name() + "!"));

        }, argGamemode, argumentEntity);
    }

    private void setPlayerGamemode(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your gamemode has been set to <yellow>" + gameMode.name() + "</yellow>!"));
    }
}