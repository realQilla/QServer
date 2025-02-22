package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class PermissionCommand extends Command {

    private static final String NAME = "permission";
    private static final String[] ALIASES = {"perm"};

    private static final String ARG_GAMEMODE = "gamemode";


    public PermissionCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 4)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.addSyntax((sender, context) -> {
            Player player = (Player) sender;
            GameMode gameMode = context.get(ARG_GAMEMODE);
            player.setGameMode(gameMode);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your gamemode has been set to " + gameMode.name() + "!"));
        }, ArgumentType.Enum(ARG_GAMEMODE, GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED));
    }
}
