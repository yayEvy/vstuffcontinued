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

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putLong("shipId", shipId == null ? -1 : shipId); // use ground body id for tag
        tag.put("blockPos", NbtUtils.writeBlockPos(blockPos));
        tag.put("localPos", writeVector3d(localPos));
        tag.putString("posType", posType.name());
        tag.putString("blockType", blockType.name());

        return tag;
    }

    public static RopePosData fromTag(CompoundTag tag) {
        Long shipId = tag.getLong("shipId");
        shipId = shipId == -1 ? null : shipId;
        BlockPos blockPos = NbtUtils.readBlockPos(tag.getCompound("blockPos"));
        Vector3d localPos = readVector3d(tag.getCompound("localPos"));
        PosType posType = PosType.valueOf(tag.getString("posType"));
        BlockType blockType = BlockType.valueOf(tag.getString("blockType"));

        return new RopePosData(shipId, blockPos, localPos, posType, blockType);
    }

    public static CompoundTag writeVector3d(Vector3d vector3d) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("X", vector3d.x);
        tag.putDouble("Y", vector3d.y);
        tag.putDouble("Z", vector3d.z);
        return tag;
    }

    public static Vector3d readVector3d(CompoundTag tag) {
        return new Vector3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
    }

    @Override
    public @NotNull String toString() {
        return "RopePosData with shipId " + shipId + ", blockPos " + blockPos + ", localPos " + localPos + ", posType " + posType + ", blockType " + blockType;
    }
}