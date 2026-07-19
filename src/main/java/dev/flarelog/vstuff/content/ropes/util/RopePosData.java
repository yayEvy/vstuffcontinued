package dev.flarelog.vstuff.content.ropes.util;

import dev.flarelog.vstuff.content.physics.VSUtil;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import dev.flarelog.vstuff.VStuff;
import org.joml.Vector3d;

import javax.annotation.Nullable;

import static dev.flarelog.vstuff.content.ropes.util.RopeUtil.getLocalPos;

public record RopePosData(@Nullable Long shipId, BlockPos blockPos, Vector3d localPos, boolean isWorld) {
    public static RopePosData create(ServerLevel level, Long id, BlockPos pos) {
        if (VSUtil.getGroundBodyId(level).equals(id)) {
            VStuff.LOGGER.warn("RopePosData received actual id for ground body identifier instead of null (expected value), correcting");
            id = null;
        }
        Vector3d localPos = getLocalPos(level, pos);

        return new RopePosData(id, pos, localPos, id == null);
    }

    public static Pair<RopePosData, RopePosData> create(ServerLevel level, Long ship0, Long ship1, BlockPos blockPos0, BlockPos blockPos1) {
        RopePosData first = RopePosData.create(level, ship0, blockPos0);
        RopePosData second = RopePosData.create(level, ship1, blockPos1);

        if (!first.isWorld && second.isWorld) return new Pair<>(second, first);
        return new Pair<>(first, second);
    }

    public boolean sameShip(RopePosData other) {
        if (this.isWorld && other.isWorld) return true;
        if (this.isWorld != other.isWorld) return false;
        return this.shipId.equals(other.shipId);
    }

    public @NotNull Long getShipIdSafe(ServerLevel level) {
        return shipId == null ? VSUtil.getGroundBodyId(level) : shipId;
    }

    public Vector3d getWorldPos(ServerLevel level) {
        return RopeUtil.getWorldPos(level, blockPos, shipId);
    }
}