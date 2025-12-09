package yay.evy.everest.vstuff.content.constraintrework.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.util.RopeStyles;

public class PulleyRope extends AbstractRope {

    public RopeUtils.RopeType type = RopeUtils.RopeType.PULLEY;

    public PulleyRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1) {
        super(level, ropeId, ship0, ship1, localPos0, localPos1);

    }

    public PulleyRope(Integer ropeId, Long ship0, Long ship1, boolean ship0IsGround, boolean ship1IsGround,
                      Vector3d localPos0, Vector3d localPos1, Vector3d worldPos0, Vector3d worldPos1,
                      float minLength, float maxLength, float maxForce, float maxTorque, float tolerance,
                      float stiffness, float damping, BlockPos blockPos0, BlockPos blockPos1,
                      RopeStyles.RopeStyle style, RopeUtils.RopeType type) {
        super(ropeId, ship0, ship1, ship0IsGround, ship1IsGround, localPos0, localPos1, worldPos0, worldPos1,
                minLength, maxLength, maxForce, maxTorque, tolerance, stiffness, damping, blockPos0, blockPos1, style, type);
    }

    @Override
    public boolean createJoint(ServerLevel level) {
        return false;
    }

    @Override
    public boolean editJoint(ServerLevel level) {
        return false;
    }

    @Override
    public boolean removeJoint(ServerLevel level) {
        return false;
    }



    @Override
    public CompoundTag toTag() {
        return super.toTag();
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
                getVector3d("worldPos0", tag),
                getVector3d("worldPos1", tag),

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
}
