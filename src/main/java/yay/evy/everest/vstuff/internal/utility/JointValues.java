package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.nbt.CompoundTag;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;

import javax.annotation.Nullable;

public record JointValues(VSJointMaxForceTorque maxForceTorque, Float minLength, Float maxLength, Double compliance, @Nullable Float tolerance, @Nullable Float stiffness, @Nullable Float damping) {
    public static final Float DEFAULT_MINLENGTH = 0f;
    public static final Float DEFAULT_TOLERANCE = 0.1f;
    public static final Float DEFAULT_STIFFNESS = 1e8f;
    public static final Float DEFAULT_DAMPING = null;
    public static JointValues withDefault(VSJointMaxForceTorque maxForceTorque, Float maxLength, Double compliance) {
        return new JointValues(maxForceTorque, DEFAULT_MINLENGTH, maxLength, compliance, DEFAULT_TOLERANCE, DEFAULT_STIFFNESS, DEFAULT_DAMPING);
    }

    public JointValues withChanged(@Nullable VSJointMaxForceTorque newMaxForceTorque, @Nullable Float newMinLength,
                                   @Nullable Float newMaxLength, @Nullable Double newCompliance, @Nullable Float newTolerance,
                                   @Nullable Float newStiffness, @Nullable Float newDamping) {
        return new JointValues(
                newMaxForceTorque == null ? this.maxForceTorque : newMaxForceTorque,
                newMinLength == null ? this.minLength : newMinLength,
                newMaxLength == null ? this.maxLength : newMaxLength,
                newCompliance == null ? this.compliance : newCompliance,
                newTolerance == null ? this.tolerance : newTolerance,
                newStiffness == null ? this.stiffness : newStiffness,
                newDamping == null ? this.damping : newDamping
        );
    }

    public VSDistanceJoint makeJoint(Long ship0, Vector3d localPos0, Long ship1, Vector3d localPos1) {
        return new VSDistanceJoint(
                ship0,
                new VSJointPose(new Vector3d(localPos0), new Quaterniond()),
                ship1,
                new VSJointPose(new Vector3d(localPos1), new Quaterniond()),
                this.maxForceTorque,
                this.compliance,
                this.minLength,
                this.maxLength,
                this.tolerance,
                this.stiffness,
                this.damping
        );
    }
}