package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.server.level.ServerLevel;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.*;
import org.valkyrienskies.core.internal.physics.*;
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.RopePosData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PhysRopeConstraint {

    public final RopePosData posData0;
    public final RopePosData posData1;
    private final List<Long> segmentShipIds = new ArrayList<>();
    private final List<Integer> jointIds = new ArrayList<>();
    private Integer ropeId;

    int segments;
    double segmentLength;

    public PhysRopeConstraint(RopePosData posData0, RopePosData posData1) {
        this.posData0 = posData0;
        this.posData1 = posData1;
    }

    public Integer getRopeId() { return ropeId; }
    public void setRopeId(Integer id) { this.ropeId = id; }

    public void create(ServerLevel level, int ropeId) {
        this.ropeId = ropeId;

        int segments = VStuffConfigs.server().physRopeSegments.get();
        double radius = VStuffConfigs.server().physRopeCapsuleRadius.get().doubleValue();
        double massPerSegment = VStuffConfigs.server().physRopeMassPerSegment.get().doubleValue();

        Vector3d worldPos0 = posData0.getWorldPos(level);
        Vector3d worldPos1 = posData1.getWorldPos(level);

        String dimId = VSGameUtilsKt.getDimensionId(level);
        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        List<Vector3d> segmentPositions = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            float t = (float)(i + 0.5) / segments;
            segmentPositions.add(new Vector3d(
                    worldPos0.x + (worldPos1.x - worldPos0.x) * t,
                    worldPos0.y + (worldPos1.y - worldPos0.y) * t,
                    worldPos0.z + (worldPos1.z - worldPos0.z) * t
            ));
        }

        double segmentLength = worldPos0.distance(worldPos1) / segments;
        double linkLength = Math.max(segmentLength * 0.5 - radius, 0.01);

        double rSq = radius * radius;
        double iAxial = 0.5 * massPerSegment * rSq;
        double iLateral = massPerSegment * (rSq / 4.0 + segmentLength * segmentLength / 12.0);
        Matrix3d tensor = new Matrix3d(
                iLateral, 0, 0,
                0, iAxial, 0,
                0, 0, iLateral
        );

        for (Vector3d pos : segmentPositions) {
            long segId = shipWorld.allocateShipId(dimId);
            segmentShipIds.add(segId);

            PhysicsEntityData data = new PhysicsEntityData(
                    segId,
                    ShipTransformImpl.Companion.create(
                            (org.joml.Vector3dc) pos,
                            (org.joml.Vector3dc) new Vector3d(),
                            (org.joml.Quaterniondc) new Quaterniond(),
                            (org.joml.Vector3dc) new Vector3d(1, 1, 1)
                    ),
                    new ShipInertiaDataImpl(new Vector3d(), massPerSegment, tensor),
                    (org.joml.Vector3dc) new Vector3d(),
                    (org.joml.Vector3dc) new Vector3d(),
                    new VSCapsuleCollisionShapeData(radius, linkLength),
                    -1,
                    0.5,
                    0.5,
                    0.0,
                    false
            );
            shipWorld.createPhysicsEntity(data, dimId);
            this.segments = segments;
            this.segmentLength = segmentLength;
        }

        final int segCount = segments;
        final double segLen = segmentLength;
        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 100,
                () -> createJoints(level, segCount, segLen)
        ));
    }

    private void createJoints(ServerLevel level, int segments, double segmentLength) {
        var gtpa = GTPAUtils.getGTPA(level);

        long firstSegId = segmentShipIds.get(0);
        VSDistanceJoint j0 = new VSDistanceJoint(
                posData0.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData0.localPos()), new Quaterniond()),
                firstSegId,
                new VSJointPose(new Vector3d(-(segmentLength * 0.5), 0, 0), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength * 0.95f,
                null, null, null
        );
        gtpa.addJoint(j0, 15, id -> { if (id != -1) jointIds.add(id); });

        for (int i = 0; i < segments - 1; i++) {
            long idA = segmentShipIds.get(i);
            long idB = segmentShipIds.get(i + 1);
            VSDistanceJoint j = new VSDistanceJoint(
                    idA, new VSJointPose(new Vector3d(segmentLength * 0.5, 0, 0), new Quaterniond()),
                    idB, new VSJointPose(new Vector3d(-(segmentLength * 0.5), 0, 0), new Quaterniond()),
                    null, VSJoint.DEFAULT_COMPLIANCE,
                    0f, (float) segmentLength,
                    null, null, null
            );
            gtpa.addJoint(j, 15, id -> { if (id != -1) jointIds.add(id); });
        }

        long lastSegId = segmentShipIds.get(segments - 1);
        VSDistanceJoint jN = new VSDistanceJoint(
                lastSegId,
                new VSJointPose(new Vector3d(segmentLength * 0.5, 0, 0), new Quaterniond()),
                posData1.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData1.localPos()), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength,
                null, null, null
        );
        gtpa.addJoint(jN, 15, id -> { if (id != -1) jointIds.add(id); });
    }

    public void destroy(ServerLevel level) {
        var gtpa = GTPAUtils.getGTPA(level);
        for (int jointId : jointIds) {
            gtpa.removeJoint(jointId);
        }
        jointIds.clear();

        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        String dimId = VSGameUtilsKt.getDimensionId(level);
        for (long segId : segmentShipIds) {
            shipWorld.deletePhysicsEntity(segId);
        }
        segmentShipIds.clear();

        posData0.remove(level, ropeId);
        posData1.remove(level, ropeId);
    }

    public List<Vector3d> getSegmentWorldPositions(Map<Long, PhysicsEntityServer> loadedEntities) {
        List<Vector3d> positions = new ArrayList<>();
        for (long segId : segmentShipIds) {
            PhysicsEntityServer entity = loadedEntities.get(segId);
            if (entity == null) return null;
            org.joml.Vector3dc pos = entity.getShipTransform().getPositionInWorld();
            positions.add(new Vector3d(pos.x(), pos.y(), pos.z()));
        }
        return positions;
    }

    public void restoreSegmentIds(List<Long> ids) {
        segmentShipIds.addAll(ids);
    }

    public long[] getSegmentShipIds() {
        long[] arr = new long[segmentShipIds.size()];
        for (int i = 0; i < segmentShipIds.size(); i++) arr[i] = segmentShipIds.get(i);
        return arr;
    }

    public void restoreJoints(ServerLevel level, int segments, double segmentLength) {
        var gtpa = GTPAUtils.getGTPA(level);

        long firstSegId = segmentShipIds.get(0);
        VSDistanceJoint j0 = new VSDistanceJoint(
                posData0.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData0.localPos()), new Quaterniond()),
                firstSegId,
                new VSJointPose(new Vector3d(-(segmentLength * 0.5), 0, 0), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength * 0.95f,
                null, null, null
        );
        gtpa.addJoint(j0, 200, id -> { if (id != -1) jointIds.add(id); });

        for (int i = 0; i < segments - 1; i++) {
            long idA = segmentShipIds.get(i);
            long idB = segmentShipIds.get(i + 1);
            VSDistanceJoint j = new VSDistanceJoint(
                    idA, new VSJointPose(new Vector3d(segmentLength * 0.5, 0, 0), new Quaterniond()),
                    idB, new VSJointPose(new Vector3d(-(segmentLength * 0.5), 0, 0), new Quaterniond()),
                    null, VSJoint.DEFAULT_COMPLIANCE,
                    0f, (float) segmentLength,
                    null, null, null
            );
            gtpa.addJoint(j, 200, id -> { if (id != -1) jointIds.add(id); });
        }

        long lastSegId = segmentShipIds.get(segments - 1);
        VSDistanceJoint jN = new VSDistanceJoint(
                lastSegId,
                new VSJointPose(new Vector3d(segmentLength * 0.5, 0, 0), new Quaterniond()),
                posData1.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData1.localPos()), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength,
                null, null, null
        );
        gtpa.addJoint(jN, 200, id -> { if (id != -1) jointIds.add(id); });
    }

    public int getSegments() { return segments; }
    public double getSegmentLength() { return segmentLength; }

    public void recreatePhysEntities(ServerLevel level) {
        clearJointIds(level);
        double radius = VStuffConfigs.server().physRopeCapsuleRadius.get().doubleValue();
        double massPerSegment = VStuffConfigs.server().physRopeMassPerSegment.get().doubleValue();

        Vector3d worldPos0 = posData0.getWorldPos(level);
        Vector3d worldPos1 = posData1.getWorldPos(level);

        String dimId = VSGameUtilsKt.getDimensionId(level);
        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        double linkLength = Math.max(segmentLength * 0.5 - radius, 0.01);
        double rSq = radius * radius;
        double iAxial = 0.5 * massPerSegment * rSq;
        double iLateral = massPerSegment * (rSq / 4.0 + segmentLength * segmentLength / 12.0);
        Matrix3d tensor = new Matrix3d(iLateral, 0, 0, 0, iAxial, 0, 0, 0, iLateral);

        segmentShipIds.clear();

        for (int i = 0; i < segments; i++) {
            float t = (float)(i + 0.5) / segments;
            Vector3d pos = new Vector3d(
                    worldPos0.x + (worldPos1.x - worldPos0.x) * t,
                    worldPos0.y + (worldPos1.y - worldPos0.y) * t,
                    worldPos0.z + (worldPos1.z - worldPos0.z) * t
            );

            long segId = shipWorld.allocateShipId(dimId);
            segmentShipIds.add(segId);

            PhysicsEntityData data = new PhysicsEntityData(
                    segId,
                    ShipTransformImpl.Companion.create(
                            (org.joml.Vector3dc) pos,
                            (org.joml.Vector3dc) new Vector3d(),
                            (org.joml.Quaterniondc) new Quaterniond(),
                            (org.joml.Vector3dc) new Vector3d(1, 1, 1)
                    ),
                    new ShipInertiaDataImpl(new Vector3d(), massPerSegment, tensor),
                    (org.joml.Vector3dc) new Vector3d(),
                    (org.joml.Vector3dc) new Vector3d(),
                    new VSCapsuleCollisionShapeData(radius, linkLength),
                    -1, 0.5, 0.5, 0.0, false
            );
            shipWorld.createPhysicsEntity(data, dimId);
        }
    }

    public void clearJointIds(ServerLevel level) {
        var gtpa = GTPAUtils.getGTPA(level);
        for (int jointId : jointIds) {
            gtpa.removeJoint(jointId);
        }
        jointIds.clear();
    }
    public boolean hasSegments() {
        return !segmentShipIds.isEmpty();
    }

    public void restoreSegmentData(int segments, double segmentLength) {
        this.segments = segments;
        this.segmentLength = segmentLength;
    }
}