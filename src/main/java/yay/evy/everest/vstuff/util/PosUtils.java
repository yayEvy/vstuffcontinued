package yay.evy.everest.vstuff.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.content.roperework.NewRopeUtils;

public class PosUtils {

    public static Vector3f getLocalPos(BlockPos pos) {
        return new Vector3f(pos.getX() - 0.5f, pos.getY() - 0.5f, pos.getZ() - 0.5f);
    }

    public static Vector3f getWorldPosition(ServerLevel level, BlockPos pos, Long shipId) {
        Vector3f localPos = new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3f worldPos = new Vector3f();
                shipObject.getTransform().getShipToWorld().transformPosition(new Vector3d(localPos), new Vector3d(worldPos));
                return worldPos;
            }
        }
        return localPos;
    }

    public static Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public static boolean matchesBlockType(ServerLevel level, BlockPos pos, NewRopeUtils.BlockType posType) {
        return switch (posType) {
            case PULLEY -> isPhysPulley(level, pos);
            case PULLEY_ANCHOR -> isPulleyAnchor(level, pos);
            case NORMAL -> true; // any other conditions would have triggered so we know it's a normal block
        };
    }

    public static NewRopeUtils.BlockType getBlockType(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PhysPulleyBlockEntity) return NewRopeUtils.BlockType.PULLEY;
        if (be instanceof PulleyAnchorBlockEntity) return NewRopeUtils.BlockType.PULLEY_ANCHOR;
        return NewRopeUtils.BlockType.NORMAL;
    }

    public static boolean isPhysPulley(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PhysPulleyBlockEntity;
    }

    public static boolean isPulleyAnchor(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PulleyAnchorBlockEntity;
    }
}
