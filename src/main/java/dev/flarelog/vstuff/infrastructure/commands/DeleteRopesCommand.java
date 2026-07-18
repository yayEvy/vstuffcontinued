package dev.flarelog.vstuff.infrastructure.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.flarelog.vstuff.content.ropes.phys_ropes.ReworkedPhysRopeFactory;
import dev.flarelog.vstuff.content.ropes.phys_ropes.ReworkedPhysRopeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import dev.flarelog.vstuff.internal.utility.RopeUtils;

public class DeleteRopesCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> removeAll() {
        return Commands.literal("removeAllRopes")
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();

                    ReworkedPhysRopeManager manager = ReworkedPhysRopeManager.get(level);

                    int removed = 0;

                    // todo reimplement

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
                            ReworkedPhysRopeManager manager = ReworkedPhysRopeManager.get(level);

                            return r(ctx.getSource(), manager, toRemove);
                        })
                ).executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    Player player = ctx.getSource().getPlayerOrException();
                    ReworkedPhysRopeManager manager = ReworkedPhysRopeManager.get(level);

                    Integer toRemove = RopeUtils.findRopeId(level, player);

                    if (toRemove == null) {
                        ctx.getSource().sendFailure(Component.literal("No rope found"));
                        return 0;
                    }

                    return r(ctx.getSource(), manager, toRemove);
                });
    }

    private static int r(CommandSourceStack sourceStack, ReworkedPhysRopeManager manager, int toRemove) {
        if (!manager.hasRope(toRemove)) {
            sourceStack.sendFailure(Component.literal("Could not find rope with id " + toRemove + "."));
            return 0;
        }

        // todo reimplement

        Component message = Component.literal("Successfully removed rope with id " + toRemove + ".");

        sourceStack.sendSuccess(() -> message, false);
        return 1;
    }
}