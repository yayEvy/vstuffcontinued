package yay.evy.everest.vstuff.internal.utility;

import kotlin.Pair;
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

public record RopePosData(@Nullable Long shipId, BlockPos blockPos, Vector3d localPos, boolean isWorld, RopeUtils.SelectType selectType) {
    public static RopePosData create(ServerLevel level, Long id, BlockPos pos) {
        if (ShipUtils.getGroundBodyId(level).equals(id)) {
            VStuff.LOGGER.warn("RopePosData received actual id for ground body identifier instead of null (expected value), correcting");
            id = null;
        }
        Vector3d localPos = getLocalPos(level, pos);
        RopeUtils.SelectType selectType = getSelectType(level, pos);

        return new RopePosData(id, pos, localPos, id == null, selectType);
    }

    public static Pair<RopePosData, RopePosData> create(ServerLevel level, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1) {
        RopePosData first = RopePosData.create(level, ship0, blockPos0);
        RopePosData second = RopePosData.create(level, ship1, blockPos1);

        if (!first.isWorld && second.isWorld) {
            return new Pair<>(second, first);
        } else {
            return new Pair<>(first, second);
        }
    }

    public void attach(ServerLevel level, Integer ropeId) {
        //System.out.println("attaching for " + ropeId);
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

    public boolean sameShip(RopePosData other) {
        if (this.isWorld && other.isWorld) return true;
        if (this.isWorld != other.isWorld) return false;
        return this.shipId.equals(other.shipId);
    }

    public @NotNull Long getShipIdSafe(ServerLevel level) {
        return shipId == null ? ShipUtils.getGroundBodyId(level) : shipId;
    }

    public Vector3d getWorldPos(ServerLevel level) {
        return RopeUtils.getWorldPos(level, blockPos, shipId);
    }

    @Override
    public @NotNull String toString() {
        return "RopePosData with shipId " + shipId + ", blockPos " + blockPos + ", localPos " + localPos + ", selectType " + selectType;
    }
}