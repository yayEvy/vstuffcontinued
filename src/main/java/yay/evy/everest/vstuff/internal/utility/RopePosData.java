package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.utility.RopeUtils.PosType;
import yay.evy.everest.vstuff.internal.utility.RopeUtils.BlockType;
import org.joml.Vector3d;

import javax.annotation.Nullable;

import static yay.evy.everest.vstuff.internal.utility.PosUtils.getBlockType;
import static yay.evy.everest.vstuff.internal.utility.RopeUtils.getLocalPos;

public record RopePosData(@Nullable Long shipId, BlockPos blockPos, Vector3d localPos, RopeUtils.PosType posType, RopeUtils.BlockType blockType) {
    public static RopePosData create(ServerLevel level, Long id, BlockPos pos) {
        if (ShipUtils.getGroundBodyId(level).equals(id)) {
            VStuff.LOGGER.warn("RopePosData received actual id for ground body identifier instead of null (expected value), correcting");
            id = null;
        }
        PosType posType = id == null ? PosType.WORLD : PosType.SHIP;
        Vector3d localPos = getLocalPos(level, pos);

        return new RopePosData(id, pos, localPos, posType, getBlockType(level, pos));
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
        return "RopePosData with shipId " + shipId + ", blockPos " + blockPos + ", localPos " + localPos + ", posType " + posType + ", blockType " + blockType;
    }
}