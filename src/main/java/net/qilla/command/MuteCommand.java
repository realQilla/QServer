package net.qilla.command;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.mojang.MojangUtils;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerData;
import net.qilla.data.PlayerPunishment;
import net.qilla.player.QPlayer;
import net.qilla.util.StringUtil;
import net.qilla.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MuteCommand extends Command {

    private static final String NAME = "mute";
    private static final String[] ALIASES = {"mt"};

    private static final String ARG_PLAYER = "player";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_REASON = "reason";
    private static final String ARG_LENGTH = "length";
    private static final String ARG_LENGTH_TYPE = "length_type";
    private static final String ARG_PERMANENT = "permanent";
    private static final String ARG_REMOVE = "remove";

    private final PDRegistry pdRegistry;

    public MuteCommand(@NotNull PDRegistry pdRegistry) {
        super(NAME, ALIASES);

        this.pdRegistry = pdRegistry;

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(this::malformedCommand);
        });

        ArgumentEntity argPlayer = ArgumentType.Entity(ARG_PLAYER).onlyPlayers(true);
        ArgumentStringArray argReason = ArgumentType.StringArray(ARG_REASON);
        ArgumentLiteral argPermanent = ArgumentType.Literal(ARG_PERMANENT);

        super.addSyntax((sender, context) -> {
            Player mutedBy = (Player) sender;
            List<QPlayer> players = context.get(argPlayer).find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();
            String reason = String.join(" ", context.get(argReason));

            if(players.isEmpty()) {
                sender.sendMessage(this::noPlayersFound);
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                playerTarget.mute(mutedBy, reason, PlayerPunishment.PERMANENT);
                sender.sendMessage(this.mutePerm(playerTarget.getUsername(), reason));
                return;
            }
            List<String> usernames = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                playerTarget.mute(mutedBy, reason, PlayerPunishment.PERMANENT);
                usernames.add(playerTarget.getUsername());
            }
            sender.sendMessage(this.massMutePerm(usernames, reason));
        }, argPlayer, argPermanent, argReason);

        ArgumentLong argLength = ArgumentType.Long(ARG_LENGTH);
        ArgumentEnum<TimeUnit> argLengthType = ArgumentType.Enum(ARG_LENGTH_TYPE, TimeUnit.class);

        super.addSyntax((sender, context) -> {
            Player mutedBy = (Player) sender;
            List<QPlayer> players = context.get(argPlayer).find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();
            long expiration = System.currentTimeMillis() + context.get(argLength) * context.get(argLengthType).getDuration();
            String reason = String.join(" ", context.get(argReason));

            if(players.isEmpty()) {
                sender.sendMessage(this::noPlayersFound);
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                playerTarget.mute(mutedBy, reason, expiration);
                sender.sendMessage(this.mute(playerTarget.getUsername(), reason, expiration));
                return;
            }
            List<String> usernames = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                playerTarget.mute(mutedBy, reason, expiration);
                usernames.add(playerTarget.getUsername());
            }
            sender.sendMessage(this.massMute(usernames, reason, expiration));
        }, argPlayer, argLength, argLengthType, argReason);

        ArgumentString argUsername = ArgumentType.String(ARG_USERNAME);

        super.addSyntax((sender, context) -> {
            Player mutedBy = (Player) sender;
            String usernameInput = context.get(argUsername);
            String reason = String.join(" ", context.get(argReason));

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

            playerData.setMuted(mutedBy.getUuid(), reason, System.currentTimeMillis(), PlayerPunishment.PERMANENT);

            sender.sendMessage(this.mutePerm(username, reason));
        }, argUsername, argPermanent, argReason);

        super.addSyntax((sender, context) -> {
            Player mutedBy = (Player) sender;
            String usernameInput = context.get(argUsername);
            String reason = String.join(" ", context.get(argReason));
            long expiration = System.currentTimeMillis() + context.get(argLength) * context.get(argLengthType).getDuration();

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

            playerData.setMuted(mutedBy.getUuid(), reason, System.currentTimeMillis(), expiration);

            sender.sendMessage(this.mute(username, reason, expiration));
        }, argUsername, argLength, argLengthType, argReason);

        ArgumentLiteral argRemoveMute = ArgumentType.Literal(ARG_REMOVE);

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

            PlayerPunishment punishment = playerData.lookupActive(PlayerPunishment.PunishmentType.MUTE);
            if(punishment == null) {
                sender.sendMessage(this.notMuted(username));
                return;
            }

            punishment.setExpired();
            sender.sendMessage(this.removeMute(username));
        }, argRemoveMute, argUsername);
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

    public @NotNull Component mutePerm(@NotNull String username, @NotNull String reason) {
        return MiniMessage.miniMessage().deserialize("<green>You have permanently muted <yellow>" + username + "</yellow> for <yellow>" + reason + "!");
    }

    public @NotNull Component mute(@NotNull String username, @NotNull String reason, long expiration) {
        return MiniMessage.miniMessage().deserialize("<green>You have muted <yellow>" +
                username + "</yellow> for <yellow>" + reason +
                "</yellow> until <yellow>" + TimeUtil.date(expiration) + "</yellow>!");
    }

    public @NotNull Component massMutePerm(@NotNull List<String> usernames, @NotNull String reason) {
        return MiniMessage.miniMessage().deserialize("<green>You have permanently muted <yellow>" +
                StringUtil.toLimitedNameList(usernames, ", ", 1) + " and " + (usernames.size() - 1) + " " + StringUtil.pluralize("other", usernames.size() - 1) + "</yellow> for <yellow>" + reason + "!");
    }

    public @NotNull Component massMute(@NotNull List<String> usernames, @NotNull String reason, long expiration) {
        return MiniMessage.miniMessage().deserialize("<green>You have muted <yellow>" +
                StringUtil.toLimitedNameList(usernames, ", ", 1) + " and " + (usernames.size() - 1) + " " + StringUtil.pluralize("other", usernames.size() - 1) +
                "</yellow> for <yellow>" + reason +
                "</yellow> until <yellow>" + TimeUtil.date(expiration) + "</yellow>!");
    }

    public @NotNull Component notMuted(@NotNull String username) {
        return MiniMessage.miniMessage().deserialize("<red><yellow>" + username + "</yellow> is not currently muted!");
    }

    public @NotNull Component removeMute(@NotNull String username) {
        return MiniMessage.miniMessage().deserialize("<green><yellow>" + username + "</yellow> is no longer muted!");
    }
}