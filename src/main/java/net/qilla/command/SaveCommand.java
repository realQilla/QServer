package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.qilla.data.PDRegistry;
import net.qilla.data.ServerSettings;
import net.qilla.file.PlayerDataFile;
import net.qilla.file.ServerSettingsFile;
import org.jetbrains.annotations.NotNull;

public final class SaveCommand extends Command {

    private static final String NAME = "save";
    private static final String[] ALIASES = {};

    private static final String ARG_INSTANCES = "instances";
    private static final String ARG_CURRENT_INSTANCE = "current";
    private static final String ARG_GLOBAL_INSTANCE = "global";
    private static final String ARG_PLAYER_DATA = "player_data";
    private static final String ARG_SERVER_SETTINGS = "server_settings";

    private final PlayerDataFile pdFile;
    private final ServerSettingsFile ssFile;

    public SaveCommand(@NotNull InstanceManager instanceMan, @NotNull PlayerDataFile pdFile, @NotNull PDRegistry pdRegistry, @NotNull ServerSettingsFile ssFile, @NotNull ServerSettings serverSettings) {
        super(NAME, ALIASES);
        this.pdFile = pdFile;
        this.ssFile = ssFile;

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 3)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        super.addSyntax((sender, context) -> {
            instanceMan.getInstances().forEach(Instance::saveInstance);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>All server instances have been saved!"));
        }, ArgumentType.Literal(ARG_INSTANCES), ArgumentType.Literal(ARG_GLOBAL_INSTANCE));

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;
            Instance instance = player.getInstance();
            instance.saveInstance();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>The current server instance has been saved!"));
        }, ArgumentType.Literal(ARG_INSTANCES), ArgumentType.Literal(ARG_CURRENT_INSTANCE));

        super.addSyntax((sender, context) -> {
            pdRegistry.getAll().forEach(pdFile::save);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Player data for all players has been successfully saved!"));
        }, ArgumentType.Literal(ARG_PLAYER_DATA));

        super.addSyntax((sender, context) -> {
            ssFile.save(serverSettings);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Server Settings have been successfully saved!"));
        }, ArgumentType.Literal(ARG_SERVER_SETTINGS));
    }
}