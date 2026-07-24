package dev.flarelog.vstuff.content.ropes;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.type.DistanceJointParams;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;

import static dev.flarelog.vstuff.content.ropes.RopeFactory.*;

public class VStuffRopeTypes {
    public static void bootstrap(BootstapContext<RopeType> ctx) {
        register(ctx, "normal", new RopeType(
                new DistanceJointParams.Builder()
                        .maxForceTorque(new VSJointMaxForceTorque(JOINT_MAX_FORCE_TORQUE, JOINT_MAX_FORCE_TORQUE))
                        .minDistance(0f)
                        .tolerance(JOINT_TOLERANCE)
                        .stiffness(JOINT_STIFFNESS)
                        .damping(JOINT_DAMPING)
                        .build(),
                new DistanceJointParams.Builder()
                        .maxForceTorque(new VSJointMaxForceTorque(JOINT_MAX_FORCE_TORQUE, JOINT_MAX_FORCE_TORQUE))
                        .minDistance(0f)
                        .tolerance(JOINT_TOLERANCE)
                        .stiffness(JOINT_STIFFNESS)
                        .damping(JOINT_DAMPING)
                        .build(),
                true
                )
        );
    }

    private static void register(BootstapContext<RopeType> ctx, String name, RopeType type) {
        ctx.register(ResourceKey.create(VStuffRegistries.ROPE_TYPE, VStuff.asResource(name)), type);
    }
}
