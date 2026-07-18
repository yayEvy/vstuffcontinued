package dev.flarelog.vstuff.infrastructure.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.styling.RopeStyleManager;
import dev.flarelog.vstuff.internal.styling.data.RopeStyle;
import dev.flarelog.vstuff.internal.utility.ShipUtils;

public class CreateRopeCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("createRope")
            .then(Commands.argument("pos0", BlockPosArgument.blockPos())
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                    .then(Commands.argument("style", ResourceKeyArgument.key(VStuffRegistries.ROPE_STYLE))
                        .executes(ctx -> {
                            ServerLevel serverLevel = ctx.getSource().getLevel();

                            BlockPos pos0 = BlockPosArgument.getBlockPos(ctx, "pos0");
                            BlockPos pos1 = BlockPosArgument.getBlockPos(ctx, "pos1");

                            Long ship0 = ShipUtils.getShipIdAtPos(serverLevel, pos0);
                            Long ship1 = ShipUtils.getShipIdAtPos(serverLevel, pos1);

                            Entity user = ctx.getSource().getEntity();

                            ResourceKey<?> uncheckedStyle = ctx.getArgument("style", ResourceKey.class);
                            ResourceKey<RopeStyle> style = uncheckedStyle.cast(VStuffRegistries.ROPE_STYLE).orElse(RopeStyleManager.DEFAULT_KEY);

                            // todo reimplement

                            return 1;
                        })
                    )
                )
            );
    }
}
