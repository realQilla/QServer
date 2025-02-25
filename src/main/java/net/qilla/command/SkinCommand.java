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
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.mojang.MojangUtils;
import net.qilla.player.QPlayer;
import net.qilla.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SkinCommand extends Command {

    private static final String NAME = "skin";
    private static final String[] ALIASES = {};

    private static final String ARG_SKIN = "skin";
    private static final String ARG_TARGET = "target";
    private static final String ARG_RESET = "reset";

    public SkinCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(this::malformedCommand);
        });

        ArgumentString argSkin = ArgumentType.String(ARG_SKIN);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            QPlayer player = (QPlayer) sender;
            String usernameInput = context.get(argSkin);

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(this.invalidUsername(usernameInput));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(this.notExistOrAPI(usernameInput));
                return;
            }

            String skinHolder = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerSkin skin = PlayerSkin.fromUuid(uuid.toString());

            player.lockSkin(skin);
            sender.sendMessage(this.setSelfSkin(skinHolder));
        }, argSkin);

        ArgumentLiteral argReset = ArgumentType.Literal(ARG_RESET);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            QPlayer player = (QPlayer) sender;
            JsonObject json = MojangUtils.fromUuid(player.getUuid());

            if(json == null) {
                sender.sendMessage(this.notExistOrAPI(player.getUsername()));
                return;
            }

            String skinHolder = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerSkin skin = PlayerSkin.fromUuid(uuid.toString());

            player.setSkin(skin);
            sender.sendMessage(this.setSelfSkin(skinHolder));
        }, argReset);

        ArgumentEntity argTarget = ArgumentType.Entity(ARG_TARGET).onlyPlayers(true);

        super.addSyntax((sender, context) -> {
            List<QPlayer> players = context.get(argTarget).find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();
            String usernameInput = context.get(argSkin);

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(this.invalidUsername(usernameInput));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(this.notExistOrAPI(usernameInput));
                return;
            }

            String skinHolder = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerSkin skin = PlayerSkin.fromUuid(uuid.toString());

            if(players.isEmpty()) {
                sender.sendMessage(this.noPlayersFound());
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                playerTarget.lockSkin(skin);
                sender.sendMessage(this.setSkin(playerTarget.getUsername(), skinHolder));
                return;
            }
            List<String> usernames = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                playerTarget.lockSkin(skin);
                usernames.add(playerTarget.getUsername());
            }

            sender.sendMessage(this.massSetSkin(usernames, skinHolder));
        }, argTarget, argSkin);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            List<QPlayer> players = context.get(argTarget).find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();

            if(players.isEmpty()) {
                sender.sendMessage(this.noPlayersFound());
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                JsonObject json = MojangUtils.fromUuid(playerTarget.getUuid());

                if(json == null) {
                    sender.sendMessage(this.notExistOrAPI(playerTarget.getUsername()));
                    return;
                }

                String skinHolder = json.get("name").getAsString();
                UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
                PlayerSkin skin = PlayerSkin.fromUuid(uuid.toString());

                playerTarget.lockSkin(skin);
                sender.sendMessage(this.setSkin(playerTarget.getUsername(), skinHolder));
                return;
            }
            List<String> usernames = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                JsonObject json = MojangUtils.fromUuid(playerTarget.getUuid());

                if(json == null) {
                    sender.sendMessage(this.notExistOrAPI(playerTarget.getUsername()));
                    continue;
                }

                String skinHolder = json.get("name").getAsString();
                UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
                PlayerSkin skin = PlayerSkin.fromUuid(uuid.toString());

                playerTarget.setSkin(skin);
                usernames.add(skinHolder);
            }
            sender.sendMessage(this.massResetSkin(usernames));
        }, argReset, argTarget);
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

    public @NotNull Component setSelfSkin(String skinHolder) {
        return MiniMessage.miniMessage().deserialize("<green>You have set your skin to <yellow>" + skinHolder + "</yellow>!");
    }

    public @NotNull Component setSkin(String username, String skinHolder) {
        return MiniMessage.miniMessage().deserialize("<green>You have set <yellow>" + username + "</yellow>'s skin to <yellow>" + skinHolder + "</yellow>!");
    }

    public @NotNull Component massSetSkin(List<String> usernames, String skinHolder) {
        return MiniMessage.miniMessage().deserialize("<green>You have set <yellow>" +
                StringUtil.toLimitedNameList(usernames, ", ", 1) + " and " + (usernames.size() - 1) + " " + StringUtil.pluralize("other", usernames.size() - 1) +
                "</yellow> skin's to <yellow>" + skinHolder + "</yellow>!");
    }

    public @NotNull Component massResetSkin(List<String> usernames) {
        return MiniMessage.miniMessage().deserialize("<green>You have reset <yellow>" +
                StringUtil.toLimitedNameList(usernames, ", ", 1) + " and " + (usernames.size() - 1) + " " + StringUtil.pluralize("other", usernames.size() - 1) +
                "</yellow> skin's back to their original skin!");
    }
}