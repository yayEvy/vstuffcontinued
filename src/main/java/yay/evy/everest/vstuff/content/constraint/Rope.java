package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;
import org.joml.Quaterniond;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;
import org.valkyrienskies.core.internal.joints.*;

public class Rope {

    public static RopeUtil.RopeInteractionReturn createNew(
            LeadConstraintItem ropeItem,
            ServerLevel level,
            BlockPos firstClickedPos,
            BlockPos secondClickedPos,
            Entity firstEntity,
            Long firstShipId,
            Long secondShipId,
            Player player
    ) {
        if (firstClickedPos == null && firstEntity == null) return RopeUtil.RopeInteractionReturn.FAIL;

        Vector3d firstWorldPos;
        Vector3d firstLocalPos;
        Long shipA;

        if (firstEntity != null) {
            firstWorldPos = new Vector3d(firstEntity.getX(), firstEntity.getY() + firstEntity.getBbHeight() / 2, firstEntity.getZ());
            shipA = firstShipId != null ? firstShipId : RopeUtil.getGroundBodyId(level);
            firstLocalPos = RopeUtil.convertWorldToLocal(level, firstWorldPos, shipA);
        } else {
            firstWorldPos = RopeUtil.getWorldPosition(level, firstClickedPos, firstShipId);
            shipA = firstShipId != null ? firstShipId : RopeUtil.getGroundBodyId(level);
            firstLocalPos = RopeUtil.getLocalPositionFixed(level, firstClickedPos, firstShipId, shipA);
        }

        Vector3d secondWorldPos = RopeUtil.getWorldPosition(level, secondClickedPos, secondShipId);
        Long shipB = secondShipId != null ? secondShipId : RopeUtil.getGroundBodyId(level);
        Vector3d secondLocalPos = RopeUtil.getLocalPositionFixed(level, secondClickedPos, secondShipId, shipB);

        return createNew(
                ropeItem, level,
                shipA, shipB,
                firstLocalPos, secondLocalPos,
                firstWorldPos, secondWorldPos,
                player
        );
    }

    public static RopeUtil.RopeInteractionReturn createNew(
            LeadConstraintItem ropeItem,
            ServerLevel level,
            Long shipA, Long shipB,
            Vector3d localPosA, Vector3d localPosB,
            Vector3d worldPosA, Vector3d worldPosB,
            Player player
    ) {
        final Long groundBodyId = RopeUtil.getGroundBodyId(level);

        final Long finalShipA;
        final Long finalShipB;
        final Vector3d finalLocalPosA;
        final Vector3d finalLocalPosB;
        final Vector3d finalWorldPosA;
        final Vector3d finalWorldPosB;

        boolean shipAIsWorld = shipA.equals(groundBodyId);
        boolean shipBIsWorld = shipB.equals(groundBodyId);

        if (shipAIsWorld && !shipBIsWorld) {
            finalShipA = shipB;
            finalShipB = shipA;
            finalLocalPosA = localPosB;
            finalLocalPosB = localPosA;
            finalWorldPosA = worldPosB;
            finalWorldPosB = worldPosA;
        } else {
            finalShipA = shipA;
            finalShipB = shipB;
            finalLocalPosA = localPosA;
            finalLocalPosB = localPosB;
            finalWorldPosA = worldPosA;
            finalWorldPosB = worldPosB;
        }

        double distance = finalWorldPosA.distance(finalWorldPosB);
        double maxAllowedLength = VstuffConfig.MAX_ROPE_LENGTH.get();

        if (distance > maxAllowedLength && player != null) {
            player.displayClientMessage(
                    Component.literal("§cRope too long! Max length is " + maxAllowedLength + " blocks."),
                    true
            );
            return RopeUtil.RopeInteractionReturn.FAIL;
        }

        final double maxLength = distance + 0.5;
        final double massA = RopeUtil.getMassForShip(level, finalShipA);
        final double massB = RopeUtil.getMassForShip(level, finalShipB);
        double effectiveMass = Math.min(massA, massB);
        if (effectiveMass < 100.0) effectiveMass = 100.0;
        final double compliance = (shipAIsWorld || shipBIsWorld) ? 1e-12 / effectiveMass * 0.05 : 1e-12 / effectiveMass;
        double massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        final double maxForce = (shipAIsWorld || shipBIsWorld)
                ? 50000000000000.0 * Math.min(massRatio, 20.0) * 10.0
                : 50000000000000.0 * Math.min(massRatio, 20.0);

        try {
            String dimensionId = ValkyrienSkies.getDimensionId(level);
            var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(dimensionId);

            VSJoint ropeConstraint = new VSDistanceJoint(
                    finalShipA,
                    new VSJointPose(finalLocalPosA, new Quaterniond()),
                    finalShipB,
                    new VSJointPose(finalLocalPosB, new Quaterniond()),
                    new VSJointMaxForceTorque((float) maxForce, (float) maxForce),
                    0f,
                    (float) maxLength,
                    0f,
                    1f,
                    0.1f
            );

            final Player finalPlayer = player;

            gtpa.addJoint(ropeConstraint, 0, newConstraintId -> {
                RopeStyles.RopeStyle ropeStyle = null;
                if (finalPlayer instanceof ServerPlayer serverPlayer) {
                    ropeStyle = RopeStyleHandlerServer.getStyle(serverPlayer.getUUID());
                }

                if (ropeStyle == null) {
                    ropeStyle = new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
                }

                ConstraintTracker.addConstraintToTracker(
                        level,
                        newConstraintId,
                        finalShipA,
                        finalShipB,
                        finalLocalPosA,
                        finalLocalPosB,
                        maxLength,
                        compliance,
                        maxForce,
                        ConstraintTracker.RopeConstraintData.ConstraintType.GENERIC,
                        null,
                        ropeStyle
                );


                ConstraintTracker.mapConstraintToPersistenceId(newConstraintId, "manual_rope");

                if (finalPlayer instanceof ServerPlayer serverPlayer) {
                    ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);
                }
            });

            return RopeUtil.RopeInteractionReturn.SUCCESS;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error creating rope constraint: {}", e.getMessage());
            e.printStackTrace();
        }

        return RopeUtil.RopeInteractionReturn.FAIL;
    }
}
