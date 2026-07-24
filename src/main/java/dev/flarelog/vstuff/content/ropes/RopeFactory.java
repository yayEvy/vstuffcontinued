
package dev.flarelog.vstuff.content.ropes;

import dev.flarelog.vstuff.content.physics.VSUtil;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import dev.flarelog.vstuff.content.ropes.util.ILikeRopes;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.bodies.VsBodyCreateData;
import org.valkyrienskies.core.api.bodies.VsBodyDefaults;
import org.valkyrienskies.core.api.bodies.shape.BodyShapeData;
import org.valkyrienskies.core.api.bodies.shape.SphereBodyShapeData;
import org.valkyrienskies.core.impl.bodies.properties.BodyKinematicsImpl;
import org.valkyrienskies.core.impl.bodies.properties.BodyTransformImpl;
import org.valkyrienskies.core.impl.game.bodies.BodyInertiaDataImpl;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import dev.flarelog.vstuff.infrastructure.config.VStuffConfigs;
import dev.flarelog.vstuff.content.ropes.style.RopeStyleManager;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.util.RopeUtil;
import dev.flarelog.vstuff.internal.utility.TagUtils;
import dev.flarelog.vstuff.content.ropes.util.RopePosData;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.flarelog.vstuff.content.physics.VSUtil.getGTPA;

public class RopeFactory {

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

    public static final Logger LOGGER = LogManager.getLogger("PhysRopeFactory");

    public static class PhysRopeResult {
        public final Rope rope;
        public final boolean valid;
        public final String message;

        protected PhysRopeResult(Rope rope, boolean valid, String message) {
            this.rope = rope;
            this.valid = valid;
            this.message = message;
        }

        public static PhysRopeResult withMessage(String message) {
            return new PhysRopeResult(null, false, message);
        }

        public static PhysRopeResult validResult(Rope rope) {
            return new PhysRopeResult(rope, true, null);
        }
    }

    public record PhysRopeContext(ServerLevel level, RopePosData posData0, RopePosData posData1, String dimId) {}

    public static PhysRopeResult tryCreateNewRope(ServerLevel level, ItemStack ropeItem, BlockPos blockPos0, BlockPos blockPos1, Entity entity) {
        String dimId = ropeItem.getOrCreateTagElement("data").getString("dim");

        Long ship0 = VSUtil.getLoadedShipIdAtPos(level, blockPos0);
        Long ship1 = VSUtil.getLoadedShipIdAtPos(level, blockPos1);

        float length = (float) RopeUtil.getWorldPos(level, blockPos0, ship0).distance(RopeUtil.getWorldPos(level, blockPos1, ship1)) + 0.5f;

        if (!dimId.equals(level.dimension().location().toString()))
            return PhysRopeResult.withMessage("message.rope.interdimensional_fail");
        if (length > VStuffConfigs.server().ropeMaxLength.get())
            return PhysRopeResult.withMessage("message.rope.too_long");

        Pair<RopePosData, RopePosData> posDataPair = RopePosData.create(level, ship0, ship1, blockPos0, blockPos1);
        RopePosData posData0 = posDataPair.component1();
        RopePosData posData1 = posDataPair.component2();

        PhysRopeContext ctx = new PhysRopeContext(level, posData0, posData1, dimId);

        return PhysRopeResult.validResult(createNewRope(
                ctx, RopeStyleManager.get(ropeItem.getOrCreateTag()), ((ILikeRopes) ropeItem.getItem()).getType()
        ));
    }

    public static Rope createNewRope(PhysRopeContext ctx, ResourceKey<RopeStyle> style, ResourceKey<RopeType> type) {
        RopePosData posData0 = ctx.posData0;
        RopePosData posData1 = ctx.posData1;
        ServerLevel level = ctx.level;

        Vector3d worldStart = posData0.getWorldPos(level);
        Vector3d worldEnd = posData1.getWorldPos(level);

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
        List<VSJoint> joints = makeJoints(segments, spacing, type, level);

        Rope physRope = new Rope(ctx.posData0, ctx.posData1, type, style, segments);

        createJoints(ctx.level, physRope, joints);

        RopeManager.get(level).addRope(physRope);

        return physRope;
    }

    private static List<RopeSegment> createSegmentBodies(PhysRopeContext ctx, int segmentCount, Vector3d spawnStart, Vector3d spawnEnd) {
        List<RopeSegment> segments = new ArrayList<>();
        Vector3d step = new Vector3d(spawnEnd).sub(spawnStart).div(segmentCount);

        Long lastId = ctx.posData0.shipId();
        Vector3d lastPos = ctx.posData0.localPos();

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

        segments.add(new RopeSegment(lastId, ctx.posData1.shipId(), lastPos, ctx.posData1.localPos()));

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

    private static List<VSJoint> makeJoints(List<RopeSegment> segments, double spacing, ResourceKey<RopeType> typeKey, ServerLevel level) {
        List<VSJoint> joints = new ArrayList<>();
        float maxLength = (float) (spacing * (1 + SAG_FACTOR));

        RopeType type = level.registryAccess().registryOrThrow(VStuffRegistries.ROPE_TYPE).get(typeKey);
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
}