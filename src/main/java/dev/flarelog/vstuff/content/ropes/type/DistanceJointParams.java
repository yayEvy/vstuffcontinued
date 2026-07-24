package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;

import java.util.Calendar;

@SuppressWarnings("unused")
public class DistanceJointParams {

    private final VSJointMaxForceTorque maxForceTorque;
    private final Double compliance;

    private final Float minDistance;
    private final Float tolerance;
    private final Float stiffness;
    private final Float damping;

    public DistanceJointParams(VSJointMaxForceTorque maxForceTorque, Double compliance, Float minDistance, Float tolerance, Float stiffness, Float damping) {
        this.maxForceTorque = maxForceTorque;
        this.compliance = compliance;
        this.minDistance = minDistance;
        this.tolerance = tolerance;
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public static class Builder {
        public Builder(){}

        private VSJointMaxForceTorque maxForceTorque = null;
        private Double compliance = VSJoint.DEFAULT_COMPLIANCE;

        private Float minDistance = null;
        private Float tolerance = null;
        private Float stiffness = null;
        private Float damping = null;
        
        public Builder minDistance(Float minDistance) {
            this.minDistance = minDistance;
            return this;
        }

        public Builder tolerance(Float tolerance) {
            this.tolerance = tolerance;
            return this;
        }
        
        public Builder stiffness(Float stiffness) {
            this.stiffness = stiffness;
            return this;
        }
        
        public Builder damping(Float damping) {
            this.damping = damping;
            return this;
        }
        
        public Builder compliance(Double compliance) {
            this.compliance = compliance;
            return this;
        }
        
        public Builder maxForceTorque(VSJointMaxForceTorque maxForceTorque) {
            this.maxForceTorque = maxForceTorque;
            return this;
        }

        public DistanceJointParams build() {
            return new DistanceJointParams(maxForceTorque, compliance, minDistance, tolerance, stiffness, damping);
        }
    }
}
