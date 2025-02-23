package net.qilla.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.qilla.util.StringUtil;
import java.util.ArrayList;
import java.util.List;

public final class KillCommand extends Command {

    private static final String NAME = "kill";
    private static final String[] ALIASES = {};

    private static final String ARG_ENTITY = "entity";

    public KillCommand() {
        super(NAME, ALIASES);

        super.setCondition((sender, input) -> {
            if((sender instanceof Player player && player.getPermissionLevel() > 2)) return true;
            if(sender instanceof ConsoleSender) return true;
            return false;
        });

        super.setDefaultExecutor((sender, context) -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You specified a malformed command, try again with valid arguments."));
        });

        ArgumentEntity entityArgument = ArgumentType.Entity(ARG_ENTITY);

        super.addConditionalSyntax((sender, commandStr) -> {
            return sender instanceof Player;
        },(sender, context) -> {
            Player player = (Player) sender;
            this.killEntity(player);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have successfully killed <yellow>" + player.getUsername() + "</yellow>!"));
        });
        super.addSyntax((sender, context) -> this.killLogic(sender, context, entityArgument), entityArgument);
    }

    private void killLogic(CommandSender sender, CommandContext context, ArgumentEntity entityArgument) {
        EntityFinder finder = context.get(entityArgument);
        List<Entity> entities = finder.find(sender);

        if(entities.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>No entities were found!"));
            return;
        }

        if(entities.size() == 1) {
            String name = this.killEntity(entities.getFirst());
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have successfully killed <yellow>" + name + "</yellow>!"));
            return;
        }
        List<String> strList = new ArrayList<>();

        for(Entity target : entities) strList.add(this.killEntity(target));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have successfully killed <yellow>" +
                StringUtil.toLimitedNameList(strList, ", ", 1) + " and " + (strList.size() - 1) + " " + StringUtil.pluralize("other", strList.size() - 1) + "</yellow>!"));
    }

    private String killEntity(Entity target) {
        if(target instanceof Player playerTarget) {
            playerTarget.kill();
            return playerTarget.getUsername();
        } else if(target instanceof LivingEntity livingTarget) {
            livingTarget.kill();
        } else target.remove();
        return String.valueOf(target.getUuid());
    }
}