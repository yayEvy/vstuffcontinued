package yay.evy.everest.vstuff.content.pulley.util;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.*;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PulleyUtils {

    private static final Map<Integer, PulleyJointTickable> trackedJoints = new ConcurrentHashMap<>();



    public static Integer createPulleyJoint(
            GameToPhysicsAdapter gtpa,
            long shipA,
            long shipB,
            Vector3d localA,
            Vector3d localB,
            double maxLength,
            float maxForce,
            float tolerance,
            float stiffness,
            float damping,
            PulleyJointCreated callback
    ) {
        try {
            VSDistanceJoint joint = new VSDistanceJoint(
                    shipA,
                    new VSJointPose(localA, new Quaterniond()),
                    shipB,
                    new VSJointPose(localB, new Quaterniond()),
                    new VSJointMaxForceTorque(maxForce, maxForce),
                    0f,
                    (float) maxLength,
                    tolerance,
                    stiffness,
                    damping
            );

            gtpa.addJoint(joint, 0, newId -> {
                TickablePulleyJoint tickable = new TickablePulleyJoint(newId, joint);
                trackedJoints.put(newId, tickable);
                if (callback != null) callback.accept(newId);
            });

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removePulleyJoint(GameToPhysicsAdapter gtpa, int constraintId) {
        gtpa.removeJoint(constraintId);
        trackedJoints.remove(constraintId);
    }

    public static void updatePulleyJointLength(GameToPhysicsAdapter gtpa, int constraintId, float newLength) {
        PulleyJointTickable tickable = trackedJoints.get(constraintId);
        if (tickable == null) {
            System.err.println("PulleyUtils.updatePulleyJointLength: no tracked joint for id " + constraintId);
            return;
        }

        VSDistanceJoint known = tickable.getJoint();
        VSDistanceJoint updated = known.copy(
                known.getShipId0(),
                known.getPose0(),
                known.getShipId1(),
                known.getPose1(),
                known.getMaxForceTorque(),
                known.getMinDistance(),
                newLength,
                known.getTolerance(),
                known.getStiffness(),
                known.getDamping()
        );

        tickable.setJoint(updated);

        VSJointAndId wrapped = new VSJointAndId(constraintId, updated);
        gtpa.updateJoint(wrapped);
    }


    public static Map<Integer, PulleyJointTickable> getTrackedJoints() {
        return trackedJoints;
    }

    public interface PulleyJointCreated {
        void accept(int constraintId);
    }

    public interface PulleyJointTickable {
        void tick(PhysLevel physLevel);
        VSDistanceJoint getJoint();
        void setJoint(VSDistanceJoint joint);
    }

    public static class TickablePulleyJoint implements PulleyJointTickable {
        private final int id;
        private VSDistanceJoint joint;

        public TickablePulleyJoint(int id, VSDistanceJoint joint) {
            this.id = id;
            this.joint = joint;
        }

        @Override
        public void tick(PhysLevel physLevel) {
        }

        @Override
        public VSDistanceJoint getJoint() {
            return joint;
        }

        @Override
        public void setJoint(VSDistanceJoint joint) {
            this.joint = joint;
        }

        public int getId() {
            return id;
        }
    }
}
