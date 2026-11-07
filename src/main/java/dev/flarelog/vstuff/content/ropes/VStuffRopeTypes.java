package dev.flarelog.vstuff.content.ropes;

import com.mojang.datafixers.util.Either;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.type.DistanceJointParams;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import org.valkyrienskies.core.api.bodies.VsBodyDefaults;
import org.valkyrienskies.core.api.bodies.shape.SphereBodyShapeData;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;

public class VStuffRopeTypes {
    public static double JOINT_COMPLIANCE = 1e4f;
    public static Float JOINT_TOLERANCE = 1e3f;
    public static Float JOINT_STIFFNESS = 1e7f;
    public static Float JOINT_DAMPING = 1e5f;
    public static float JOINT_MAX_FORCE_TORQUE = 1e1f;
    public static double SEGMENT_RADIUS = 0.125;
    public static double SEGMENT_MASS = 50;

    public static void bootstrap(BootstapContext<RopeType> ctx) {
        register(ctx, "normal", RopeType.create(
                DistanceJointParams.builder()
                        .maxForceTorque(new VSJointMaxForceTorque(JOINT_MAX_FORCE_TORQUE, JOINT_MAX_FORCE_TORQUE))
                        .minDistance(0f)
                        .maxDistance(Either.right("${maxDistance}"))
                        .tolerance(JOINT_TOLERANCE)
                        .compliance(JOINT_COMPLIANCE)
                        .stiffness(JOINT_STIFFNESS)
                        .damping(JOINT_DAMPING)
                        .build(),
                RopeType.BodyData.create(
                        new SphereBodyShapeData(SEGMENT_RADIUS),
                        SEGMENT_MASS,
                        VsBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT,
                        VsBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT,
                        VsBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT
                ),
                false
                )
        );
    }

    private static void register(BootstapContext<RopeType> ctx, String name, RopeType type) {
        ctx.register(ResourceKey.create(VStuffRegistries.ROPE_TYPE, VStuff.asResource(name)), type);
    }
}
