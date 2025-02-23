package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.qilla.instance.custom.NoiseGeneration;
import org.jetbrains.annotations.NotNull;

public final class InstanceCommand extends Command {

    private static final String NAME = "instance";
    private static final String[] ALIASES = {};

    private static final String ARG_CREATE = "create";

    public InstanceCommand(@NotNull InstanceManager instanceMan) {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if(!(sender instanceof Player player)) return false;
            if(player.getPermissionLevel() < 3) return false;
            return true;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        super.addSyntax((sender, context) -> {
            Player player = (Player) sender;

            Instance instance = instanceMan.createInstanceContainer();

            instance.enableAutoChunkLoad(true);
            instance.setGenerator(NoiseGeneration.get());
            instance.setChunkSupplier(LightingChunk::new);
            player.setInstance(instance);

            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have successfully created a new instance!"));
        }, ArgumentType.Literal(ARG_CREATE));
    }
}
