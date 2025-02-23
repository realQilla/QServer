package net.qilla.command;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.entity.EntityFinder;
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

public final class BlacklistCommand extends Command {

    private static final String NAME = "blacklist";
    private static final String[] ALIASES = {"ban", "bl"};

    private static final String ARG_PLAYER = "player";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_REASON = "reason";
    private static final String ARG_LENGTH = "length";
    private static final String ARG_LENGTH_TYPE = "length_type";
    private static final String ARG_PERMANENT = "permanent";
    private static final String ARG_REMOVE = "remove";

    public BlacklistCommand(@NotNull ConnectionManager connectionMan) {
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
        ArgumentLiteral argPermanent = ArgumentType.Literal(ARG_PERMANENT);

        super.addSyntax((sender, context) -> {
            Player blackListedBy = (Player) sender;
            EntityFinder playerFinder = context.get(argPlayer);
            List<QPlayer> players = playerFinder.find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();
            String reason = String.join(" ", context.get(argReason));

            if(players.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                playerTarget.blacklist(blackListedBy, reason, PlayerPunishment.PERMANENT);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have permanently blacklisted <yellow>" + playerTarget.getUsername() + "</yellow> for <yellow>" + reason + "!"));
                return;
            }
            List<String> strList = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                playerTarget.blacklist(blackListedBy, reason, PlayerPunishment.PERMANENT);
                strList.add(playerTarget.getUsername());
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have permanently blacklisted <yellow>" +
                    StringUtil.toLimitedNameList(strList, ", ", 1) + " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow> for <yellow>" + reason + "!"));
        }, argPlayer, argPermanent, argReason);

        ArgumentLong argLength = ArgumentType.Long(ARG_LENGTH);
        ArgumentEnum<TimeUnit> argLengthType = ArgumentType.Enum(ARG_LENGTH_TYPE, TimeUnit.class);

        super.addSyntax((sender, context) -> {
            Player blackListedBy = (Player) sender;
            EntityFinder playerFinder = context.get(argPlayer);
            List<QPlayer> players = playerFinder.find(sender).stream().filter(entity -> entity instanceof QPlayer).map(entity -> (QPlayer) entity).toList();
            long expiration = System.currentTimeMillis() + context.get(argLength) * context.get(argLengthType).getDuration();
            String reason = String.join(" ", context.get(argReason));

            if(players.isEmpty()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No players were found!"));
                return;
            }

            if(players.size() == 1) {
                QPlayer playerTarget = players.getFirst();
                playerTarget.blacklist(blackListedBy, reason, expiration);
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have blacklisted <yellow>" +
                        playerTarget.getUsername() + "</yellow> for <yellow>" + reason +
                        "</yellow> until <yellow>" + TimeUtil.date(expiration) + "</yellow>!"));
                return;
            }
            List<String> strList = new ArrayList<>();

            for(QPlayer playerTarget : players) {
                playerTarget.blacklist(blackListedBy, reason, expiration);
                strList.add(playerTarget.getUsername());
            }
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have blacklisted <yellow>" +
                    StringUtil.toLimitedNameList(strList, ", ", 1) + " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) +
                    "</yellow> for <yellow>" + reason +
                    "</yellow> until <yellow>" + TimeUtil.date(expiration) + "</yellow>!"));
        }, argPlayer, argLength, argLengthType, argReason);

        ArgumentString argUsername = ArgumentType.String(ARG_USERNAME);

        super.addSyntax((sender, context) -> {
            Player blackListedBy = (Player) sender;
            String usernameInput = context.get(argUsername);
            String reason = String.join(" ", context.get(argReason));

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified an invalid username!"));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>That player either does not exist or the API is down!"));
                return;
            }

            String username = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerData playerData = PDRegistry.getInstance().get(uuid);

            PlayerPunishment punishment = playerData.setBlackListed(blackListedBy.getUuid(), reason, System.currentTimeMillis(), PlayerPunishment.PERMANENT);
            QPlayer targetPlayer = (QPlayer) connectionMan.getOnlinePlayerByUuid(uuid);

            if(targetPlayer != null) targetPlayer.kick(PlayerPunishment.initialMsg(punishment));

            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have permanently blacklisted <yellow>" + username + "</yellow> for <yellow>" + reason + "!"));
        }, argUsername, argPermanent, argReason);

        super.addSyntax((sender, context) -> {
            Player blackListedBy = (Player) sender;
            String usernameInput = context.get(argUsername);
            String reason = String.join(" ", context.get(argReason));
            long expiration = System.currentTimeMillis() + context.get(argLength) * context.get(argLengthType).getDuration();

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified an invalid username!"));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>That player either does not exist or the API is down!"));
                return;
            }

            String username = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerData playerData = PDRegistry.getInstance().get(uuid);

            QPlayer targetPlayer = (QPlayer) connectionMan.getOnlinePlayerByUuid(uuid);
            PlayerPunishment punishment = playerData.setBlackListed(blackListedBy.getUuid(), reason, System.currentTimeMillis(), expiration);

            if(targetPlayer != null) targetPlayer.kick(PlayerPunishment.initialMsg(punishment));

            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have blacklisted <yellow>" + username + "</yellow> for <yellow>" + reason +
                    "</yellow> until <yellow>" + TimeUtil.date(expiration) + "</yellow>!"));
        }, argUsername, argLength, argLengthType, argReason);

        ArgumentLiteral argRemoveBlacklist = ArgumentType.Literal(ARG_REMOVE);

        super.addSyntax((sender, context) -> {
            String usernameInput = context.get(argUsername);

            if(!(usernameInput.length() >= 3 && usernameInput.length() <= 16)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified an invalid username!"));
                return;
            }
            JsonObject json = MojangUtils.fromUsername(usernameInput);

            if(json == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>That player either does not exist or the API is down!"));
                return;
            }

            String username = json.get("name").getAsString();
            UUID uuid = StringUtil.formatUUID(json.get("id").toString().replace("\"", ""));
            PlayerData playerData = PDRegistry.getInstance().get(uuid);

            PlayerPunishment punishment = playerData.lookupActive();
            if(punishment == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>This player is not currently blacklisted!"));
                return;
            }

            punishment.setExpired();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green><yellow>" + username + "</yellow> has been successfully unblacklisted!"));
        }, argRemoveBlacklist, argUsername);
    }
}