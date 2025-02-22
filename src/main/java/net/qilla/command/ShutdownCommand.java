package net.qilla.command;

import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.qilla.MCServ;
import org.jetbrains.annotations.NotNull;

public class ShutdownCommand extends Command {

    private static final String NAME = "shutdown";
    private static final String[] ALIASES = {"shutdown", "stop"};


    public ShutdownCommand(@NotNull MCServ mcServ) {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 3)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        setDefaultExecutor((sender, context) -> {
            mcServ.shutdown();
        });
    }
}