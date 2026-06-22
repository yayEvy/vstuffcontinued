package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

public class DeleteRopesCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> removeAll() {
        return Commands.literal("removeAllRopes")
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();

                    RopeManager manager = RopeManager.get(level);

                    int removed = 0;

                    for (Integer id : manager.getIdList()) {
                        RopeFactory.removeRope(level, id);
                        removed++;
                    }

                    Component message = Component.literal("Successfully removed " + removed + " rope" + (removed == 1 ? "." : "s."));

                    ctx.getSource().sendSuccess(() -> message, false);

                    return 1;
                });
    }

    public static ArgumentBuilder<CommandSourceStack, ?> remove() {
        return Commands.literal("remove")
                .then(Commands.argument("id", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            int toRemove = IntegerArgumentType.getInteger(ctx, "id");

                            ServerLevel level = ctx.getSource().getLevel();
                            RopeManager manager = RopeManager.get(level);

                            return r(ctx.getSource(), manager, toRemove);
                        })
                ).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    Player player = ctx.getSource().getPlayerOrException();
                    RopeManager manager = RopeManager.get(level);

                    Integer toRemove = RopeUtils.findRopeId(level, player);

                    if (toRemove == null) {
                        ctx.getSource().sendFailure(Component.literal("No rope found"));
                        return 0;
                    }

                    return r(ctx.getSource(), manager, toRemove);
                });
    }

    private static int r(CommandSourceStack sourceStack, RopeManager manager, int toRemove) {
        if (!manager.hasRope(toRemove)) {
            sourceStack.sendFailure(Component.literal("Could not find rope with id " + toRemove + "."));
            return 0;
        }

        manager.removeRope(toRemove);

        Component message = Component.literal("Successfully removed rope with id " + toRemove + ".");

        sourceStack.sendSuccess(() -> message, false);
        return 1;
    }
}