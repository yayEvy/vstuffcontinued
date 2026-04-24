package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.impl.util.serialization.VSJacksonUtil;
import org.valkyrienskies.core.internal.joints.*;
import org.valkyrienskies.core.internal.physics.*;
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;
import yay.evy.everest.vstuff.internal.utility.GTPAUtils;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhysRopeConstraint {

    public final RopePosData posData0;
    public final RopePosData posData1;
    private final List<Long> segmentShipIds = new ArrayList<>();
    private final List<Integer> jointIds = new ArrayList<>();
    private Integer ropeId;
    private List<Vector3d> lastKnownPositions = new ArrayList<>();

    int segments;
    double segmentLength;

    private final List<PhysicsEntityData> savedSegmentData = new ArrayList<>();

    public PhysRopeConstraint(RopePosData posData0, RopePosData posData1) {
        this.posData0 = posData0;
        this.posData1 = posData1;
    }

    public Integer getRopeId() { return ropeId; }
    public void setRopeId(Integer id) { this.ropeId = id; }


    public void create(ServerLevel level, int ropeId) {
        this.ropeId = ropeId;

        double targetSegmentLength = VStuffConfigs.server().physRopeSegmentLength.get().doubleValue();
        double radius = VStuffConfigs.server().physRopeCapsuleRadius.get().doubleValue();
        double massPerSegment = VStuffConfigs.server().physRopeMassPerSegment.get().doubleValue();

        Vector3d worldPos0 = posData0.getWorldPos(level);
        Vector3d worldPos1 = posData1.getWorldPos(level);

        double totalLength = worldPos0.distance(worldPos1);
        segments = Math.max(2, (int) Math.round(totalLength / targetSegmentLength));
        segmentLength = totalLength / segments;

        String dimId = VSGameUtilsKt.getDimensionId(level);
        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        Vector3d dir = new Vector3d(worldPos1).sub(worldPos0).normalize();
        Quaterniond rot = getRotationToDir(dir);

        double linkLength = Math.max(segmentLength * 0.5 - radius, 0.01);
        Matrix3d tensor = makeInertiaTensor(linkLength, radius, massPerSegment);

        for (int i = 0; i < segments; i++) {
            float t = (float) (i + 0.5) / segments;
            Vector3d pos = new Vector3d(
                    worldPos0.x + (worldPos1.x - worldPos0.x) * t,
                    worldPos0.y + (worldPos1.y - worldPos0.y) * t,
                    worldPos0.z + (worldPos1.z - worldPos0.z) * t
            );

            long segId = shipWorld.allocateShipId(dimId);
            segmentShipIds.add(segId);

            PhysicsEntityData data = new PhysicsEntityData(
                    segId,
                    ShipTransformImpl.Companion.create(pos, new Vector3d(), rot, new Vector3d(1, 1, 1)),
                    new ShipInertiaDataImpl(new Vector3d(), massPerSegment, tensor),
                    new Vector3d(), new Vector3d(),
                    new VSCapsuleCollisionShapeData(radius, linkLength),
                    -1, 0.5, 0.5, 0.0, false
            );
            shipWorld.createPhysicsEntity(data, dimId);
        }

        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 2,
                () -> createJoints(level, segments, segmentLength)
        ));
    }


    private void createJoints(ServerLevel level, int segments, double segmentLength) {
        var gtpa = GTPAUtils.getGTPA(level);
        double halfLen = segmentLength * 0.5;

        long firstSegId = segmentShipIds.get(0);
        gtpa.addJoint(new VSDistanceJoint(
                posData0.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData0.localPos()), new Quaterniond()),
                firstSegId,
                new VSJointPose(new Vector3d(-halfLen, 0, 0), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength * 0.95f,
                null, null, null
        ), 15, id -> { if (id != -1) jointIds.add(id); });

        for (int i = 0; i < segments - 1; i++) {
            long idA = segmentShipIds.get(i);
            long idB = segmentShipIds.get(i + 1);
            gtpa.addJoint(new VSDistanceJoint(
                    idA, new VSJointPose(new Vector3d(halfLen, 0, 0), new Quaterniond()),
                    idB, new VSJointPose(new Vector3d(-halfLen, 0, 0), new Quaterniond()),
                    null, VSJoint.DEFAULT_COMPLIANCE,
                    0f, (float) segmentLength,
                    null, null, null
            ), 15, id -> { if (id != -1) jointIds.add(id); });
        }

        long lastSegId = segmentShipIds.get(segments - 1);
        gtpa.addJoint(new VSDistanceJoint(
                lastSegId,
                new VSJointPose(new Vector3d(halfLen, 0, 0), new Quaterniond()),
                posData1.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData1.localPos()), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength,
                null, null, null
        ), 15, id -> { if (id != -1) jointIds.add(id); });
    }

    public void restoreJoints(ServerLevel level) {
        var gtpa = GTPAUtils.getGTPA(level);
        double halfLen = segmentLength * 0.5;

        long firstSegId = segmentShipIds.get(0);
        gtpa.addJoint(new VSDistanceJoint(
                posData0.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData0.localPos()), new Quaterniond()),
                firstSegId,
                new VSJointPose(new Vector3d(-halfLen, 0, 0), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength * 0.95f,
                null, null, null
        ), 20, id -> { if (id != -1) jointIds.add(id); });

        for (int i = 0; i < segments - 1; i++) {
            long idA = segmentShipIds.get(i);
            long idB = segmentShipIds.get(i + 1);
            gtpa.addJoint(new VSDistanceJoint(
                    idA, new VSJointPose(new Vector3d(halfLen, 0, 0), new Quaterniond()),
                    idB, new VSJointPose(new Vector3d(-halfLen, 0, 0), new Quaterniond()),
                    null, VSJoint.DEFAULT_COMPLIANCE,
                    0f, (float) segmentLength,
                    null, null, null
            ), 20, id -> { if (id != -1) jointIds.add(id); });
        }

        long lastSegId = segmentShipIds.get(segments - 1);
        gtpa.addJoint(new VSDistanceJoint(
                lastSegId,
                new VSJointPose(new Vector3d(halfLen, 0, 0), new Quaterniond()),
                posData1.getShipIdSafe(level),
                new VSJointPose(new Vector3d(posData1.localPos()), new Quaterniond()),
                null, VSJoint.DEFAULT_COMPLIANCE,
                0f, (float) segmentLength,
                null, null, null
        ), 20, id -> { if (id != -1) jointIds.add(id); });
    }


    public void captureSegmentData(Map<Long, PhysicsEntityServer> loadedEntities) {
        savedSegmentData.clear();

        double radius = VStuffConfigs.server().physRopeCapsuleRadius.get().doubleValue();
        double massPerSegment = VStuffConfigs.server().physRopeMassPerSegment.get().doubleValue();
        double linkLength = Math.max(segmentLength * 0.5 - radius, 0.01);
        Matrix3d tensor = makeInertiaTensor(linkLength, radius, massPerSegment);

        for (long segId : segmentShipIds) {
            PhysicsEntityServer entity = loadedEntities.get(segId);

            Vector3d pos;
            Quaterniond rot;
            if (entity != null) {
                pos = new Vector3d(entity.getShipTransform().getPositionInWorld());
                rot = new Quaterniond(entity.getShipTransform().getShipToWorldRotation());
            } else {
                int idx = segmentShipIds.indexOf(segId);
                if (idx >= 0 && idx < lastKnownPositions.size()) {
                    pos = new Vector3d(lastKnownPositions.get(idx));
                } else {
                    pos = new Vector3d();
                }
                rot = new Quaterniond();
            }

            savedSegmentData.add(new PhysicsEntityData(
                    segId,
                    ShipTransformImpl.Companion.create(pos, new Vector3d(), rot, new Vector3d(1, 1, 1)),
                    new ShipInertiaDataImpl(new Vector3d(), massPerSegment, tensor),
                    new Vector3d(), new Vector3d(),
                    new VSCapsuleCollisionShapeData(radius, linkLength),
                    -1, 0.5, 0.5, 0.0, false
            ));
        }
    }

    /** Serialize captured segment data to NBT. */
    public ListTag saveSegmentDataToNBT() {
        ListTag list = new ListTag();
        ObjectMapper mapper = VSJacksonUtil.INSTANCE.getDefaultMapper();
        for (PhysicsEntityData data : savedSegmentData) {
            try {
                byte[] bytes = mapper.writeValueAsBytes(data);
                CompoundTag tag = new CompoundTag();
                tag.putByteArray("data", bytes);
                list.add(tag);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize PhysicsEntityData for rope segment", e);
            }
        }
        return list;
    }

    /** Deserialize segment data from NBT and recreate physics entities at their saved positions. */
    public void restorePhysEntities(ServerLevel level, ListTag savedDataTag) {
        String dimId = VSGameUtilsKt.getDimensionId(level);
        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        ObjectMapper mapper = VSJacksonUtil.INSTANCE.getDefaultMapper();

        double radius = VStuffConfigs.server().physRopeCapsuleRadius.get().doubleValue();
        double massPerSegment = VStuffConfigs.server().physRopeMassPerSegment.get().doubleValue();
        double linkLength = Math.max(segmentLength * 0.5 - radius, 0.01);
        Matrix3d tensor = makeInertiaTensor(linkLength, radius, massPerSegment);

        for (int i = 0; i < segments; i++) {
            long segId = segmentShipIds.get(i);
            PhysicsEntityData data;

            if (savedDataTag != null && i < savedDataTag.size()) {
                try {
                    byte[] bytes = savedDataTag.getCompound(i).getByteArray("data");
                    PhysicsEntityData saved = mapper.readValue(bytes, PhysicsEntityData.class);
                    data = new PhysicsEntityData(
                            segId,
                            saved.getTransform(),
                            new ShipInertiaDataImpl(new Vector3d(), massPerSegment, tensor),
                            new Vector3d(), new Vector3d(),
                            new VSCapsuleCollisionShapeData(radius, linkLength),
                            -1, 0.5, 0.5, 0.0, false
                    );
                } catch (Exception e) {
                    data = fallbackData(level, i, segId, radius, linkLength, massPerSegment, tensor);
                }
            } else {
                data = fallbackData(level, i, segId, radius, linkLength, massPerSegment, tensor);
            }

            shipWorld.createPhysicsEntity(data, dimId);
        }
    }

    private PhysicsEntityData fallbackData(ServerLevel level, int i, long segId,
                                           double radius, double linkLength,
                                           double massPerSegment, Matrix3d tensor) {
        Vector3d w0 = posData0.getWorldPos(level);
        Vector3d w1 = posData1.getWorldPos(level);
        float t = (float) (i + 0.5) / segments;
        Vector3d pos = new Vector3d(
                w0.x + (w1.x - w0.x) * t,
                w0.y + (w1.y - w0.y) * t,
                w0.z + (w1.z - w0.z) * t
        );
        Quaterniond rot = getRotationToDir(new Vector3d(w1).sub(w0).normalize());
        return new PhysicsEntityData(
                segId,
                ShipTransformImpl.Companion.create(pos, new Vector3d(), rot, new Vector3d(1, 1, 1)),
                new ShipInertiaDataImpl(new Vector3d(), massPerSegment, tensor),
                new Vector3d(), new Vector3d(),
                new VSCapsuleCollisionShapeData(radius, linkLength),
                -1, 0.5, 0.5, 0.0, false
        );
    }


    public void destroy(ServerLevel level) {
        clearJointIds(level);

        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        for (long segId : segmentShipIds) {
            shipWorld.deletePhysicsEntity(segId);
        }
        segmentShipIds.clear();

        posData0.remove(level, ropeId);
        posData1.remove(level, ropeId);
    }

    public void clearJointIds(ServerLevel level) {
        var gtpa = GTPAUtils.getGTPA(level);
        for (int jointId : jointIds) {
            gtpa.removeJoint(jointId);
        }
        jointIds.clear();
    }


    public List<Vector3d> getSegmentWorldPositions(Map<Long, PhysicsEntityServer> loadedEntities) {
        List<Vector3d> positions = new ArrayList<>();
        for (long segId : segmentShipIds) {
            PhysicsEntityServer entity = loadedEntities.get(segId);
            if (entity == null) return null;
            var pos = entity.getShipTransform().getPositionInWorld();
            positions.add(new Vector3d(pos.x(), pos.y(), pos.z()));
        }
        return positions;
    }

    public void updateLastKnownPositions(List<Vector3d> positions) {
        this.lastKnownPositions = new ArrayList<>(positions);
    }

    public List<Vector3d> getLastKnownPositions() {
        return lastKnownPositions;
    }

    public void restoreSegmentIds(List<Long> ids) {
        segmentShipIds.addAll(ids);
    }

    public long[] getSegmentShipIds() {
        long[] arr = new long[segmentShipIds.size()];
        for (int i = 0; i < segmentShipIds.size(); i++) arr[i] = segmentShipIds.get(i);
        return arr;
    }

    public int getSegments() { return segments; }
    public double getSegmentLength() { return segmentLength; }
    public boolean hasSegments() { return !segmentShipIds.isEmpty(); }

    public void restoreSegmentData(int segments, double segmentLength) {
        this.segments = segments;
        this.segmentLength = segmentLength;
    }

    private static Matrix3d makeInertiaTensor(double l, double r, double mass) {
        double rSq = r * r;
        double cM_temp = l * rSq * Math.PI;
        double hsM_temp = 2.0 * rSq * r * Math.PI * (1.0 / 3.0);
        double density = mass / (cM_temp + hsM_temp);
        double cM = cM_temp * density;
        double hsM = hsM_temp * density;

        Matrix3d tensor = new Matrix3d();
        tensor.m11 = rSq * cM * 0.5;
        tensor.m22 = tensor.m11 * 0.5 + cM * l * l * (1.0 / 12.0);
        tensor.m00 = tensor.m22;

        double temp0 = hsM * 2.0 * rSq / 5.0;
        tensor.m11 += temp0 * 2.0;
        double temp1 = l * 0.5;
        double temp2 = temp0 + hsM * (temp1 * temp1 + 3.0 * (1.0 / 8.0) * l * r);
        tensor.m00 += temp2 * 2.0;
        tensor.m22 += temp2 * 2.0;

        return tensor;
    }

    private static Quaterniond getRotationToDir(Vector3d dir) {
        Vector3d from = new Vector3d(1, 0, 0);
        if (dir.lengthSquared() < 1e-10) return new Quaterniond();
        dir = new Vector3d(dir).normalize();
        double dot = from.dot(dir);
        if (dot > 0.9999) return new Quaterniond();
        if (dot < -0.9999) return new Quaterniond().rotateY(Math.PI);
        Vector3d axis = new Vector3d(from).cross(dir).normalize();
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));
        return new Quaterniond().fromAxisAngleRad(axis, angle);
    }
}