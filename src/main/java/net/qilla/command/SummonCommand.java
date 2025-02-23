package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.qilla.util.StringUtil;

public final class SummonCommand extends Command {

    private static final String NAME = "summon";
    private static final String[] ALIASES = {"spawn", "create"};

    private static final String ARG_TYPE = "entity";
    private static final String ARG_AMOUNT = "amount";

    public SummonCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if(!(sender instanceof Player player)) return false;
            if(player.getPermissionLevel() < 3) return false;
            return true;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        var entityArgument = ArgumentType.EntityType(ARG_TYPE);
        var amountArgument = ArgumentType.Integer(ARG_AMOUNT).min(1).max(1024).setDefaultValue(1);

        super.addSyntax((sender, context) -> {
            Player player = (Player) sender;
            Instance instance = player.getInstance();
            Pos position = player.getPosition();
            EntityType entityType = context.get(ARG_TYPE);
            int amount = context.get(ARG_AMOUNT);

            for(int i = 0; i < amount; i++){
                Entity entity = new Entity(entityType);

                entity.setInstance(instance, position);
            }

            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have successfully summoned " + amount + "x " + StringUtil.pluralize(entityType.toString(), amount) + "!"));
        }, entityArgument, amountArgument);
    }
}