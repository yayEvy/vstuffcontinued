package yay.evy.everest.vstuff.content.constraintrework.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

public class WorldToWorldRope extends AbstractRope {

    public RopeUtils.RopeType type = RopeUtils.RopeType.WORLDTOWORLD;

    public WorldToWorldRope(ServerLevel level, Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1) {
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

    public static WorldToWorldRope fromTag(CompoundTag tag) {

    }
}
