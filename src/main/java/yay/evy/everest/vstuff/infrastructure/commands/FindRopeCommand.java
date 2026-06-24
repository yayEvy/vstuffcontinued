package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

public class FindRopeCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("findRope")
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    Integer id = RopeUtils.findRopeId(level, player);

                    if (id == null) {
                        ctx.getSource().sendFailure(Component.literal("No rope found"));
                        return 0;
                    }

                    Component message = Component.literal("Found rope with id " + id + ".");

                    ctx.getSource().sendSuccess(() -> message, false);
                    return 1;
                });
    }

}
