package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointAndId;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

public class GTPAUtils {

    public static GameToPhysicsAdapter getGTPA(ServerLevel level) {
        String dimId = ValkyrienSkies.getDimensionId(level);
        return ValkyrienSkiesMod.getOrCreateGTPA(dimId);
    }


    public static void addRopeJoint(ServerLevel level, ReworkedRope rope, Player player) {
        VSDistanceJoint distanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        if (!JointUtils.isJointPoseFinite(distanceJoint)) {
            VStuff.LOGGER.warn("Rejecting corrupted rope during creation: non-finite pose data.");
            return;
        }

        gtpa.addJoint(distanceJoint, 0, (jointId) -> VSGameUtilsKt.executeOrSchedule(level, () -> {
            if (jointId == -1) {
                VStuff.LOGGER.warn("Failed to create rope joint, returning.");
                return;
            }

            RopeManager.get(level).addRope(rope);

            rope.setJointId(jointId);
            JointUtils.removeMatchingJointsExcept(gtpa, distanceJoint, jointId);

            rope.attachActors(level);

            if (player instanceof ServerPlayer serverPlayer) {
                RopeManager.syncAllRopesToPlayer(serverPlayer);
            }
        }));
    }

    public static void editJoint(ServerLevel level, ReworkedRope rope) {
        VSDistanceJoint newDistanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        if (!JointUtils.isJointPoseFinite(newDistanceJoint)) {
            VStuff.LOGGER.warn(
                "Discarding corrupted rope joint {} during update and removing the rope to prevent instability.",
                rope.getRopeId()
            );
            removeJoint(level, rope);
            return;
        }

        VSJointAndId resolvedJoint = resolveTrackedJoint(level, gtpa, rope, newDistanceJoint);

        if (resolvedJoint != null) {
            System.out.println("update joint");
            gtpa.updateJoint(new VSJointAndId(resolvedJoint.getJointId(), newDistanceJoint));
//            rope.setRopeId(resolvedJoint.getJointId());
            JointUtils.removeMatchingJointsExcept(gtpa, newDistanceJoint, resolvedJoint.getJointId());
            return;
        }

        gtpa.addJoint(newDistanceJoint, 0, jointId -> {
            if (jointId == -1) {
                VStuff.LOGGER.warn("Failed to recreate missing rope joint for rope {}", rope.getRopeId());
                return;
            }

            rope.unsafeSetJointId(jointId);
            JointUtils.removeMatchingJointsExcept(gtpa, newDistanceJoint, jointId);
        });
    }

    public static void removeJoint(ServerLevel level, ReworkedRope rope) {
        GameToPhysicsAdapter gtpa = getGTPA(level);

        Set<Integer> jointIdsToRemove = new LinkedHashSet<>();

        if (rope.hasTrackedJoint()) {
            jointIdsToRemove.add(rope.getJointId());
        }

        VSDistanceJoint expectedJoint = rope.makeJoint();

        if (JointUtils.isJointPoseFinite(expectedJoint))
            jointIdsToRemove.addAll(JointUtils.findMatchingJointIds(gtpa, expectedJoint));
        else
            VStuff.LOGGER.warn("Rope {} has corrupted pose data during removal; removing tracked joint ids only.", rope.getRopeId());

        for (Integer jointId : jointIdsToRemove) {
            gtpa.removeJoint(jointId);
        }

        rope.detachActors(level);

        RopeManager.get(level).removeRope(rope.getRopeId());
    }

    @Nullable
    private static VSJointAndId resolveTrackedJoint(ServerLevel level, GameToPhysicsAdapter gtpa, ReworkedRope rope, VSDistanceJoint expectedJoint) {
        if (rope.hasTrackedJoint()) {
            VSJoint existingJoint = gtpa.getJointById(rope.getJointId());
            if (existingJoint instanceof VSDistanceJoint existingDistanceJoint) {
                if (!JointUtils.isJointPoseFinite(existingDistanceJoint)) {
                    VStuff.LOGGER.warn(
                        "Discarding invalid restored rope joint {} for rope {}.",
                        rope.getJointId(),
                        rope.getRopeId()
                    );
                    gtpa.removeJoint(rope.getJointId());
                    RopeManager.get(level).removeRope(rope.getRopeId());
                } else {
                    JointUtils.removeMatchingJointsExcept(gtpa, existingDistanceJoint, rope.getJointId());
                    return new VSJointAndId(rope.getJointId(), existingDistanceJoint);
                }
            } else if (existingJoint != null) {
                VStuff.LOGGER.warn(
                    "Discarding joint {} for rope {} because it resolved to a joint type that was not DISTANCE: {}.",
                    rope.getJointId(),
                    rope.getRopeId(),
                    existingJoint.getJointType()
                );
                gtpa.removeJoint(rope.getJointId());
                RopeManager.get(level).removeRope(rope.getRopeId());
            }
        }

        VSJointAndId matchingJoint = JointUtils.findMatchingJoint(gtpa, expectedJoint);

        if (matchingJoint == null) return null;

        if (!(matchingJoint.getJoint() instanceof VSDistanceJoint matchingDistanceJoint)) {
            VStuff.LOGGER.warn(
                "Discarding structurally matching joint {} for rope {} because it resolved to a joint type that was not DISTANCE: {}.",
                matchingJoint.getJointId(),
                rope.getRopeId(),
                matchingJoint.getJoint().getJointType()
            );
            gtpa.removeJoint(matchingJoint.getJointId());
            return null;
        }

        if (!JointUtils.isJointPoseFinite(matchingDistanceJoint)) {
            VStuff.LOGGER.warn(
                "Discarding invalid structurally matching rope joint {} for rope {}.",
                matchingJoint.getJointId(),
                rope.getRopeId()
            );
            gtpa.removeJoint(matchingJoint.getJointId());
            return null;
        }

        rope.unsafeSetJointId(matchingJoint.getJointId());
        JointUtils.removeMatchingJointsExcept(gtpa, matchingDistanceJoint, matchingJoint.getJointId());
        return new VSJointAndId(matchingJoint.getJointId(), matchingDistanceJoint);
    }

}
