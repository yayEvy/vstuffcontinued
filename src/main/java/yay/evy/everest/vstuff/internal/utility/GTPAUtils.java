package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointAndId;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeManager;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

public class GTPAUtils {

    public static GameToPhysicsAdapter getGTPA(ServerLevel level) {
        String dimId = ValkyrienSkies.getDimensionId(level);
        return ValkyrienSkiesMod.getOrCreateGTPA(dimId);
    }

    public static void addRopeJoint(ServerLevel level, Player player, ReworkedRope rope) {
        VSDistanceJoint distanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        if (!JointUtils.isJointPoseFinite(distanceJoint)) {
            VStuff.LOGGER.warn(
                "Rejecting corrupted rope joint {} during creation: non-finite pose data.",
                rope.ropeId
            );
            return;
        }

        gtpa.addJoint(distanceJoint, 0, (jointId) -> {
            if (jointId == -1) {
                VStuff.LOGGER.warn("Failed to create rope joint for rope {}", rope.ropeId);
                return;
            }
            rope.jointId = jointId;
            JointUtils.removeMatchingJointsExcept(gtpa, distanceJoint, jointId);

            RopeManager.addRopeWithPersistence(level, rope);
            rope.attachActors(level);

            if (player instanceof ServerPlayer serverPlayer) {
                RopeManager.syncAllRopesToPlayer(serverPlayer);
            }
        });
    }

    public static void editJoint(ServerLevel level, ReworkedRope rope) {
        VSDistanceJoint newDistanceJoint = rope.makeJoint();
        GameToPhysicsAdapter gtpa = getGTPA(level);
        if (!JointUtils.isJointPoseFinite(newDistanceJoint)) {
            VStuff.LOGGER.warn(
                "Discarding corrupted rope joint {} during update and removing the rope to prevent instability.",
                rope.ropeId
            );
            removeJoint(level, rope);
            return;
        }

        VSJointAndId resolvedJoint = resolveTrackedJoint(level, gtpa, rope, newDistanceJoint);
        if (resolvedJoint != null) {
            gtpa.updateJoint(new VSJointAndId(resolvedJoint.getJointId(), newDistanceJoint));
            rope.jointId = resolvedJoint.getJointId();
            JointUtils.removeMatchingJointsExcept(gtpa, newDistanceJoint, resolvedJoint.getJointId());
            return;
        }

        gtpa.addJoint(newDistanceJoint, 0, jointId -> {
            if (jointId == -1) {
                VStuff.LOGGER.warn("Failed to recreate missing rope joint for rope {}", rope.ropeId);
                return;
            }
            rope.jointId = jointId;
            JointUtils.removeMatchingJointsExcept(gtpa, newDistanceJoint, jointId);
        });
    }

    @Nullable
    public static VSJointAndId resolveTrackedJoint(ServerLevel level, ReworkedRope rope) {
        VSDistanceJoint expectedJoint = rope.makeJoint();
        if (!JointUtils.isJointPoseFinite(expectedJoint)) {
            VStuff.LOGGER.warn(
                "Discarding corrupted rope joint state {} while reconciling restore data.",
                rope.ropeId
            );
            removeJoint(level, rope);
            return null;
        }
        return resolveTrackedJoint(level, getGTPA(level), rope, expectedJoint);
    }

    public static void removeJoint(ServerLevel level, ReworkedRope rope) {
        GameToPhysicsAdapter gtpa = getGTPA(level);
        Set<Integer> jointIdsToRemove = new LinkedHashSet<>();
        if (rope.hasTrackedJoint()) {
            jointIdsToRemove.add(rope.jointId);
        }

        VSDistanceJoint expectedJoint = rope.makeJoint();
        if (JointUtils.isJointPoseFinite(expectedJoint)) {
            jointIdsToRemove.addAll(JointUtils.findMatchingJointIds(gtpa, expectedJoint));
        } else {
            VStuff.LOGGER.warn(
                "Rope {} has corrupted pose data during removal; removing tracked joint ids only.",
                rope.ropeId
            );
        }

        for (Integer jointId : jointIdsToRemove) {
            gtpa.removeJoint(jointId);
        }

        rope.jointId = null;

        rope.detachActors(level);

        RopeManager.removeRopeWithPersistence(level, rope.ropeId);
    }

    @Nullable
    private static VSJointAndId resolveTrackedJoint(ServerLevel level, GameToPhysicsAdapter gtpa, ReworkedRope rope, VSDistanceJoint expectedJoint) {
        if (rope.hasTrackedJoint()) {
            VSJoint existingJoint = gtpa.getJointById(rope.jointId);
            if (existingJoint instanceof VSDistanceJoint existingDistanceJoint) {
                if (!JointUtils.isJointPoseFinite(existingDistanceJoint)) {
                    VStuff.LOGGER.warn(
                        "Discarding invalid restored rope joint {} for rope {}.",
                        rope.jointId,
                        rope.ropeId
                    );
                    gtpa.removeJoint(rope.jointId);
                    rope.jointId = null;
                } else {
                    JointUtils.removeMatchingJointsExcept(gtpa, existingDistanceJoint, rope.jointId);
                    return new VSJointAndId(rope.jointId, existingDistanceJoint);
                }
            } else if (existingJoint != null) {
                VStuff.LOGGER.warn(
                    "Discarding rope joint {} for rope {} because it resolved to the wrong joint type: {}.",
                    rope.jointId,
                    rope.ropeId,
                    existingJoint.getClass().getSimpleName()
                );
                gtpa.removeJoint(rope.jointId);
                rope.jointId = null;
            }
        }

        VSJointAndId matchingJoint = JointUtils.findMatchingJoint(gtpa, expectedJoint);
        if (matchingJoint == null) {
            return null;
        }
        if (!(matchingJoint.getJoint() instanceof VSDistanceJoint matchingDistanceJoint)) {
            VStuff.LOGGER.warn(
                "Discarding structurally matched joint {} for rope {} because it resolved to the wrong joint type: {}.",
                matchingJoint.getJointId(),
                rope.ropeId,
                matchingJoint.getJoint().getClass().getSimpleName()
            );
            gtpa.removeJoint(matchingJoint.getJointId());
            return null;
        }
        if (!JointUtils.isJointPoseFinite(matchingDistanceJoint)) {
            VStuff.LOGGER.warn(
                "Discarding invalid structurally matched rope joint {} for rope {}.",
                matchingJoint.getJointId(),
                rope.ropeId
            );
            gtpa.removeJoint(matchingJoint.getJointId());
            return null;
        }

        rope.jointId = matchingJoint.getJointId();
        JointUtils.removeMatchingJointsExcept(gtpa, matchingDistanceJoint, matchingJoint.getJointId());
        return new VSJointAndId(matchingJoint.getJointId(), matchingDistanceJoint);
    }

}
