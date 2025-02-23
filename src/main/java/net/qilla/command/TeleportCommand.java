package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;
import net.qilla.util.NumberUtil;

import java.util.List;

public final class TeleportCommand extends Command {

    private static final String NAME = "teleport";
    private static final String[] ALIASES = {"tp"};

    private static final String ARG_POS = "position";
    private static final String ARG_ENTITY = "entity";
    private static final String ARG_TO_TARGET = "target";

    public TeleportCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if(!(sender instanceof Player player)) return false;
            if(player.getPermissionLevel() < 3) return false;
            return true;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        ArgumentRelativeBlockPosition argRelativeBlockPos = ArgumentType.RelativeBlockPosition(ARG_POS);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;
            RelativeVec relativeVec = context.get(ARG_POS);
            Pos pos = relativeVec.fromSender(sender).asPosition().withView(player.getPosition());

            player.teleport(pos);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" +
                    NumberUtil.decimalTruncation(pos.x(), 1) + ", " +
                    NumberUtil.decimalTruncation(pos.y(), 1) + ", " +
                    NumberUtil.decimalTruncation(pos.z(), 1) + "</yellow>!"));
        }, argRelativeBlockPos);

        ArgumentEntity argEntity = ArgumentType.Entity(ARG_ENTITY);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;
            EntityFinder entityFinder = context.get(argEntity);
            Entity entity = entityFinder.find(sender).getFirst();
            Pos pos = entity.getPosition().withView(player.getPosition());

            if(entity instanceof Player playerTarget) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" + playerTarget.getUsername() + "</yellow>!"));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" + entity.getUuid() + "</yellow>!"));
            }
            player.teleport(pos);
        }, argEntity);

        ArgumentEntity argToTarget = ArgumentType.Entity(ARG_TO_TARGET).singleEntity(true);

        super.addSyntax((sender, context) -> {
            EntityFinder targetEntityFinder = context.get(argEntity);
            List<Entity> targetEntity = targetEntityFinder.find(sender);
            EntityFinder toEntityFinder = context.get(argToTarget);
            Entity toEntity = toEntityFinder.find(sender).getFirst();

            for(Entity entity : targetEntity) {
                Pos pos = toEntity.getPosition().withView(entity.getPosition());
                if(entity instanceof Player playerTarget) {
                    if(toEntity instanceof Player playerTo) {
                        playerTarget.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" + playerTo.getUsername() + "</yellow>!"));
                    } else {
                        playerTarget.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" + toEntity.getUuid() + "</yellow>!"));
                    }
                }
                entity.teleport(pos);
            }
        }, argEntity, argToTarget);

        super.addSyntax((sender, context) -> {
            EntityFinder targetEntityFinder = context.get(argEntity);
            List<Entity> targetEntity = targetEntityFinder.find(sender);
            RelativeVec relativeVec = context.get(ARG_POS);

            for(Entity entity : targetEntity) {
                Pos pos = relativeVec.fromSender(sender).asPosition().withView(entity.getPosition());
                if(entity instanceof Player playerTarget) {
                    playerTarget.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" +
                            NumberUtil.decimalTruncation(pos.x(), 1) + ", " +
                            NumberUtil.decimalTruncation(pos.y(), 1) + ", " +
                            NumberUtil.decimalTruncation(pos.z(), 1) + "</yellow>!"));
                }
                entity.teleport(pos);
            }
        }, argEntity, argRelativeBlockPos);
    }
}