package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;

import java.util.Calendar;

@Builder
@AllArgsConstructor
public class DistanceJointParams {
    private final VSJointMaxForceTorque maxForceTorque;
    @Builder.Default private final Double compliance = VSJoint.DEFAULT_COMPLIANCE;

    private final Float minDistance;
    private final Float tolerance;
    private final Float stiffness;
    private final Float damping;
}
