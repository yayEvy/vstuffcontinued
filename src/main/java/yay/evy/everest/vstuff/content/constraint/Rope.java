package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Quaterniond;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.VstuffConfig;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;
import org.valkyrienskies.core.internal.joints.*;
import yay.evy.everest.vstuff.content.constraint.RopeUtil.*;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Rope {

    private @Nullable ServerLevel level;
    String levelId;
    public Integer ID;
    public Long shipA;
    public Long shipB;
    public boolean shipAIsGround;
    public boolean shipBIsGround;
    public Vector3d localPosA;
    public Vector3d localPosB;
    public double maxLength;
    public double compliance;
    public double maxForce;
    public ConstraintType constraintType;
    public net.minecraft.core.BlockPos sourceBlockPos;
    public RopeStyles.RopeStyle style;
    boolean hasPhysicalImpact = true;

    @Nullable VSDistanceJoint constraint;

    /**
     * yes rope very cool wow
     * this is used for anything with ropes, it stores all data
     */
    private Rope(@Nullable ServerLevel level, Integer constraintId, Long shipA, Long shipB, Vector3d localPosA,
                 Vector3d localPosB, double maxLength, double compliance, double maxForce, ConstraintType constraintType,
                 net.minecraft.core.BlockPos sourceBlockPos, RopeStyles.RopeStyle style, @Nullable VSDistanceJoint constraint) {
        this.level = level;
        this.levelId = RopeUtil.registerLevel(level);

        this.ID = constraintId;

        this.shipA = shipA;
        this.shipB = shipB;

        this.localPosA = localPosA;
        this.localPosB = localPosB;

        this.maxLength = maxLength;
        this.maxForce = maxForce;
        this.compliance = compliance;

        this.constraintType = constraintType;

        this.sourceBlockPos = sourceBlockPos;
        this.style = style;
        this.constraint = constraint;

        try {
            Long currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));

            this.shipAIsGround = shipA != null && shipA.equals(currentGroundBodyId);
            this.shipBIsGround = shipB != null && shipB.equals(currentGroundBodyId);
        } catch (Exception e) {
            VStuff.LOGGER.error("Rope creation failed to check ground body ID: {}", e.getMessage());
        }

        if (shipAIsGround && shipBIsGround) {
            this.hasPhysicalImpact = false;
            this.constraint = null;
        }
    }

    private Rope(@Nullable ServerLevel level, Integer constraintId, Long shipA, Long shipB, boolean shipAIsGround,
                 boolean shipBIsGround,Vector3d localPosA, Vector3d localPosB, double maxLength, double compliance,
                 double maxForce, ConstraintType constraintType, net.minecraft.core.BlockPos sourceBlockPos,
                 RopeStyles.RopeStyle style, @Nullable VSDistanceJoint constraint) {
        this.level = level;
        this.levelId = RopeUtil.registerLevel(level);

        this.ID = constraintId;

        this.shipA = shipA;
        this.shipB = shipB;
        this.shipAIsGround = shipAIsGround;
        this.shipBIsGround = shipBIsGround;

        this.localPosA = localPosA;
        this.localPosB = localPosB;

        this.maxLength = maxLength;
        this.maxForce = maxForce;
        this.compliance = compliance;

        this.constraintType = constraintType;

        this.sourceBlockPos = sourceBlockPos;
        this.style = style;
        this.constraint = constraint;
    }

    public @NotNull ServerLevel getLevel() {
        return level == null ? RopeUtil.getRegisteredLevel(levelId) : level;
    }

    public Vector3d getWorldPosA(ServerLevel level) {
        try {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            if (shipA.equals(groundBodyId)) {
                return new Vector3d(localPosA);
            } else {
                Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipA);
                if (shipObject != null) {
                    Vector3d worldPos = new Vector3d();
                    shipObject.getTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                    return worldPos;
                }
            }
            return new Vector3d(localPosA);
        } catch (Exception e) {
            return new Vector3d(localPosA);
        }
    }

    public Vector3d getWorldPosB(ServerLevel level) {
        try {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            if (shipB.equals(groundBodyId)) {
                return new Vector3d(localPosB);
            } else {
                Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipB);
                if (shipObject != null) {
                    Vector3d worldPos = new Vector3d();
                    shipObject.getTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                    return worldPos;
                }
            }
            return new Vector3d(localPosB);
        } catch (Exception e) {
            return new Vector3d(localPosB);
        }
    }

    // methods for join creation / deletion / editing

    /**
     * Removes a Rope's joint and sets its id and constraint to null.
     * This method calls ConstraintTracker.removeConstraintWithPersistence
     * and NetworkHandler.sendConstraintRemove, so it is not needed to be done outside
     * of this method
     * @param level the ServerLevel to remove the joint from
     * @return if the method succeeded
     */
    public boolean removeJoint(ServerLevel level) {
        if (!hasPhysicalImpact) {
            VStuff.LOGGER.info("nuh uh [not removing joint for rope without joint]");
            return false;
        }
        this.level = level;
        this.levelId = RopeUtil.registerLevel(level);

        if (constraint == null) {
            VStuff.LOGGER.warn("Cannot remove an already null joint!");
            return false;
        }

        try {
            String dimensionId = ValkyrienSkies.getDimensionId(level);
            var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(dimensionId);


            gtpa.removeJoint(ID);
            this.constraint = null;
            ConstraintTracker.removeConstraintWithPersistence(level, ID);
            NetworkHandler.sendConstraintRemove(ID);
            VStuff.LOGGER.info("Successfully removed joint with id {}", ID);
            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error removing joint for constraint {}: {}", ID, e.getMessage());
        }
        return false;
    }

    public boolean setJointLength(ServerLevel level, float newLength) {
        if (!hasPhysicalImpact) {
            VStuff.LOGGER.info("nuh uh [not settings joint length for rope without joint]");
            return false;
        }

        if (constraint == null) {
            VStuff.LOGGER.warn("Cannot change the length of a null joint!");
            return false;
        }

        try {
            String dimensionId = ValkyrienSkies.getDimensionId(level);
            var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(dimensionId);

            VSDistanceJoint newConstraint = constraint.copy(
                    constraint.getShipId0(),
                    constraint.getPose0(),
                    constraint.getShipId1(),
                    constraint.getPose1(),
                    constraint.getMaxForceTorque(),
                    constraint.getMinDistance(),
                    newLength,
                    constraint.getTolerance(),
                    1e10f,
                    1000f
            );

            gtpa.updateJoint(new VSJointAndId(ID, newConstraint));
            constraint = newConstraint;
            maxLength = newConstraint.getMaxDistance();
            System.out.println(maxLength);
        } catch (Exception e) {
            VStuff.LOGGER.error("Error updating joint for constraint {}: {}", ID, e.getMessage());
        }
        return false;
    }
    /**
     * Restores a Rope's joint and sets its id and constraint.
     * This method calls ConstraintTracker.addConstraintWithPersistence.
     * @param level the ServerLevel to restore the joint to
     * @return if the method succeeded
     */
    public boolean restoreJoint(ServerLevel level) {
        if (!hasPhysicalImpact) {
            VStuff.LOGGER.info("nuh uh [not restoring joint for rope without joint]");
            return false;
        }
        this.level = level;
        this.levelId = RopeUtil.registerLevel(level);

        Long currentGroundBodyId;

        try {
            currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
        } catch (Exception e) {
            VStuff.LOGGER.warn("Exception occurred while trying to get ground body id: {}", e.getMessage());
            return false;
        }

        Long actualShipA = shipAIsGround ? currentGroundBodyId : shipA;
        Long actualShipB = shipBIsGround ? currentGroundBodyId : shipB;

        try {
            String dimensionId = ValkyrienSkies.getDimensionId(level);
            var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(dimensionId);

            VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

            boolean shipAValid = shipAIsGround ||
                    (actualShipA != null && shipWorld.getAllShips().getById(actualShipA) != null);
            boolean shipBValid = shipBIsGround ||
                    (actualShipB != null && shipWorld.getAllShips().getById(actualShipB) != null);

            if (!shipAValid || !shipBValid) return false;

            VSJoint ropeConstraint = makeDistanceJoint(actualShipA, actualShipB);

            gtpa.addJoint(ropeConstraint, 0, newConstraintId -> {
                ConstraintTracker.addConstraintToTracker(this);

                ID = newConstraintId;
            });



            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error restoring joint for constraint {}: {}", ID, e.getMessage());
        }
        return false;
    }


    private @NotNull VSJoint makeDistanceJoint(Long actualShipA, Long actualShipB, double maxLength) {
        VSJointPose poseA = new VSJointPose(localPosA, new Quaterniond());
        VSJointPose poseB = new VSJointPose(localPosB, new Quaterniond());

        return new VSDistanceJoint(
                actualShipA,
                poseA,
                actualShipB,
                poseB,
                new VSJointMaxForceTorque(
                        (float) maxForce,
                        (float) maxForce
                ),
                0f,
                (float) maxLength,
                0f,
                1f,
                0.1f
        );
    }

    private @NotNull VSJoint makeDistanceJoint(Long actualShipA, Long actualShipB) {
        return makeDistanceJoint(actualShipA, actualShipB, (float) maxLength);
    }

    // static methods for object creation

    /**
     * Creates a new rope object and a new VSDistanceJoint.
     * Adds itself to persistence and syncs constraints.
     * @param ropeItem the LeadConstraintItem being used.
     * @param level the Serverlevel to create the joint in
     * @param firstClickedPos the first clicked BlockPos to create the rope from
     * @param secondClickedPos the second clicked BlockPos to create the rope to
     * @param firstShipId the id of the body that firstClickedPos belongs to
     * @param secondShipId the id of the body that secondClickedPos belongs to
     * @param player player
     * @return yes
     */
    public static RopeReturn createNew(
            LeadConstraintItem ropeItem,
            ServerLevel level,
            BlockPos firstClickedPos,
            BlockPos secondClickedPos,
            Long firstShipId,
            Long secondShipId,
            Player player
    ) {
        Vector3d firstWorldPos = RopeUtil.getWorldPosition(level, firstClickedPos, firstShipId);
        Long shipA = firstShipId != null ? firstShipId : RopeUtil.getGroundBodyId(level);
        Vector3d firstLocalPos = RopeUtil.getLocalPositionFixed(level, firstClickedPos, firstShipId, shipA);

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

    public static RopeReturn createNew(
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
            return RopeUtil.RopeReturn.FAIL;
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

            VSDistanceJoint ropeConstraint = new VSDistanceJoint(
                    finalShipA,
                    new VSJointPose(finalLocalPosA, new Quaterniond()),
                    finalShipB,
                    new VSJointPose(finalLocalPosB, new Quaterniond()),
                    new VSJointMaxForceTorque((float) maxForce, (float) maxForce),
                    0f,
                    (float) maxLength,
                    null,
                    null,
                    null
            );

            final Player finalPlayer = player;

            if (shipAIsWorld && shipBIsWorld) {
                RopeStyles.RopeStyle ropeStyle = null;
                Rope rope;
                if (finalPlayer instanceof ServerPlayer serverPlayer) {
                    ropeStyle = RopeStyleHandlerServer.getStyle(serverPlayer.getUUID());
                }

                if (ropeStyle == null) {
                    ropeStyle = new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
                }

                rope = new Rope(
                        level, UUID.randomUUID().hashCode(), finalShipA, finalShipB,
                        finalLocalPosA, finalLocalPosB, maxLength, compliance, maxForce,
                        ConstraintType.GENERIC, null, ropeStyle, null
                );
                rope.hasPhysicalImpact = false;

                ConstraintTracker.addConstraintWithPersistence(rope);

                if (finalPlayer instanceof ServerPlayer serverPlayer) {
                    ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);
                }

                return new RopeReturn(RopeInteractionReturn.SUCCESS, rope);
            } else {
                RopeStyles.RopeStyle ropeStyle = null;
                if (finalPlayer instanceof ServerPlayer serverPlayer) {
                    ropeStyle = RopeStyleHandlerServer.getStyle(serverPlayer.getUUID());
                }

                if (ropeStyle == null) {
                    ropeStyle = new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
                }

                Rope rope = new Rope(
                        level, -1, finalShipA, finalShipB, // id is temporary
                        finalLocalPosA, finalLocalPosB, maxLength, compliance, maxForce,
                        ConstraintType.GENERIC, null, ropeStyle, ropeConstraint
                );

                gtpa.addJoint(ropeConstraint, 0, newConstraintId -> rope.ID = newConstraintId);

                ConstraintTracker.addConstraintWithPersistence(rope);

                if (finalPlayer instanceof ServerPlayer serverPlayer) {
                    ConstraintTracker.syncAllConstraintsToPlayer(serverPlayer);
                }
                return new RopeReturn(RopeInteractionReturn.SUCCESS, rope);
            }
        } catch (Exception e) {
            VStuff.LOGGER.error("Error creating rope constraint: {}", e.getMessage());
            e.printStackTrace();
        }

        return RopeReturn.FAIL;
    }

    /**
     * Create a Rope from data in a CompoundTag, except the level and constraint parameters,
     * as those cannot be restored from the tag.
     * @param tag The tag in which the data is stored
     * @return the Rope restored from the tag data
     */
    public static Rope fromTag(CompoundTag tag) {
        ConstraintType constraintType = ConstraintType.GENERIC;

        long shipALong = tag.getLong("shipA");
        long shipBLong = tag.getLong("shipB");
        Long shipA = shipALong == 0L ? null : shipALong;
        Long shipB = shipBLong == 0L ? null : shipBLong;

        if (tag.contains("constraintType")) {
            try {
                constraintType = ConstraintType.valueOf(tag.getString("constraintType"));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid constraint type in save data, defaulting to GENERIC: " + e.getMessage());
            }
        }

        BlockPos sourceBlockPos = null;
        if (tag.contains("sourceBlockPos_x")) {
            sourceBlockPos = new BlockPos(
                    tag.getInt("sourceBlockPos_x"),
                    tag.getInt("sourceBlockPos_y"),
                    tag.getInt("sourceBlockPos_z")
            );
        }

        String style = tag.contains("style") ? tag.getString("style") : "normal";
        String primitiveType = tag.contains("primitiveStyle") ? tag.getString("primitiveStyle") : "normal";
        String styleLKey = tag.contains("styleLKey") ? tag.getString("styleLKey") : "vstuff.rope.normal";

        RopeStyles.RopeStyle ropeStyle = new RopeStyles.RopeStyle(style, primitiveType, styleLKey);

        String levelId = tag.getString("levelId");

        return new Rope(
                RopeUtil.getRegisteredLevel(levelId),
                tag.getInt("id"),
                shipA,
                shipB,
                tag.getBoolean("shipAIsGround"),
                tag.getBoolean("shipBIsGround"),
                new Vector3d(tag.getDouble("localPosA_x"), tag.getDouble("localPosA_y"), tag.getDouble("localPosA_z")),
                new Vector3d(tag.getDouble("localPosB_x"), tag.getDouble("localPosB_y"), tag.getDouble("localPosB_z")),
                tag.getDouble("maxLength"),
                tag.getDouble("compliance"),
                tag.getDouble("maxForce"),
                constraintType,
                sourceBlockPos,
                ropeStyle,
                null
        );
    }

    /**
     * Convert data in the Rope class to a CompoundTag
     * @return the fuck do you think it returns
     */
    public CompoundTag toTag() {
        CompoundTag constraintTag = new CompoundTag();

        constraintTag.putInt("id", ID);
        constraintTag.putString("levelId", levelId);
        constraintTag.putLong("shipA", shipA != null ? shipA : 0L);
        constraintTag.putLong("shipB", shipB != null ? shipB : 0L);
        constraintTag.putBoolean("shipAIsGround", shipAIsGround);
        constraintTag.putBoolean("shipBIsGround", shipBIsGround);

        constraintTag.putDouble("localPosA_x", localPosA.x);
        constraintTag.putDouble("localPosA_y", localPosA.y);
        constraintTag.putDouble("localPosA_z", localPosA.z);
        constraintTag.putDouble("localPosB_x", localPosB.x);
        constraintTag.putDouble("localPosB_y", localPosB.y);
        constraintTag.putDouble("localPosB_z", localPosB.z);

        constraintTag.putDouble("maxLength", maxLength);
        constraintTag.putDouble("compliance", compliance);
        constraintTag.putDouble("maxForce", maxForce);

        constraintTag.putString("constraintType", constraintType.name());

        if (sourceBlockPos != null) {
            constraintTag.putInt("sourceBlockPos_x", sourceBlockPos.getX());
            constraintTag.putInt("sourceBlockPos_y", sourceBlockPos.getY());
            constraintTag.putInt("sourceBlockPos_z", sourceBlockPos.getZ());
        }

        constraintTag.putString("style", style.getStyle());
        constraintTag.putString("primitiveStyle", style.getBasicStyle().name().toLowerCase());
        constraintTag.putString("styleLKey", style.getLangKey());

        return constraintTag;
    }

}