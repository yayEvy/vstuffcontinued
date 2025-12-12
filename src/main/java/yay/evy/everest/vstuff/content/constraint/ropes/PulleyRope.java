package yay.evy.everest.vstuff.content.constraint.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointAndId;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.util.GetterUtils;
import yay.evy.everest.vstuff.util.RopeStyles;

import static yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils.*;

public class PulleyRope extends AbstractRope {

    public PulleyRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1, RopeStyles.RopeStyle style) {
        super(level, ropeId, ship0, ship1, blockPos0, blockPos1, style);
        this.type = RopeType.PULLEY;
    }

    public PulleyRope(Integer ropeId, Long ship0, Long ship1, boolean ship0IsGround, boolean ship1IsGround,
                      Vector3d localPos0, Vector3d localPos1, float minLength, float maxLength, float maxForce,
                      float maxTorque, float tolerance, float stiffness, float damping, BlockPos blockPos0,
                      BlockPos blockPos1, RopeStyles.RopeStyle style, RopeUtils.RopeType type) {
        super(ropeId, ship0, ship1, ship0IsGround, ship1IsGround, localPos0, localPos1,
                minLength, maxLength, maxForce, maxTorque, tolerance, stiffness, damping, blockPos0, blockPos1, style, type);
    }

    public static PulleyRope create(ServerLevel level, Player player, BlockPos firstPos, BlockPos secondPos, Long firstShip, Long secondShip) {
        return new PulleyRope(level, RopeUtils.createTempId(), firstShip, secondShip, firstPos, secondPos, RopeStyleHandlerServer.getStyle(player.getUUID()));
    }


    @Override
    public boolean createJoint(ServerLevel level) {
        Long currentGroundBodyId;

        try {
            currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
        } catch (Exception e) {
            VStuff.LOGGER.warn("Exception occurred while trying to get ground body id: {}", e.getMessage());
            return false;
        }

        Long actualShipA = ship0IsGround ? currentGroundBodyId : ship0;
        Long actualShipB = ship1IsGround ? currentGroundBodyId : ship1;

        try {
            GameToPhysicsAdapter gtpa = GetterUtils.getGTPA(level);

            VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

            boolean shipAValid = ship0IsGround ||
                    (actualShipA != null && shipWorld.getAllShips().getById(actualShipA) != null);
            boolean shipBValid = ship1IsGround ||
                    (actualShipB != null && shipWorld.getAllShips().getById(actualShipB) != null);

            if (!shipAValid || !shipBValid) return false;

            VSJoint ropeConstraint = makeDistanceJoint();

            gtpa.addJoint(ropeConstraint, 0, newConstraintId -> this.ID = newConstraintId);

            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error restoring joint for constraint {}: {}", ID, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean removeJoint(ServerLevel level) {
        if (constraint == null) {
            VStuff.LOGGER.warn("Cannot remove an already null constraint");
            return false;
        }

        try {
            GameToPhysicsAdapter gtpa = GetterUtils.getGTPA(level);

            gtpa.removeJoint(ID);

            this.constraint = null;
            this.ID = RopeUtils.createTempId();
            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error removing joint for id {}: {}", ID, e.getMessage());
            return false;
        }
    }

    public boolean setJointLength(ServerLevel level, float newlength) {
        if (constraint == null) {
            VStuff.LOGGER.warn("Cannot set the length of a null joint!");
            return false;
        }

        try {
            GameToPhysicsAdapter gtpa = GetterUtils.getGTPA(level);
            VSDistanceJoint newJoint = constraint.copy(
                    constraint.getShipId0(),
                    constraint.getPose0(),
                    constraint.getShipId1(),
                    constraint.getPose1(),
                    constraint.getMaxForceTorque(),
                    constraint.getMinDistance(),
                    newlength,
                    constraint.getTolerance(),
                    constraint.getStiffness(),
                    constraint.getDamping()
            );

            gtpa.updateJoint(new VSJointAndId(ID, newJoint));
            constraint = newJoint;
            maxLength = newlength;
            VStuff.LOGGER.info("Set joint id {} to length {}", ID, newlength);
            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error setting length of joint {}: {}", ID, e.getMessage());
            return false;
        }
    }

    public boolean shiftJointLength(ServerLevel level, float shift) {
        if (constraint == null) {
            VStuff.LOGGER.warn("Cannot change the length of a null joint!");
            return false;
        }

        float newLength = constraint.getMaxDistance() + shift;
        return setJointLength(level, newLength);
    }


    @Override
    public CompoundTag toTag() {
        return super.toTag();
    }

    @Override
    public void addToBuf(FriendlyByteBuf buf) {
        super.addToBuf(buf);
    }
    public static PulleyRope fromTag(CompoundTag tag) {
        return new PulleyRope(
                tag.getInt("id"),
                tag.getLong("ship0"),
                tag.getLong("ship1"),
                tag.getBoolean("ship0IsGround"),
                tag.getBoolean("ship1IsGround"),

                getVector3d("localPos0", tag),
                getVector3d("localPos1", tag),

                tag.getFloat("minLength"),
                tag.getFloat("maxLength"),

                tag.getFloat("maxForce"),
                tag.getFloat("maxTorque"),

                tag.getFloat("tolerance"),
                tag.getFloat("stiffness"),
                tag.getFloat("damping"),

                getBlockPos("blockPos0", tag),
                getBlockPos("blockPos1", tag),

                RopeStyles.fromString(tag.getString("style")),

                RopeUtils.RopeType.valueOf(tag.getString("type"))
        );
    }

    public static PulleyRope fromBuf(FriendlyByteBuf buf) {
        return new PulleyRope(
                buf.readInt(),
                buf.readLong(),
                buf.readLong(),
                buf.readBoolean(),
                buf.readBoolean(),
                v3fToV3d(buf.readVector3f()),
                v3fToV3d(buf.readVector3f()),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBlockPos(),
                buf.readBlockPos(),
                RopeStyles.fromString(buf.readUtf()),
                buf.readEnum(RopeUtils.RopeType.class)
        );
    }

    @Override
    public String toString() {
        return "PulleyRope {" +
                "ID=" + ID +
                ", ship0=" + ship0 +
                ", ship1=" + ship1 +
                ", ship0IsGround=" + ship0IsGround +
                ", ship1IsGround=" + ship1IsGround +
                ", localPos0=" + localPos0 +
                ", localPos1=" + localPos1 +
                ", minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", maxForce=" + maxForce +
                ", maxTorque=" + maxTorque +
                ", tolerance=" + tolerance +
                ", stiffness=" + stiffness +
                ", damping=" + damping +
                ", blockPos0=" + blockPos0 +
                ", blockPos1=" + blockPos1 +
                ", style=" + style +
                ", type=" + type +
                ", constraint=" + constraint +
                '}';
    }
}
