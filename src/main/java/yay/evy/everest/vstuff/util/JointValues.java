package yay.evy.everest.vstuff.util;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;

public record JointValues(VSJointMaxForceTorque maxForceTorque, Float minLength, Float maxLength, Double compliance, Float tolerance, Float stiffness, Float damping) {
    public static final Float DEFAULT_MINLENGTH = 0f;
    public static final Float DEFAULT_TOLERANCE = 0.1f;
    public static final Float DEFAULT_STIFFNESS = 1e8f;
    public static final Float DEFAULT_DAMPING = null;
    public static JointValues withDefault(VSJointMaxForceTorque maxForceTorque, Float maxLength, Double compliance) {
        return new JointValues(maxForceTorque, DEFAULT_MINLENGTH, maxLength, compliance, DEFAULT_TOLERANCE, DEFAULT_STIFFNESS, DEFAULT_DAMPING);
    }

    public VSDistanceJoint makeJoint(Long ship0, Vector3f localPos0, Long ship1, Vector3f localPos1) {
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
