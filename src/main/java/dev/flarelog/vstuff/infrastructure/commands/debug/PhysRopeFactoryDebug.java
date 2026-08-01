package dev.flarelog.vstuff.infrastructure.commands.debug;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.flarelog.vstuff.content.ropes.NewRopeFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.valkyrienskies.core.internal.joints.VSJoint;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PhysRopeFactoryDebug {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("factory")
                .then(factoryValue(
                        "length", () -> NewRopeFactory.SEGMENT_LENGTH,
                        length -> NewRopeFactory.SEGMENT_LENGTH = length,
                        0.8f, Float::parseFloat
                ))
                .then(factoryValue(
                        "radius", () -> NewRopeFactory.SEGMENT_RADIUS,
                        radius -> NewRopeFactory.SEGMENT_RADIUS = radius,
                        0.125, Double::parseDouble
                ))
                .then(factoryValue(
                        "mass", () -> NewRopeFactory.SEGMENT_MASS,
                        mass -> NewRopeFactory.SEGMENT_MASS = mass,
                        1.0, Double::parseDouble
                ))
                .then(factoryValue(
                        "compliance", () -> NewRopeFactory.JOINT_COMPLIANCE,
                        compliance -> NewRopeFactory.JOINT_COMPLIANCE = compliance,
                        VSJoint.DEFAULT_COMPLIANCE, Double::parseDouble
                ))
                .then(factoryValue(
                        "tolerance", () -> NewRopeFactory.JOINT_TOLERANCE,
                        tolerance -> NewRopeFactory.JOINT_TOLERANCE = tolerance,
                        0.1f, Float::parseFloat
                ))
                .then(factoryValue(
                        "stiffness", () -> NewRopeFactory.JOINT_STIFFNESS,
                        stiffness -> NewRopeFactory.JOINT_STIFFNESS = stiffness,
                        1e8f, Float::parseFloat
                ))
                .then(factoryValue(
                        "damping", () -> NewRopeFactory.JOINT_DAMPING,
                        damping -> NewRopeFactory.JOINT_DAMPING = damping,
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
