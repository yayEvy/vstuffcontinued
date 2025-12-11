package yay.evy.everest.vstuff.content.constraintrework.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.util.RopeStyles;

import static yay.evy.everest.vstuff.content.constraintrework.ropes.RopeUtils.*;

public class JointlessRope extends AbstractRope {

    public RopeUtils.RopeType type = RopeUtils.RopeType.WORLDTOWORLD;

    public JointlessRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1) {
        super(level, ropeId, ship0, ship1, RopeUtils.getLocalPosition(blockPos0), RopeUtils.getLocalPosition(blockPos1));
        this.blockPos0 = blockPos0;
        this.blockPos1 = blockPos1;
    }

    public JointlessRope(Integer ropeId, Long ship0, Long ship1, boolean ship0IsGround, boolean ship1IsGround,
                         Vector3d localPos0, Vector3d localPos1, float minLength, float maxLength, float maxForce,
                         float maxTorque, float tolerance,
                         float stiffness, float damping, BlockPos blockPos0, BlockPos blockPos1,
                         RopeStyles.RopeStyle style, RopeUtils.RopeType type) {
        super(ropeId, ship0, ship1, ship0IsGround, ship1IsGround, localPos0, localPos1,
                minLength, maxLength, maxForce, maxTorque, tolerance, stiffness, damping, blockPos0, blockPos1, style, type);
    }

    public static JointlessRope create(ServerLevel level, Player player, BlockPos firstPos, BlockPos secondPos, Long firstShip, Long secondShip) {
        return new JointlessRope(level, -1, firstShip, secondShip, firstPos, secondPos); // -1 is a temp id
    }
    @Override
    public boolean createJoint(ServerLevel level) {
        VStuff.LOGGER.info("Not creating joint for a jointless rope");
        return true;
    }

    @Override
    public boolean editJoint(ServerLevel level) {
        VStuff.LOGGER.info("Not editing joint for a jointless rope");
        return true;
    }

    @Override
    public boolean removeJoint(ServerLevel level) {
        VStuff.LOGGER.info("Not removing joint for a jointless rope");
        return true;
    }

    @Override
    public CompoundTag toTag() {
        return super.toTag();
    }

    @Override
    public void addToBuf(FriendlyByteBuf buf) {
        super.addToBuf(buf);
    }

    public static JointlessRope fromTag(CompoundTag tag) {
        return new JointlessRope(
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

    public static JointlessRope fromBuf(FriendlyByteBuf buf) {
        return new JointlessRope(
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
        return "JointlessRope {" +
                "ID=" + ID +
                ", ship0=" + ship0 +
                ", ship1=" + ship1 +
                ", ship0IsGround=" + ship0IsGround +
                ", ship1IsGround=" + ship1IsGround +
                ", localPos0=" + localPos0 +
                ", localPos1=" + localPos1 +
                ", minLength=" + minLength +
                ", maxLength=" + maxLength +
                ", blockPos0=" + blockPos0 +
                ", blockPos1=" + blockPos1 +
                ", style=" + style +
                ", type=" + type +
                ", type=" + type +
                '}';
    }
}
