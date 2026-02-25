package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;

public class TagUtils {

    public static CompoundTag writeVector3d(Vector3d vector3d) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putDouble("X", vector3d.x);
        compoundTag.putDouble("Y", vector3d.y);
        compoundTag.putDouble("Z", vector3d.z);
        return compoundTag;
    }

    public static Vector3d readVector3d(CompoundTag tag) {
        return new Vector3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
    }

    public static CompoundTag writeJointValues(JointValues jointValues) {
        CompoundTag tag = new CompoundTag();

        tag.putFloat("maxForce", jointValues.maxForceTorque().getMaxForce());
        tag.putFloat("maxTorque", jointValues.maxForceTorque().getMaxTorque());
        tag.putFloat("minLength", jointValues.minLength());
        tag.putFloat("maxLength", jointValues.maxLength());
        tag.putDouble("compliance", jointValues.compliance());
        tag.putFloat("tolerance", jointValues.tolerance() == null ? -1 : jointValues.tolerance());
        tag.putFloat("stiffness", jointValues.stiffness() == null ? -1 : jointValues.stiffness());
        tag.putFloat("damping", jointValues.damping() == null ? -1 : jointValues.damping());

        return tag;
    }

    public static JointValues readJointValues(CompoundTag tag) {
        VSJointMaxForceTorque maxForceTorque = new VSJointMaxForceTorque(tag.getFloat("maxForce"), tag.getFloat("maxTorque"));
        Float tolerance = tag.getFloat("tolerance");
        Float stiffness = tag.getFloat("stiffness");
        Float damping = tag.getFloat("damping");

        if (tolerance == -1) tolerance = null;
        if (stiffness == -1) stiffness = null;
        if (damping == -1) damping = null;

        return new JointValues(maxForceTorque, tag.getFloat("minLength"), tag.getFloat("maxLength"), tag.getDouble("compliance"), tolerance, stiffness, damping);
    }

    public static CompoundTag writePosData(RopePosData posData) {
        CompoundTag tag = new CompoundTag();

        tag.putLong("shipId", posData.shipId() == null ? -1 : posData.shipId()); // use -1 to denote ground body
        tag.put("blockPos", NbtUtils.writeBlockPos(posData.blockPos()));
        tag.put("localPos", writeVector3d(posData.localPos()));
        tag.putString("posType", posData.posType().name());

        return tag;
    }

    public static RopePosData readPosData(CompoundTag tag) {
        Long shipId = tag.getLong("shipId");
        shipId = shipId == -1 ? null : shipId;
        BlockPos blockPos = NbtUtils.readBlockPos(tag.getCompound("blockPos"));
        Vector3d localPos = readVector3d(tag.getCompound("localPos"));
        RopeUtils.PosType posType = RopeUtils.PosType.valueOf(tag.getString("posType"));

        return new RopePosData(shipId, blockPos, localPos, posType);
    }
}
