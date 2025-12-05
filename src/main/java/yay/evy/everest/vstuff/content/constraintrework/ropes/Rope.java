package yay.evy.everest.vstuff.content.constraintrework.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

public class Rope extends AbstractRope {

    public RopeUtils.RopeType type = RopeUtils.RopeType.NORMAL;

    public Rope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1) {
        super(level, ropeId, ship0, ship1, localPos0, localPos1);
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

    public static Rope fromTag(CompoundTag tag) {

        Integer id = tag.getInt("id");
        Long ship0 = tag.getLong("ship0");
        tag.getLong("ship1");
        tag.getBoolean("ship0IsGround");
        tag.getBoolean("ship1IsGround");

        getVector3d("localPos0", tag);
        getVector3d("localPos1", tag);
        getVector3d("worldPos0", tag);
        getVector3d("worldPos1", tag);

        tag.getFloat("minLength");
        tag.getFloat("maxLength");

        tag.getFloat("maxForce");
        tag.getFloat("maxTorque");

        tag.getFloat("tolerance");
        tag.getFloat("stiffness");
        tag.getFloat("damping");

        getBlockPos("blockPos0", tag);
        getBlockPos("blockPos1", tag);

        tag.getString("type");

        tag.getString("style");
        tag.getString("primitiveStyle");
        tag.getString("styleLKey");

        return new Rope(

        )
    }

}
