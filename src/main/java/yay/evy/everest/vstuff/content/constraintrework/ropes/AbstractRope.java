package yay.evy.everest.vstuff.content.constraintrework.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.constraint.RopeUtil;
import yay.evy.everest.vstuff.util.RopeStyles;

public abstract class AbstractRope {

    public Integer ID;
    public Long ship0;
    public Long ship1;

    public boolean ship0IsGround;
    public boolean ship1IsGround;

    public Vector3d localPos0;
    public Vector3d localPos1;
    public Vector3d worldPos0;
    public Vector3d worldPos1;

    public float minLength = 0f;
    public float maxLength;

    public float maxForce;
    public float maxTorque;

    public float tolerance = 0f;
    public float stiffness = 1f;
    public float damping = 0.1f;

    public BlockPos blockPos0;
    public BlockPos blockPos1;

    public RopeStyles.RopeStyle style;
    public RopeUtils.RopeType type;

    public VSDistanceJoint constraint;

    public AbstractRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1) {
        this.ID = ropeId;
        this.ship0 = ship0;
        this.ship1 = ship1;
        this.localPos0 = localPos0;
        this.localPos1 = localPos1;

        this.worldPos0 = RopeUtils.convertLocalToWorld(level, localPos0, ship0);
        this.worldPos1 = RopeUtils.convertLocalToWorld(level, localPos1, ship1);

        this.maxForce = calcMaxForce(level);
        this.maxTorque = calcMaxTorque(level);

        this.maxLength = (float) worldPos0.distance(worldPos1);
    }

    public AbstractRope(Integer ropeId, Long ship0, Long ship1, boolean ship0IsGround, boolean ship1IsGround,
                        Vector3d localPos0, Vector3d localPos1, Vector3d worldPos0, Vector3d worldPos1,
                        float minLength, float maxLength, float maxForce, float maxTorque, float tolerance,
                        float stiffness, float damping, BlockPos blockPos0, BlockPos blockPos1,
                        RopeStyles.RopeStyle style, RopeUtils.RopeType type) {
    }

    public abstract boolean createJoint(ServerLevel level);
    public abstract boolean editJoint(ServerLevel level);
    public abstract boolean removeJoint(ServerLevel level);

    public float calcMaxForce(ServerLevel level) {
        final float massA = RopeUtil.getFMassForShip(level, ship0);
        final float massB = RopeUtil.getFMassForShip(level, ship1);
        float massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        return (ship0IsGround || ship1IsGround)
                ? 50000000000000f * Math.min(massRatio, 20f) * 10f
                : 50000000000000f * Math.min(massRatio, 20f);
    }

    public float calcMaxTorque(ServerLevel level) {
        final float massA = RopeUtil.getFMassForShip(level, ship0);
        final float massB = RopeUtil.getFMassForShip(level, ship1);
        float massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        return (ship0IsGround || ship1IsGround)
                ? 50000000000000f * Math.min(massRatio, 20f) * 10f
                : 50000000000000f * Math.min(massRatio, 20f);
    }

    public VSDistanceJoint makeDistanceJoint() {
        return new VSDistanceJoint(
                ship0,
                new VSJointPose(localPos0, new Quaterniond()),
                ship1,
                new VSJointPose(localPos1, new Quaterniond()),
                new VSJointMaxForceTorque(maxForce, maxTorque),
                minLength,
                maxLength,
                tolerance,
                stiffness,
                damping
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("id", ID);
        tag.putLong("ship0", ship0 != null ? ship0 : 0L);
        tag.putLong("ship1", ship1 != null ? ship1 : 0L);
        tag.putBoolean("ship0IsGround", ship0IsGround);
        tag.putBoolean("ship1IsGround", ship1IsGround);

        putVector3d("localPos0", localPos0, tag);
        putVector3d("localPos1", localPos1, tag);
        putVector3d("worldPos0", worldPos0, tag);
        putVector3d("worldPos1", worldPos1, tag);

        tag.putFloat("minLength", minLength);
        tag.putFloat("maxLength", maxLength);

        tag.putFloat("maxForce", maxForce);
        tag.putFloat("maxTorque", maxTorque);

        tag.putFloat("tolerance", tolerance);
        tag.putFloat("stiffness", stiffness);
        tag.putFloat("damping", damping);

        putBlockPos("blockPos0", blockPos0, tag);
        putBlockPos("blockPos1", blockPos1, tag);

        tag.putString("type", type.name());

        tag.putString("style", style.getStyle());

        return tag;
    }


    protected static void putBlockPos(String pKey, BlockPos blockPos, CompoundTag toTag) {
        toTag.putInt(pKey + "_x", blockPos.getX());
        toTag.putInt(pKey + "_y", blockPos.getY());
        toTag.putInt(pKey + "_z", blockPos.getZ());
    }

    protected static void putVector3d(String pKey, Vector3d vec3d, CompoundTag toTag) {
        toTag.putDouble(pKey + "_x", vec3d.x);
        toTag.putDouble(pKey + "_y", vec3d.y);
        toTag.putDouble(pKey + "_z", vec3d.z);
    }

    protected static BlockPos getBlockPos(String pKey, CompoundTag fromTag) {
        return new BlockPos(
                fromTag.getInt(pKey + "_x"),
                fromTag.getInt(pKey + "_x"),
                fromTag.getInt(pKey + "_x")
        );
    }

    protected static Vector3d getVector3d(String pKey, CompoundTag fromTag) {
        return new Vector3d(
                fromTag.getDouble(pKey + "_x"),
                fromTag.getDouble(pKey + "_x"),
                fromTag.getDouble(pKey + "_x")
        );
    }

    public static <T extends AbstractRope> T fromTag(CompoundTag tag) {
        VStuff.LOGGER.warn("AbstractRope fromTag called!");
        return null;
    }
}
