package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.IRopeActor;
import yay.evy.everest.vstuff.internal.utility.RopeUtils.PosType;
import org.joml.Vector3d;

import javax.annotation.Nullable;

import static yay.evy.everest.vstuff.internal.utility.RopeUtils.getLocalPos;
import static yay.evy.everest.vstuff.internal.utility.RopeUtils.getSelectType;

public record RopePosData(@Nullable Long shipId, BlockPos blockPos, Vector3d localPos, RopeUtils.PosType posType, RopeUtils.SelectType selectType) {
    public static RopePosData create(ServerLevel level, Long id, BlockPos pos) {
        if (ShipUtils.getGroundBodyId(level).equals(id)) {
            VStuff.LOGGER.warn("RopePosData received actual id for ground body identifier instead of null (expected value), correcting");
            id = null;
        }
        PosType posType = id == null ? PosType.WORLD : PosType.SHIP;
        Vector3d localPos = getLocalPos(level, pos);
        RopeUtils.SelectType selectType = getSelectType(level, pos);

        return new RopePosData(id, pos, localPos, posType, selectType);
    }

    public void attach(ServerLevel level, Integer ropeId) {
        if (selectType == RopeUtils.SelectType.ACTOR) {
            IRopeActor actor = (IRopeActor) level.getBlockEntity(blockPos);
            if (actor == null) return;
            actor.connectRope(ropeId, actor.getActorBlockState(), level, blockPos);
        }
    }

    public void remove(ServerLevel level, Integer ropeId) {
        if (selectType == RopeUtils.SelectType.ACTOR) {
            IRopeActor actor = (IRopeActor) level.getBlockEntity(blockPos);
            if (actor == null) return;
            actor.removeRope(ropeId, actor.getActorBlockState(), level, blockPos);
        }
    }

    public boolean isWorld() {
        return this.posType == PosType.WORLD;
    }

    public @NotNull Long getShipIdSafe(ServerLevel level) {
        return shipId == null ? ShipUtils.getGroundBodyId(level) : shipId;
    }

    public Vector3d getWorldPos(ServerLevel level) {
        return RopeUtils.getWorldPos(level, blockPos, shipId);
    }

    @Override
    public @NotNull String toString() {
        return "RopePosData with shipId " + shipId + ", blockPos " + blockPos + ", localPos " + localPos + ", posType " + posType + ", selectType " + selectType;
    }
}