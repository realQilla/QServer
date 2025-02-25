package net.qilla.command;

import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;

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
            sender.sendMessage(this::malformedCommand);
        });

        ArgumentRelativeBlockPosition argRelativeBlockPos = ArgumentType.RelativeBlockPosition(ARG_POS);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;
            RelativeVec relativeVec = context.get(ARG_POS);
            Pos pos = relativeVec.fromSender(sender).asPosition().withView(player.getPosition());

            player.teleport(pos);
            player.sendMessage(this.teleport(pos.x(), pos.y(), pos.z()));
        }, argRelativeBlockPos);

        ArgumentEntity argEntity = ArgumentType.Entity(ARG_ENTITY);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        }, (sender, context) -> {
            Player player = (Player) sender;
            EntityFinder entityFinder = context.get(argEntity);

            if(entityFinder.find(sender).isEmpty()) {
                player.sendMessage(this.noEntitiesFound());
                return;
            }

            Entity entity = entityFinder.find(sender).getFirst();
            Pos pos = entity.getPosition().withView(player.getPosition());

            if(entity instanceof Player playerTarget) player.sendMessage(this.teleport(playerTarget.getUsername()));
            else player.sendMessage(this.teleport(entity.getUuid().toString()));

            player.teleport(pos);
        }, argEntity);

        ArgumentEntity argToTarget = ArgumentType.Entity(ARG_TO_TARGET).singleEntity(true);

        super.addSyntax((sender, context) -> {
            EntityFinder targetEntityFinder = context.get(argEntity);
            List<Entity> targetEntity = targetEntityFinder.find(sender);
            EntityFinder toEntityFinder = context.get(argToTarget);

            if(toEntityFinder.find(sender).isEmpty()) {
                sender.sendMessage(this.noEntitiesFound());
                return;
            }

            Entity toEntity = toEntityFinder.find(sender).getFirst();

            for(Entity entity : targetEntity) {
                Pos pos = toEntity.getPosition().withView(entity.getPosition());
                if(entity instanceof Player playerTarget) {
                    if(toEntity instanceof Player playerTo) {
                        playerTarget.sendMessage(this.teleport(playerTo.getUsername()));
                    } else {
                        playerTarget.sendMessage(this.teleport(toEntity.getUuid().toString()));
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
                    playerTarget.sendMessage(this.teleport(pos.x(), pos.y(), pos.z()));
                }
                entity.teleport(pos);
            }
        }, argEntity, argRelativeBlockPos);
    }

    private @NotNull Component malformedCommand() {
        return MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments.");
    }

    private @NotNull Component noEntitiesFound() {
        return MiniMessage.miniMessage().deserialize("<red>No entities were found!");
    }

    private @NotNull Component teleport(String username) {
        return MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" + username + "</yellow>!");
    }

    private @NotNull Component teleport(double x, double y, double z) {
        return MiniMessage.miniMessage().deserialize("<green>You have been teleported to <yellow>" +
                NumberUtil.decimalTruncation(x, 1) + ", " +
                NumberUtil.decimalTruncation(y, 1) + ", " +
                NumberUtil.decimalTruncation(z, 1) + "</yellow>!");
    }
}