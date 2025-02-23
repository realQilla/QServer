package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.qilla.data.PlayerData;
import net.qilla.player.QPlayer;
import net.qilla.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class WhitelistCommand extends Command {

    private static final String NAME = "whitelist";
    private static final String[] ALIASES = {"wl"};

    private static final String ARG_PLAYER = "target";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_ADD = "add";
    private static final String ARG_REMOVE = "remove";

    public WhitelistCommand() {
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

        super.addSyntax((sender, context) -> {
            List<Player> playerList = context.get(argPlayer).find(sender).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).toList();

            if(playerList.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(playerList.size() == 1) {
                QPlayer player = (QPlayer) playerList.getFirst();
                this.whitelistUser(player);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have whitelisted <yellow>" + player.getUsername() + "</yellow> to the server!"));
            }
            List<String> strList = new ArrayList<>();

            for(Player player : playerList) {
                QPlayer qPlayer = (QPlayer) player;
                this.whitelistUser(qPlayer);
                strList.add(qPlayer.getUsername());
            }

            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have whitelisted <yellow>" +
                    StringUtil.toLimitedNameList(strList, ", ", 1) +
                    " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow> to the server!"));
        }, argPlayer);
    }

    private void whitelistUser(QPlayer player) {
        PlayerData playerData = player.getData();

        playerData.setWhitelisted(true);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been whitelisted to the server!"));
    }
}