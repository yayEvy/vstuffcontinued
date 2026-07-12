package yay.evy.everest.vstuff.infrastructure.commands.debug;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import yay.evy.everest.vstuff.content.ropes.phys_ropes.ReworkedPhysRopeFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PhysRopeFactoryDebug {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("factory")
                .then(factoryValue(
                        "length",
                        () -> ReworkedPhysRopeFactory.SEGMENT_LENGTH,
                        length -> ReworkedPhysRopeFactory.SEGMENT_LENGTH = length,
                        FloatArgumentType::floatArg, FloatArgumentType::getFloat
                ))
                .then(factoryValue(
                        "radius",
                        () -> ReworkedPhysRopeFactory.SEGMENT_RADIUS,
                        radius -> ReworkedPhysRopeFactory.SEGMENT_RADIUS = radius,
                        DoubleArgumentType::doubleArg, DoubleArgumentType::getDouble
                ))
                .then(factoryValue(
                        "mass",
                        () -> ReworkedPhysRopeFactory.SEGMENT_MASS,
                        mass -> ReworkedPhysRopeFactory.SEGMENT_MASS = mass,
                        DoubleArgumentType::doubleArg, DoubleArgumentType::getDouble
                ));
    }

    private static <T> LiteralArgumentBuilder<CommandSourceStack> factoryValue(String name, Supplier<T> getter, Consumer<T> setter, Supplier<ArgumentType<T>> argType, BiFunction<CommandContext<CommandSourceStack>, String, T> argGetter) {
        return Commands.literal(name)
                .executes(ctx -> {
                    Component msg = Component.literal("Current " + name + " is " + getter.get());
                    ctx.getSource().sendSuccess(() -> msg, false);

                    return 1;
                })
                .then(Commands.argument("setTo", argType.get())
                        .executes(ctx -> {
                            setter.accept(argGetter.apply(ctx, "setTo"));

                            Component msg = Component.literal("Set " + name + " to " + getter.get());
                            ctx.getSource().sendSuccess(() -> msg, false);

                            return 1;
                        }));
    }

}
