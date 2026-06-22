package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.joml.*;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.bodies.properties.BodyInertia;
import org.valkyrienskies.core.api.bodies.properties.BodyKinematics;
import org.valkyrienskies.core.impl.bodies.properties.BodyKinematicsFactory;
import org.valkyrienskies.core.impl.game.bodies.BodyInertiaDataImpl;
import org.valkyrienskies.core.internal.game.StandaloneBodyCreateData;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.core.internal.physics.VSSphereCollisionShapeData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PhysRopeFactory
{

    private static final double SEGMENT_RADIUS = 0.2;
    private static final double SEGMENT_MASS = 1;
    private static final float SEGMENT_LENGTH = 1f;
    private static final float INTER_SEGMENT_MAX_FORCE = 1e1f;
    private static final double JOINT_COMPLIANCE = 1e4f;
    private static final float ANCHOR_OFFSET = 0.1f;

    private static final AtomicInteger CLIENT_ROPE_ID_COUNTER = new AtomicInteger(100_000);

    public static PhysRope createPhysRope(ServerLevel level, RopePosData posData0, RopePosData posData1, ResourceKey<RopeStyle> style, Entity entity) {
        Vector3d worldStart = posData0.getWorldPos(level);
        Vector3d worldEnd   = posData1.getWorldPos(level);

        double totalDist = worldStart.distance(worldEnd);
        if (totalDist < 0.01)
        {
            VStuff.LOGGER.warn("PhysRopeFactory: attachment points are too close ({} m), aborting.", totalDist);
            return null;
        }

        VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        String dimId = ValkyrienSkies.getDimensionId(level);
        Long dimensionGroundBodyId = shipWorld.getDimensionToGroundBodyIdImmutable().get(dimId);
        if (dimensionGroundBodyId == null) {
            VStuff.LOGGER.error("PhysRopeFactory: ground body ID is null for dimension '{}' — aborting.", dimId);
            return null;
        }

        Long bodyId0 = resolveBodyId(shipWorld, posData0.getShipIdSafe(level), posData0, dimensionGroundBodyId);
        Long bodyId1 = resolveBodyId(shipWorld, posData1.getShipIdSafe(level), posData1, dimensionGroundBodyId);
        if (bodyId0 == null || bodyId1 == null) {
            VStuff.LOGGER.error("PhysRopeFactory: could not resolve body IDs for anchors, aborting.");
            return null;
        }

        Vector3d dir = new Vector3d(worldEnd).sub(worldStart).normalize();
        Vector3d spawnStart = new Vector3d(worldStart).add(new Vector3d(dir).mul(ANCHOR_OFFSET));
        Vector3d spawnEnd   = new Vector3d(worldEnd).sub(new Vector3d(dir).mul(ANCHOR_OFFSET));

        double spawnDist = spawnStart.distance(spawnEnd);
        if (spawnDist < 0.01)
        {
            VStuff.LOGGER.warn("PhysRopeFactory: attachment points too close after offset ({} m), aborting.", spawnDist);
            return null;
        }

        int segCount = Math.max(1, (int) Math.ceil(spawnDist / SEGMENT_LENGTH));
        float actualSegLen = SEGMENT_LENGTH;
        Vector3d step = new Vector3d(spawnEnd).sub(spawnStart).div(segCount);

        PhysRope rope = new PhysRope(posData0, posData1, style, actualSegLen);

        List<Long> allBodyIds = new ArrayList<>();
        allBodyIds.add(bodyId0);

        for (int i = 0; i < segCount - 1; i++)
        {
            Vector3d segPos = new Vector3d(spawnStart).add(new Vector3d(step).mul(i + 1));
            ServerVsBody body = createSegmentBody(shipWorld, dimId, segPos);
            if (body == null)
            {
                destroySegmentBodies(shipWorld, rope.segmentBodies);
                return null;
            }
            rope.segmentBodies.add(body);
            allBodyIds.add(body.getId());
        }
        allBodyIds.add(bodyId1);

        buildJointsAndFinalize(level, rope, allBodyIds, actualSegLen, entity, posData0, posData1, worldStart, worldEnd, dimensionGroundBodyId);

        return rope;
    }

    public static PhysRope createPhysRopeAtPositions(ServerLevel level, RopePosData posData0, RopePosData posData1, RopeStyle style, List<Vector3d> segmentPositions, float actualSegLen, Entity entity) {
        Vector3d worldStart = posData0.getWorldPos(level);
        Vector3d worldEnd   = posData1.getWorldPos(level);

        VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        String dimId = ValkyrienSkies.getDimensionId(level);
        Long dimensionGroundBodyId = shipWorld.getDimensionToGroundBodyIdImmutable().get(dimId);
        if (dimensionGroundBodyId == null)
        {
            VStuff.LOGGER.error("PhysRopeFactory: ground body ID is null for dimension '{}' — aborting.", dimId);
            return null;
        }

        Long bodyId0 = resolveBodyId(shipWorld, posData0.getShipIdSafe(level), posData0, dimensionGroundBodyId);
        Long bodyId1 = resolveBodyId(shipWorld, posData1.getShipIdSafe(level), posData1, dimensionGroundBodyId);
        if (bodyId0 == null || bodyId1 == null)
        {
            VStuff.LOGGER.error("PhysRopeFactory: could not resolve body IDs for anchors, aborting.");
            return null;
        }

        PhysRope rope = new PhysRope(posData0, posData1, style, actualSegLen);

        List<Long> allBodyIds = new ArrayList<>();
        allBodyIds.add(bodyId0);

        for (Vector3d segPos : segmentPositions)
        {
            ServerVsBody body = createSegmentBody(shipWorld, dimId, segPos);
            if (body == null)
            {
                destroySegmentBodies(shipWorld, rope.segmentBodies);
                return null;
            }
            rope.segmentBodies.add(body);
            allBodyIds.add(body.getId());
        }
        allBodyIds.add(bodyId1);

        buildJointsAndFinalize(level, rope, allBodyIds, actualSegLen, entity, posData0, posData1, worldStart, worldEnd, dimensionGroundBodyId);

        return rope;
    }

    private static void buildJointsAndFinalize(ServerLevel level, PhysRope rope, List<Long> allBodyIds, float actualSegLen, Entity entity, RopePosData posData0, RopePosData posData1, Vector3d worldStart, Vector3d worldEnd, Long dimensionGroundBodyId) {
        Long bodyId0 = allBodyIds.get(0);
        Long bodyId1 = allBodyIds.get(allBodyIds.size() - 1);

        Vector3d p0 = bodyId0.equals(dimensionGroundBodyId) ? worldStart : posData0.localPos();
        Vector3d p1 = bodyId1.equals(dimensionGroundBodyId) ? worldEnd   : posData1.localPos();

        VSJointPose centrePos   = new VSJointPose(new Vector3d(0, 0, 0), new Quaterniond());
        VSJointPose anchor0Pose = new VSJointPose(new Vector3d(p0), new Quaterniond());
        VSJointPose anchor1Pose = new VSJointPose(new Vector3d(p1), new Quaterniond());

        int totalJoints = allBodyIds.size() - 1;
        List<VSDistanceJoint> joints = new ArrayList<>(totalJoints);

        for (int i = 0; i < totalJoints; i++)
        {
            VSJointPose pose0 = (i == 0)               ? anchor0Pose : centrePos;
            VSJointPose pose1 = (i == totalJoints - 1) ? anchor1Pose : centrePos;
            joints.add(new VSDistanceJoint(
                    allBodyIds.get(i), pose0,
                    allBodyIds.get(i + 1), pose1,
                    new VSJointMaxForceTorque(INTER_SEGMENT_MAX_FORCE, INTER_SEGMENT_MAX_FORCE),
                    JOINT_COMPLIANCE,
                    0f,
                    actualSegLen,
                    1e3f, 1e7f, 1e5f
            ));
        }

        GameToPhysicsAdapter gtpa = GTPAUtils.getGTPA(level);
        AtomicInteger failed = new AtomicInteger(0);

        addJointSequential(level, gtpa, rope, joints, 0, totalJoints, failed, allBodyIds, actualSegLen, entity);
    }

    private static void addJointSequential(ServerLevel level, GameToPhysicsAdapter gtpa, PhysRope rope, List<VSDistanceJoint> joints, int index, int totalJoints, AtomicInteger failed, List<Long> allBodyIds, float actualSegLen, Entity placer) {
        if (failed.get() > 0) return;

        gtpa.addJoint(joints.get(index), 6, jointId -> VSGameUtilsKt.executeOrSchedule(level, () -> {
            if (failed.get() > 0) return;

            if (jointId == -1)
            {
                if (failed.getAndIncrement() == 0)
                {
                    VStuff.LOGGER.warn("PhysRopeFactory: joint {}/{} failed, tearing down rope.", index, totalJoints);
                    destroyPhysRope(level, rope);
                }
                return;
            }

            rope.jointIds.add(jointId);

            if (index + 1 < totalJoints)
            {
                addJointSequential(level, gtpa, rope, joints, index + 1, totalJoints, failed, allBodyIds, actualSegLen, placer);
            }
            else
            {
                finalizeRope(level, rope, allBodyIds, actualSegLen, placer);
            }
        }));
    }

    private static Long resolveBodyId(VsiServerShipWorld shipWorld, Long shipId, RopePosData posData, Long groundBodyId) {
        if (posData.isWorld() || shipId == null) return groundBodyId;

        var ship = shipWorld.getAllShips().getById(shipId);
        if (ship == null) {
            VStuff.LOGGER.warn("PhysRopeFactory: ship {} not found in world, falling back to ground body.", shipId);
            return groundBodyId;
        }

        Long bodyId = ship.getBodyId();
        return bodyId != null ? bodyId : groundBodyId;
    }

    private static ServerVsBody createSegmentBody(VsiServerShipWorld shipWorld, String dimId, Vector3d worldPos) {
        double r = SEGMENT_RADIUS;
        double m = SEGMENT_MASS;
        double I = 0.4 * m * r * r;
        Matrix3d inertiaTensor = new Matrix3d(I, 0, 0, 0, I, 0, 0, 0, I);

        BodyInertia inertia = new BodyInertiaDataImpl(new Vector3d(), m, inertiaTensor);

        BodyKinematics kinematics = BodyKinematicsFactory.INSTANCE.create(
                new Vector3d(0, 0, 0), new Vector3d(0, 0, 0),
                worldPos,
                new Quaterniond(),
                new Vector3d(1, 1, 1),
                new Vector3d(0, 0, 0)
        );

        StandaloneBodyCreateData createData = new StandaloneBodyCreateData(
                dimId, inertia, kinematics,
                new VSSphereCollisionShapeData(r),
                false, -1, 0.5, 0.3, 0.0
        );

        try {
            return shipWorld.createBody(createData);
        } catch (Exception e) {
            VStuff.LOGGER.error("PhysRopeFactory: failed to create segment body at {}: {}", worldPos, e.getMessage());
            return null;
        }
    }

    private static void finalizeRope(ServerLevel level, PhysRope rope, List<Long> allBodyIds, float segmentLength, Entity entity) {
        VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        int totalJoints = allBodyIds.size() - 1;

        for (int i = 0; i < totalJoints; i++) {
            Long bodyId0 = allBodyIds.get(i);
            Long bodyId1 = allBodyIds.get(i + 1);

            Vector3d lp0;
            Long renderShip0;
            boolean isDynamicEnd0;
            if (i == 0) {
                lp0 = rope.posData0.localPos();
                renderShip0 = rope.posData0.shipId();
                isDynamicEnd0 = false;
            } else {
                lp0 = getSegmentWorldPos(shipWorld, bodyId0);
                renderShip0 = null;
                isDynamicEnd0 = true;
            }

            Vector3d lp1;
            Long renderShip1;
            boolean isDynamicEnd1;
            if (i == totalJoints - 1) {
                lp1 = rope.posData1.localPos();
                renderShip1 = rope.posData1.shipId();
                isDynamicEnd1 = false;
            } else {
                lp1 = getSegmentWorldPos(shipWorld, bodyId1);
                renderShip1 = null;
                isDynamicEnd1 = true;
            }

            int clientId = CLIENT_ROPE_ID_COUNTER.getAndIncrement();
            rope.clientRopeIds.add(clientId);

            ClientRopeManager.addClientConstraint(
                    clientId, renderShip0, renderShip1,
                    lp0, lp1,
                    segmentLength, rope.styleKey
            );

            if (!isDynamicEnd0 && renderShip0 == null) {
                ClientRopeManager.updateClientRopePositions(clientId, Long.MAX_VALUE, new Vector3d(lp0), new Vector3d(), null, null);
            }
            if (!isDynamicEnd1 && renderShip1 == null) {
                ClientRopeManager.updateClientRopePositions(clientId, Long.MAX_VALUE, null, null, new Vector3d(lp1), new Vector3d());
            }
        }

        rope.attach(level);
        PhysRopeManager.get(level).addPhysRope(rope);

        if (entity instanceof ServerPlayer sp) {
            PhysRopeManager.syncAllPhysRopesToPlayer(sp);
        }

        VStuff.LOGGER.debug("PhysRopeFactory: created phys rope with {} segments, {} joints.",
                rope.segmentBodies.size(), rope.jointIds.size());
    }

    static Vector3d getSegmentWorldPos(VsiServerShipWorld shipWorld, Long bodyId)
    {
        try
        {
            ServerVsBody body = shipWorld.getAllBodies().getById(bodyId);
            if (body != null)
            {
                Vector3dc pos = body.getKinematics().getPosition();
                return new Vector3d(pos.x(), pos.y(), pos.z());
            }
        }
        catch (Exception e)
        {
            VStuff.LOGGER.warn("PhysRopeFactory: could not get world pos for body {}: {}", bodyId, e.getMessage());
        }
        return new Vector3d(0, 0, 0);
    }

    public static void destroyPhysRope(ServerLevel level, PhysRope rope)
    {
        GameToPhysicsAdapter gtpa = GTPAUtils.getGTPA(level);

        for (Integer jointId : rope.jointIds)
        {
            try { gtpa.removeJoint(jointId); }
            catch (Exception e)
            {
                VStuff.LOGGER.warn("PhysRopeFactory: error removing joint {}: {}", jointId, e.getMessage());
            }
        }
        rope.jointIds.clear();

        rope.segmentBodies.clear();

        try { rope.detach(level); }
        catch (Exception e)
        {
            VStuff.LOGGER.warn("PhysRopeFactory: error detaching rope {}: {}", rope.physRopeId, e.getMessage());
        }

        try { PhysRopeManager.get(level).removePhysRope(rope.physRopeId); }
        catch (Exception e)
        {
            VStuff.LOGGER.warn("PhysRopeFactory: error removing rope from manager {}: {}", rope.physRopeId, e.getMessage());
        }
    }

    private static void destroySegmentBodies(VsiServerShipWorld shipWorld, List<ServerVsBody> bodies)
    {
        bodies.clear();
    }

    static Vector3d getSegmentVelocity(VsiServerShipWorld shipWorld, Long bodyId)
    {
        try
        {
            ServerVsBody body = shipWorld.getAllBodies().getById(bodyId);
            if (body != null)
            {
                Vector3dc vel = body.getKinematics().getVelocity();
                return new Vector3d(vel.x(), vel.y(), vel.z());
            }
        }
        catch (Exception e)
        {
            VStuff.LOGGER.warn("PhysRopeFactory: could not get velocity for body {}: {}", bodyId, e.getMessage());
        }
        return new Vector3d(0, 0, 0);
    }
}