package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.RopeStyles;
import org.valkyrienskies.core.internal.joints.*;
import yay.evy.everest.vstuff.content.constraint.RopeUtil.*;

import javax.annotation.Nullable;
import java.util.UUID;

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
    public RopeType type;
    public RopeStyles.RopeStyle style;
    boolean hasPhysicalImpact = true;
    public boolean hasRestoredJoint = false;
    private Integer physicsId = null;

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

        this.type = RopeType.SS;
        if (shipA == null) {
            this.type = RopeType.WS;
            this.shipAIsGround = true;
        }

        if (shipB == null) {
            this.type =  RopeType.WW;
            this.shipBIsGround = true;
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


    /**
     * Removes a Rope's joint and sets its id and constraint to null.
     * This method calls ConstraintTracker.removeConstraintWithPersistence
     * and NetworkHandler.sendConstraintRemove, so it is not needed to be done outside
     * of this method
     * @param level the ServerLevel to remove the joint from
     * @return if the method succeeded
     */
    public boolean removeJoint(ServerLevel level) {
        this.level = level;
        this.levelId = RopeUtil.registerLevel(level);

        if (this.ID != null) {
            NetworkHandler.sendConstraintRemove(this.ID);
        }

        if (hasPhysicalImpact && physicsId != null) {
            try {
                var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level));
                gtpa.removeJoint(this.physicsId);
                //  VStuff.LOGGER.info("Successfully removed physics joint {}", physicsId);
            } catch (Exception e) {
                //  VStuff.LOGGER.error("Failed to remove physics joint for rope {}: {}", ID, e.getMessage());
            }
        }

        this.physicsId = null;
        this.constraint = null;
        this.hasRestoredJoint = false;

        ConstraintTracker.removeConstraintWithPersistence(level, this.ID);

        return true;
    }

    public boolean setJointLength(ServerLevel level, float newLength) {
        if (!hasPhysicalImpact || physicsId == null || constraint == null) return false;

        try {
            Long actualShipA = shipAIsGround ? null : shipA;
            Long actualShipB = shipBIsGround ? null : shipB;

            VSDistanceJoint newConstraint = new VSDistanceJoint(
                    actualShipA,
                    constraint.getPose0(),
                    actualShipB,
                    constraint.getPose1(),
                    constraint.getMaxForceTorque(),
                    constraint.getCompliance(),
                    1F,
                    newLength,
                    constraint.getTolerance(),
                    constraint.getStiffness(),
                    constraint.getDamping()
            );

            String dimensionId = ValkyrienSkies.getDimensionId(level);
            var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(dimensionId);

            gtpa.updateJoint(new VSJointAndId(this.physicsId, newConstraint));

            this.constraint = newConstraint;
            this.maxLength = (double) newLength;
            return true;

        } catch (Exception e) {
            VStuff.LOGGER.error("Failed to update VS Joint {}: {}", physicsId, e.getMessage());
            return false;
        }
    }



    /**
     * Restores a Rope's joint and sets its id and constraint.
     * This method calls ConstraintTracker.addConstraintWithPersistence.
     * <br></br>
     * This method will silently fail if one (or more) of the ropes attached ships can't be found.
     * However, the ropes ID will become -1 in that situation.
     *
     * @param level the ServerLevel to restore the joint to
     */


    public void restoreJoint(ServerLevel level) {
        if (!hasPhysicalImpact) return;
        if (hasRestoredJoint && this.physicsId != null) return;

        this.level = level;
        this.levelId = RopeUtil.registerLevel(level);
        this.hasRestoredJoint = true;
        this.physicsId = null;

        Long actualShipA = shipAIsGround ? null : shipA;
        Long actualShipB = shipBIsGround ? null : shipB;

        VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        boolean shipAValid = shipAIsGround || (actualShipA != null && shipWorld.getAllShips().getById(actualShipA) != null);
        boolean shipBValid = shipBIsGround || (actualShipB != null && shipWorld.getAllShips().getById(actualShipB) != null);

        if (!shipAValid || !shipBValid) {
            this.hasRestoredJoint = false;
            return;
        }

        VSDistanceJoint ropeConstraint = (VSDistanceJoint) makeDistanceJoint(actualShipA, actualShipB);
        var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level));

        gtpa.addJoint(ropeConstraint, 0, newConstraintId -> {
            this.physicsId = newConstraintId;
            this.constraint = ropeConstraint;

            if (!ConstraintTracker.getActiveRopes().containsKey(this.ID)) {
                ConstraintTracker.addConstraintWithPersistence(this);
            } else {
                ConstraintTracker.getActiveRopes().put(this.ID, this);
            }

            MinecraftServer server = level.getServer();
            if (server != null) {
                for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
                    ConstraintTracker.syncAllConstraintsToPlayer(sp);
                }
            }
         //   VStuff.LOGGER.info("Successfully restored Physics Joint for Rope ID: {} (Physics ID: {})", this.ID, newConstraintId);
        });
    }

    private @NotNull VSDistanceJoint makeDistanceJoint(Long actualShipA, Long actualShipB, double maxLength) {
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
                compliance,
                0f,
                (float) maxLength,
                0.1f,
                1e8f,
                100f
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
        Vector3d firstLocalPos = RopeUtil.getLocalPos(level, firstClickedPos);

        Vector3d secondWorldPos = RopeUtil.getWorldPosition(level, secondClickedPos, secondShipId);
        Vector3d secondLocalPos = RopeUtil.getLocalPos(level, secondClickedPos);

        return createNew(
                ropeItem, level,
                firstShipId, secondShipId,
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
        boolean shipAIsWorld = shipA == null;
        boolean shipBIsWorld = shipB == null;

        if (!shipAIsWorld && shipBIsWorld) {
            Long tmpShip = shipA; shipA = shipB; shipB = tmpShip;
            Vector3d tmpLocal = localPosA; localPosA = localPosB; localPosB = tmpLocal;
            Vector3d tmpWorld = worldPosA; worldPosA = worldPosB; worldPosB = tmpWorld;
        }

        double distance = worldPosA.distance(worldPosB);
        double maxAllowedLength = VstuffConfig.MAX_ROPE_LENGTH.get();
        if (distance > maxAllowedLength && player != null) {
            player.displayClientMessage(Component.literal("§cRope too long! Max length is " + maxAllowedLength + " blocks."), true);
            return RopeUtil.RopeReturn.FAIL;
        }

        double maxLength = distance + 0.5;
        double massA = RopeUtil.getMassForShip(level, shipA);
        double massB = RopeUtil.getMassForShip(level, shipB);
        double effectiveMass = Math.max(Math.min(massA, massB), 100.0);
        double compliance = (shipAIsWorld || shipBIsWorld) ? 1e-12 / effectiveMass * 0.05 : 1e-12 / effectiveMass;
        double massRatio = Math.max(massA, massB) / Math.min(massA, massB);
        double maxForce = 5e13 * Math.min(massRatio, 20.0) * (shipAIsWorld || shipBIsWorld ? 10 : 1);

        RopeStyles.RopeStyle ropeStyle = null;
        if (player instanceof ServerPlayer serverPlayer) {
            ropeStyle = RopeStyleHandlerServer.getStyle(serverPlayer.getUUID());
        }
        if (ropeStyle == null) {
            ropeStyle = new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
        }

        int persistentId = ConstraintTracker.getNextId();

        if (shipAIsWorld && shipBIsWorld) {
            Rope rope = new Rope(level, persistentId, null, null, localPosA, localPosB,
                    maxLength, compliance, maxForce, ConstraintType.GENERIC, null, ropeStyle, null);
            rope.hasRestoredJoint = true;
            ConstraintTracker.addConstraintWithPersistence(rope);
            if (player instanceof ServerPlayer sp) ConstraintTracker.syncAllConstraintsToPlayer(sp);
            return new RopeReturn(RopeInteractionReturn.SUCCESS, rope);
        }

        VSDistanceJoint ropeConstraint = new VSDistanceJoint(
                shipA, new VSJointPose(localPosA, new Quaterniond()),
                shipB, new VSJointPose(localPosB, new Quaterniond()),
                new VSJointMaxForceTorque((float) maxForce, (float) maxForce),
                compliance,
                0f,
                (float) maxLength,
                1f,
                1e8f,
                100f
        );

        Rope rope = new Rope(level, persistentId, shipA, shipB, localPosA, localPosB,
                maxLength, compliance, maxForce, ConstraintType.GENERIC, null, ropeStyle, ropeConstraint);

        var gtpa = ValkyrienSkiesMod.getOrCreateGTPA(ValkyrienSkies.getDimensionId(level));

        gtpa.addJoint(ropeConstraint, 0, newConstraintId -> {
            rope.physicsId = newConstraintId;
            rope.hasRestoredJoint = true;
            ConstraintTracker.addConstraintWithPersistence(rope);
            if (player instanceof ServerPlayer sp) ConstraintTracker.syncAllConstraintsToPlayer(sp);
        });

        return new RopeReturn(RopeInteractionReturn.SUCCESS, rope);
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
                // VStuff.LOGGER.warn("Invalid constraint type in save data, defaulting to GENERIC: {}", e.getMessage());
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

        RopeStyles.RopeStyle ropeStyle = RopeStyles.fromString(style);

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

        return constraintTag;
    }

    public void setSourceBlockPos(BlockPos sourceBlockPos) {
        this.sourceBlockPos = sourceBlockPos;
    }
    public Integer getPhysicsId() {
        return this.physicsId;
    }
}

/*
hidden devlog 1
wren here
i've been lost in the rope forest for so many days now... i don't know if i'll ever get out
so many ropes...
 */