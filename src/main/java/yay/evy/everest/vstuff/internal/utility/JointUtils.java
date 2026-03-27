package yay.evy.everest.vstuff.internal.utility;

import org.joml.Quaterniondc;
import org.joml.Vector3dc;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointAndId;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JointUtils {
    private static final double JOINT_POSITION_EPSILON = 1.0e-3;
    private static final double JOINT_ROTATION_DOT_EPSILON = 1.0e-5;

    private JointUtils() {}

    public static boolean isJointPoseFinite(VSJoint joint) {
        return isPoseFinite(joint.getPose0()) && isPoseFinite(joint.getPose1());
    }

    @Nullable
    public static VSJointAndId findMatchingJoint(GameToPhysicsAdapter gtpa, VSJoint target) {
        for (Map.Entry<Integer, VSJoint> entry : gtpa.getAllJoints().entrySet()) {
            if (matchesByAnchors(entry.getValue(), target)) {
                return new VSJointAndId(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    public static List<Integer> findMatchingJointIds(GameToPhysicsAdapter gtpa, VSJoint target) {
        List<Integer> jointIds = new ArrayList<>();
        for (Map.Entry<Integer, VSJoint> entry : gtpa.getAllJoints().entrySet()) {
            if (matchesByAnchors(entry.getValue(), target)) {
                jointIds.add(entry.getKey());
            }
        }
        return jointIds;
    }

    public static int removeMatchingJointsExcept(GameToPhysicsAdapter gtpa, VSJoint target, int keepJointId) {
        int removed = 0;
        for (Integer jointId : findMatchingJointIds(gtpa, target)) {
            if (jointId == keepJointId) {
                continue;
            }
            gtpa.removeJoint(jointId);
            removed++;
        }
        return removed;
    }

    public static boolean matchesByAnchors(VSJoint first, VSJoint second) {
        if (!first.getClass().equals(second.getClass())) {
            return false;
        }

        return matchesDirectAnchors(first, second) || matchesSwappedAnchors(first, second);
    }

    private static boolean matchesDirectAnchors(VSJoint first, VSJoint second) {
        return first.getShipId0() == second.getShipId0() &&
            first.getShipId1() == second.getShipId1() &&
            matchesPose(first.getPose0(), second.getPose0()) &&
            matchesPose(first.getPose1(), second.getPose1());
    }

    private static boolean matchesSwappedAnchors(VSJoint first, VSJoint second) {
        return first.getShipId0() == second.getShipId1() &&
            first.getShipId1() == second.getShipId0() &&
            matchesPose(first.getPose0(), second.getPose1()) &&
            matchesPose(first.getPose1(), second.getPose0());
    }

    private static boolean matchesPose(VSJointPose first, VSJointPose second) {
        return first.getPos().distanceSquared(second.getPos()) <= JOINT_POSITION_EPSILON * JOINT_POSITION_EPSILON &&
            rotationDot(first.getRot(), second.getRot()) >= 1.0 - JOINT_ROTATION_DOT_EPSILON;
    }

    private static boolean isPoseFinite(VSJointPose pose) {
        return isVectorFinite(pose.getPos()) && isQuaternionFinite(pose.getRot());
    }

    private static boolean isVectorFinite(Vector3dc vector) {
        return Double.isFinite(vector.x()) && Double.isFinite(vector.y()) && Double.isFinite(vector.z());
    }

    private static boolean isQuaternionFinite(Quaterniondc quaternion) {
        return Double.isFinite(quaternion.x()) &&
            Double.isFinite(quaternion.y()) &&
            Double.isFinite(quaternion.z()) &&
            Double.isFinite(quaternion.w());
    }

    private static double rotationDot(Quaterniondc first, Quaterniondc second) {
        return Math.abs(
            first.x() * second.x() +
                first.y() * second.y() +
                first.z() * second.z() +
                first.w() * second.w()
        );
    }
}
