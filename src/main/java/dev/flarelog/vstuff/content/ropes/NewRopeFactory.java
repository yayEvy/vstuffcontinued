package dev.flarelog.vstuff.content.ropes;

import com.mojang.datafixers.util.Pair;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import dev.flarelog.vstuff.content.ropes.util.LocalPosAndBodyId;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;
import dev.flarelog.vstuff.infrastructure.config.VStuffConfigs;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.bodies.VsBodyCreateData;
import org.valkyrienskies.core.api.bodies.VsBodyDefaults;
import org.valkyrienskies.core.api.bodies.shape.BodyShapeData;
import org.valkyrienskies.core.api.bodies.shape.SphereBodyShapeData;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.bodies.properties.BodyKinematicsImpl;
import org.valkyrienskies.core.impl.bodies.properties.BodyTransformImpl;
import org.valkyrienskies.core.impl.game.bodies.BodyInertiaDataImpl;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.flarelog.vstuff.content.physics.VSUtil.getGTPA;

public class NewRopeFactory {

    public static float SEGMENT_LENGTH = 0.8f;
    public static double SEGMENT_RADIUS = 0.125;
    public static double SEGMENT_MASS = 1;
    public static double JOINT_COMPLIANCE = 1e4f;
    public static Float JOINT_TOLERANCE = 1e3f;
    public static Float JOINT_STIFFNESS = 1e7f;
    public static Float JOINT_DAMPING = 1e5f;
    public static float JOINT_MAX_FORCE_TORQUE = 1e1f;
    private static final float ANCHOR_OFFSET = 0.1f;
    public static double SAG_FACTOR = 0.08; // higher = more sag

    public static final Logger LOGGER = LogManager.getLogger("VStuffRopeFactory");

    public static RopeResult tryCreateRope(ServerLevel level, LocalPosAndBodyId data0, LocalPosAndBodyId data1, ResourceKey<RopeType> type, ResourceKey<RopeStyle> style) {
        return tryCreateRope(
                level, data0, data1,
                level.registryAccess().registryOrThrow(VStuffRegistries.ROPE_TYPE).get(type),
                style
        );
    }

    public static RopeResult tryCreateRope(ServerLevel level, LocalPosAndBodyId data0, LocalPosAndBodyId data1, RopeType type, ResourceKey<RopeStyle> style) {
        float length = (float) data0.getWorldPos(level).distance(data1.getWorldPos(level)) + 0.5f;

        if (length > VStuffConfigs.server().ropeMaxLength.get())
            return RopeResult.withMessage("message.rope.too_long");

        RopeContext ctx = new RopeContext(level, data0, data1);

        return RopeResult.validResult(createRope(
                ctx, style, type
        ));
    }

    public static Rope createRope(RopeContext ctx, ResourceKey<RopeStyle> styleKey, RopeType type) {
        LocalPosAndBodyId first = ctx.data0();
        LocalPosAndBodyId second = ctx.data1();

        ServerLevel level = ctx.level();

        Registry<RopeType> typeRegistry = level.registryAccess().registryOrThrow(VStuffRegistries.ROPE_TYPE);
        Registry<RopeStyle> styleRegistry = level.registryAccess().registryOrThrow(VStuffRegistries.ROPE_STYLE);

        Vector3d worldStart = first.getWorldPos(level);
        Vector3d worldEnd = second.getWorldPos(level);

        if (worldStart.distance(worldEnd) < 0.01) {
            LOGGER.warn("Attachment points are too close, stopping phys rope creation.");
            return null;
        }

        Vector3d dir = new Vector3d(worldEnd).sub(worldStart).normalize();
        Vector3d spawnStart = new Vector3d(worldStart).add(new Vector3d(dir).mul(ANCHOR_OFFSET));
        Vector3d spawnEnd = new Vector3d(worldEnd).sub(new Vector3d(dir).mul(ANCHOR_OFFSET));

        double totalDistance = spawnStart.distance(spawnEnd);
        int segmentCount = Math.max(1, (int) Math.round(totalDistance / SEGMENT_LENGTH));
        double spacing = totalDistance / segmentCount;

        List<RopeSegment> segments = createSegmentBodies(ctx, segmentCount, spawnStart, spawnEnd);
        List<VSJoint> joints = makeJoints(segments, spacing, type);

        Rope physRope = new Rope(ctx.data0(), ctx.data1(), type, styleKey, segments);

        createJoints(ctx.level, physRope, joints);

        RopeManager.get(level).addRope(physRope);

        return physRope;
    }

    public static List<RopeSegment> createSegmentBodies(RopeContext ctx, int segmentCount, Vector3d spawnStart, Vector3d spawnEnd) {
        List<RopeSegment> segments = new ArrayList<>();
        Vector3d step = new Vector3d(spawnEnd).sub(spawnStart).div(segmentCount);

        Long lastId = ctx.data0.id();
        Vector3d lastPos = ctx.data0.pos();

        for (int i = 0; i < segmentCount - 1; i++) {
            Vector3d bodyPos = new Vector3d(spawnStart).add(new Vector3d(step).mul(i + 1));
            ServerVsBody body = createBody(ctx.level, bodyPos);
            if (body != null) {
                Long id = body.getId();
                Vector3d pos = new Vector3d();

                segments.add(new RopeSegment(lastId, id, lastPos, pos));

                lastId = id;
                lastPos = pos;
            }
        }

        segments.add(new RopeSegment(lastId, ctx.data1.id(), lastPos, ctx.data1.pos()));

        return segments;
    }

    private static ServerVsBody createBody(ServerLevel level, Vector3d pos) {
        VsBodyCreateData bodyCreateData = createRopeBodyData(level, pos);

        try {
            return VSGameUtilsKt.getShipObjectWorld(level).createBody(bodyCreateData);
        } catch (Exception e) {
            LOGGER.error("Failed to create segment body at {}: {}", pos, e.getMessage());
            return null;
        }
    }

    private static VsBodyCreateData createRopeBodyData(ServerLevel level, Vector3d pos) {
        BodyShapeData shapeData = new SphereBodyShapeData(SEGMENT_RADIUS);

        return new VsBodyCreateData(
                VSGameUtilsKt.getDimensionId(level),
                new BodyInertiaDataImpl(shapeData.getAabb().center(new Vector3d()), SEGMENT_MASS, new Matrix3d()),
                new BodyKinematicsImpl(new Vector3d(), new Vector3d(), new BodyTransformImpl(new Vector3d(pos), new Quaterniond(), new Vector3d(1), new Vector3d())),
                shapeData,
                false,
                VsBodyDefaults.DEFAULT_COLLISION_MASK,
                VsBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT,
                VsBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT,
                VsBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT
        );
    }

    private static List<VSJoint> makeJoints(List<RopeSegment> segments, double spacing, RopeType type) {
        List<VSJoint> joints = new ArrayList<>();
        float maxLength = (float) (spacing * (1 + SAG_FACTOR));

        RopeSegment first = segments.remove(0);
        RopeSegment last = segments.remove(segments.size() - 1);

        if (type == null) {
            throw new RuntimeException("WTF NULL ROPE TYPE??!!??? MEOW!! MEOW!! MEOW!!");
        }

        VSJoint firstJoint = type.getEndJointWith(first.id0(),
                new VSJointPose(first.pos0(), new Quaterniond()),
                first.id1(),
                new VSJointPose(first.pos1(), new Quaterniond()),
                maxLength).serialized();

        joints.add(firstJoint);

        VSJoint lastJoint = type.getEndJointWith(last.id0(),
                new VSJointPose(last.pos0(), new Quaterniond()),
                last.id1(),
                new VSJointPose(last.pos1(), new Quaterniond()),
                maxLength).serialized();

        joints.add(lastJoint);

        for (RopeSegment segment : segments) {
            VSJoint joint = type.getConnectingPhysBodyJointWith(
                    segment.id0(), new VSJointPose(segment.pos0(), new Quaterniond()),
                    segment.id1(), new VSJointPose(segment.pos1(), new Quaterniond()),
                    maxLength
            );
            joint.setShouldBeSerialized(true);

            joints.add(joint);
        }

        return joints;
    }

    private static void createJoints(ServerLevel level, Rope rope, List<VSJoint> joints) {
        rope.jointIds = new ArrayList<>();
        GameToPhysicsAdapter gtpa = getGTPA(level);

        AtomicInteger remaining = new AtomicInteger(joints.size());
        AtomicBoolean failed = new AtomicBoolean();

        for (VSJoint joint : joints) {
            gtpa.addJoint(joint, 5, id -> { // consumer lambda of doom and despair
                if (id == -1) {
                    LOGGER.warn("Invalid joint id received when creating phys rope!");
                    failed.set(true);
                } else {
                    rope.jointIds.add(id);
                }

                if (remaining.decrementAndGet() == 0 && failed.get()) {
                    LOGGER.info("Failed was true after all joints have been created, discarding phys rope.");
                    discardRope(rope);
                }
            });
        }
    }

    // todo implement
    private static void discardRope(Rope rope) {

    }

    // todo implement
    public static void removeRope(Rope rope) {

    }



    public record RopeContext(ServerLevel level, LocalPosAndBodyId data0, LocalPosAndBodyId data1) {}


}
