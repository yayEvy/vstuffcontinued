package dev.flarelog.vstuff.infrastructure.commands.debug;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import dev.flarelog.vstuff.content.ropes.RopeFactory;
import org.valkyrienskies.core.internal.joints.VSJoint;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PhysRopeFactoryDebug {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("factory")
                .then(factoryValue(
                        "length", () -> RopeFactory.SEGMENT_LENGTH,
                        length -> RopeFactory.SEGMENT_LENGTH = length,
                        0.8f, Float::parseFloat
                ))
                .then(factoryValue(
                        "radius", () -> RopeFactory.SEGMENT_RADIUS,
                        radius -> RopeFactory.SEGMENT_RADIUS = radius,
                        0.125, Double::parseDouble
                ))
                .then(factoryValue(
                        "mass", () -> RopeFactory.SEGMENT_MASS,
                        mass -> RopeFactory.SEGMENT_MASS = mass,
                        1.0, Double::parseDouble
                ))
                .then(factoryValue(
                        "compliance", () -> RopeFactory.JOINT_COMPLIANCE,
                        compliance -> RopeFactory.JOINT_COMPLIANCE = compliance,
                        VSJoint.DEFAULT_COMPLIANCE, Double::parseDouble
                ))
                .then(factoryValue(
                        "tolerance", () -> RopeFactory.JOINT_TOLERANCE,
                        tolerance -> RopeFactory.JOINT_TOLERANCE = tolerance,
                        0.1f, Float::parseFloat
                ))
                .then(factoryValue(
                        "stiffness", () -> RopeFactory.JOINT_STIFFNESS,
                        stiffness -> RopeFactory.JOINT_STIFFNESS = stiffness,
                        1e8f, Float::parseFloat
                ))
                .then(factoryValue(
                        "damping", () -> RopeFactory.JOINT_DAMPING,
                        damping -> RopeFactory.JOINT_DAMPING = damping,
                        null, Float::parseFloat
                ))
                ;
    }

    private static <T> LiteralArgumentBuilder<CommandSourceStack> factoryValue(String name, Supplier<T> getter, Consumer<T> setter, T defaultValue, Function<String, T> parser) {
        return Commands.literal(name)
                .executes(ctx -> {
                    Component msg = Component.literal("Current " + name + " is " + getter.get());
                    ctx.getSource().sendSuccess(() -> msg, false);

                    return 1;
                })
                .then(Commands.literal("setDefault")
                        .executes(ctx -> {
                            setter.accept(defaultValue);

                            Component msg = Component.literal("Set " + name + " to " + getter.get());
                            ctx.getSource().sendSuccess(() -> msg, false);

                            return 1;
                        }))
                .then(Commands.argument("setTo", StringArgumentType.string())
                        .executes(ctx -> {
                            String value = StringArgumentType.getString(ctx, "setTo");

                            if (value.equals("null")) {
                                try {
                                    setter.accept(null);
                                } catch (Exception e) {
                                    Component msg = Component.literal("That value cannot be null!");
                                    ctx.getSource().sendSuccess(() -> msg, false);
                                    return 1;
                                }
                            } else {
                                try {
                                    T setTo = parser.apply(value);
                                    setter.accept(setTo);
                                } catch (Exception e) {
                                    Component msg = Component.literal("Invalid value!");
                                    ctx.getSource().sendSuccess(() -> msg, false);
                                    return 1;
                                }
                            }

                            Component msg = Component.literal("Set " + name + " to " + getter.get());
                            ctx.getSource().sendSuccess(() -> msg, false);

                            return 1;
                        }));
    }

}
