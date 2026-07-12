package yay.evy.everest.vstuff.infrastructure.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

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

                            RopeFactory.createNewRope(serverLevel, ship0, ship1, pos0, pos1, style, user);

                            return 1;
                        })
                    )
                )
            );
    }
}
