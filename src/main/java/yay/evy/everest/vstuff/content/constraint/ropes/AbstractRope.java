package yay.evy.everest.vstuff.content.constraint.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.util.RopeStyles;

import static yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils.*;

public abstract class AbstractRope {

    public Integer ID;
    public Long ship0;
    public Long ship1;

    public boolean ship0IsGround;
    public boolean ship1IsGround;

    public Vector3d localPos0;
    public Vector3d localPos1;

    public float minLength = 0f;
    public float maxLength;

    public float maxForce;
    public float maxTorque;

    public float tolerance = 0f;
    public float stiffness = 1f;
    public float damping = 0.1f;

    public BlockPos blockPos0;
    public BlockPos blockPos1;

    public RopeStyles.RopeStyle style = RopeStyles.fromString("normal");
    public RopeUtils.RopeType type;

    public VSDistanceJoint constraint;

    AbstractRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1) {
        this.ID = ropeId;
        this.ship0 = ship0;
        this.ship1 = ship1;
        this.localPos0 = localPos0;
        this.localPos1 = localPos1;

        this.maxForce = calcMaxForce(level);
        this.maxTorque = calcMaxTorque(level);

        this.maxLength = (float) RopeUtils.convertLocalToWorld(level, localPos0, ship0).distance(RopeUtils.convertLocalToWorld(level, localPos0, ship1));
    }

    public AbstractRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1, RopeStyles.RopeStyle style) {
        this(level, ropeId, ship0, ship1, RopeUtils.getLocalPosition(blockPos0), RopeUtils.getLocalPosition(blockPos1));
        this.blockPos0 = blockPos0;
        this.blockPos1 = blockPos1;
        this.style = style;
    }

    public AbstractRope(Integer ropeId, Long ship0, Long ship1, boolean ship0IsGround, boolean ship1IsGround,
                        Vector3d localPos0, Vector3d localPos1, float minLength, float maxLength, float maxForce,
                        float maxTorque, float tolerance, float stiffness, float damping, BlockPos blockPos0,
                        BlockPos blockPos1, RopeStyles.RopeStyle style, RopeUtils.RopeType type) {
        this.ID = ropeId;
        this.ship0 = ship0;
        this.ship1 = ship1;
        this.ship0IsGround = ship0IsGround;
        this.ship1IsGround = ship1IsGround;
        this.localPos0 = localPos0;
        this.localPos1 = localPos1;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.maxForce = maxForce;
        this.maxTorque = maxTorque;
        this.tolerance = tolerance;
        this.stiffness = stiffness;
        this.damping = damping;
        this.blockPos0 = blockPos0;
        this.blockPos1 = blockPos1;
        this.style = style;
        this.type = type;
    }

    public abstract boolean createJoint(ServerLevel level);
    public abstract boolean removeJoint(ServerLevel level);

    public float calcMaxForce(ServerLevel level) {
        final float massA = RopeUtils.getMassForShip(level, ship0);
        final float massB = RopeUtils.getMassForShip(level, ship1);
        float massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        return (ship0IsGround || ship1IsGround)
                ? 50000000000000f * Math.min(massRatio, 20f) * 10f
                : 50000000000000f * Math.min(massRatio, 20f);
    }

    public float calcMaxTorque(ServerLevel level) {
        final float massA = RopeUtils.getMassForShip(level, ship0);
        final float massB = RopeUtils.getMassForShip(level, ship1);
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

        tag.putFloat("minLength", minLength);
        tag.putFloat("maxLength", maxLength);

        tag.putFloat("maxForce", maxForce);
        tag.putFloat("maxTorque", maxTorque);

        tag.putFloat("tolerance", tolerance);
        tag.putFloat("stiffness", stiffness);
        tag.putFloat("damping", damping);

        putBlockPos("blockPos0", blockPos0, tag);
        putBlockPos("blockPos1", blockPos1, tag);

        tag.putString("style", style.getStyle());

        tag.putString("type", type.name());

        return tag;
    }

    public void addToBuf(FriendlyByteBuf buf) {
        buf.writeInt(ID);
        buf.writeLong(ship0);
        buf.writeLong(ship1);
        buf.writeBoolean(ship0IsGround);
        buf.writeBoolean(ship1IsGround);

        buf.writeVector3f(v3dToV3f(localPos0));
        buf.writeVector3f(v3dToV3f(localPos1));

        buf.writeFloat(minLength);
        buf.writeFloat(maxLength);

        buf.writeFloat(maxForce);
        buf.writeFloat(maxTorque);

        buf.writeFloat(tolerance);
        buf.writeFloat(stiffness);
        buf.writeFloat(damping);
        buf.writeBlockPos(blockPos0);
        buf.writeBlockPos(blockPos1);

        buf.writeUtf(style.getStyle());

        buf.writeEnum(type);
    }



    public static <T extends AbstractRope> T fromTag(CompoundTag tag) {
        VStuff.LOGGER.warn("AbstractRope fromTag called!");
        return null;
    }

    public static <T extends AbstractRope> T fromBuf(FriendlyByteBuf buf) {
        VStuff.LOGGER.warn("AbstractRope fromBuf called!");
        return null;
    }
}
