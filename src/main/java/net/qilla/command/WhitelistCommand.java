package net.qilla.command;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.mojang.MojangUtils;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import net.qilla.data.PlayerPunishment;
import net.qilla.data.ServerSettings;
import net.qilla.player.QPlayer;
import net.qilla.util.MsgFormat;
import net.qilla.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class WhitelistCommand extends Command {

    private static final String NAME = "whitelist";
    private static final String[] ALIASES = {"wl"};

    private static final String ARG_PLAYER = "target";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_ADD = "add";
    private static final String ARG_ENABLE = "enable";
    private static final String ARG_DISABLE = "disable";
    private static final String ARG_REMOVE = "remove";

    private final ServerSettings serverSettings;

    public WhitelistCommand(@NotNull ServerSettings settings, @NotNull ConnectionManager connMan, @NotNull PDRegistry pdRegistry) {
        super(NAME, ALIASES);

        this.serverSettings = settings;

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(this::malformedCommand);
        });

        ArgumentLiteral argAdd = ArgumentType.Literal(ARG_ADD);
        ArgumentEntity argPlayer = ArgumentType.Entity(ARG_PLAYER).onlyPlayers(true).singleEntity(true);

        super.addSyntax((sender, context) -> {
            QPlayer targetPlayer = (QPlayer) context.get(argPlayer).findFirstPlayer(sender);

            if(targetPlayer == null) {
                sender.sendMessage(this::noPlayersFound);
                return;
            }

            if(!targetPlayer.isOnWhitelist()) {
                targetPlayer.addWhitelist();
                sender.sendMessage(this.addWhitelist(targetPlayer.getUsername()));
            } else sender.sendMessage(this.onWhitelist(targetPlayer.getUsername()));
        }, argAdd, argPlayer);

        ArgumentLiteral argRemove = ArgumentType.Literal(ARG_REMOVE);

        super.addSyntax((sender, context) -> {
            QPlayer targetPlayer = (QPlayer) context.get(argPlayer).findFirstPlayer(sender);

            if(targetPlayer == null) {
                sender.sendMessage(this::noPlayersFound);
                return;
            }

            if(targetPlayer.isOnWhitelist()) {
                targetPlayer.removeWhitelist();
                sender.sendMessage(this.removeWhitelist(targetPlayer.getUsername()));
            } else sender.sendMessage(this.notOnWhitelist(targetPlayer.getUsername()));
        }, argRemove, argPlayer);

        ArgumentLiteral argEnable = ArgumentType.Literal(ARG_ENABLE);

        super.addSyntax((sender, context) -> {
            if(serverSettings.isWhitelistEnabled()) {
                sender.sendMessage(this::whitelistAlreadyEnable);
                return;
            }
            serverSettings.setWhitelist(true);

            List<QPlayer> players = connMan.getOnlinePlayers().stream().map(player -> (QPlayer) player).toList();
            sender.sendMessage(this::whitelistEnabled);

            for(QPlayer player : players) {
                PlayerData playerData = player.getData();
                if(!playerData.isWhitelisted()) player.kick(MsgFormat.whitelisted());
            }
        }, argEnable);

        ArgumentLiteral argDisable = ArgumentType.Literal(ARG_DISABLE);

        super.addSyntax((sender, context) -> {
            if(!serverSettings.isWhitelistEnabled()) {
                sender.sendMessage(this::whitelistAlreadyDisabled);
                return;
            }
            serverSettings.setWhitelist(false);
            sender.sendMessage(this::whitelistDisabled);
        }, argDisable);

        ArgumentString argUsername = ArgumentType.String(ARG_USERNAME);

        super.addSyntax((sender, context) -> {
            String usernameInput = context.get(argUsername);

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(this.invalidUsername(usernameInput));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(this.notExistOrAPI(usernameInput));
                return;
            }

            String username = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerData playerData = pdRegistry.get(uuid);

            if(!playerData.isWhitelisted()) {
                playerData.setWhitelisted(true);
                sender.sendMessage(this.addWhitelist(username));
            } else sender.sendMessage(this.onWhitelist(username));
        }, argAdd, argUsername);

        super.addSyntax((sender, context) -> {
            String usernameInput = context.get(argUsername);

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(this.invalidUsername(usernameInput));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(this.notExistOrAPI(usernameInput));
                return;
            }

            String username = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerData playerData = pdRegistry.get(uuid);

            if(playerData.isWhitelisted()) {
                playerData.setWhitelisted(false);
                sender.sendMessage(this.removeWhitelist(username));
            } else sender.sendMessage(this.notOnWhitelist(username));
        }, argRemove, argUsername);
    }

    private @NotNull Component malformedCommand() {
        return MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments.");
    }

    private @NotNull Component noPlayersFound() {
        return MiniMessage.miniMessage().deserialize("<red>No players were found!");
    }

    private @NotNull Component invalidUsername(String username) {
        return MiniMessage.miniMessage().deserialize("<red><yellow>" + username + "</yellow> is an invalid username!");
    }

    public @NotNull Component notExistOrAPI(String username) {
        return MiniMessage.miniMessage().deserialize("<red><yellow>" + username + "</yellow> either does not exist or the API is down!");
    }

    public @NotNull Component addWhitelist(@NotNull String username) {
        return MiniMessage.miniMessage().deserialize("<green>You have added <yellow>" + username + "</yellow> to the server whitelist!");
    }

    public @NotNull Component onWhitelist(String username) {
        return MiniMessage.miniMessage().deserialize("<red>" + username + " is already added to the server whitelist!");
    }

    public @NotNull Component removeWhitelist(@NotNull String username) {
        return MiniMessage.miniMessage().deserialize("<green>You have removed <yellow>" + username + "</yellow> from the server whitelist!");
    }

    public @NotNull Component notOnWhitelist(String username) {
        return MiniMessage.miniMessage().deserialize("<red>" + username + " is not on the server whitelist!");
    }

    public @NotNull Component whitelistEnabled() {
        return MiniMessage.miniMessage().deserialize("<green>You have enabled the server whitelist!");
    }

    public @NotNull Component whitelistAlreadyEnable() {
        return MiniMessage.miniMessage().deserialize("<red>The server already has whitelist enabled!");
    }

    public @NotNull Component whitelistDisabled() {
        return MiniMessage.miniMessage().deserialize("<green>You have disabled the server whitelist!");
    }

    public @NotNull Component whitelistAlreadyDisabled() {
        return MiniMessage.miniMessage().deserialize("<red>The server already has whitelist disabled!");
    }
}