package yay.evy.everest.vstuff.foundation.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.rope.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.content.rope.roperework.RopeUtil;
import yay.evy.everest.vstuff.index.VStuffBlocks;

import static yay.evy.everest.vstuff.foundation.utility.BodyUtils.getGroundBodyId;

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

    public static Long getLoadedShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public static @NotNull Long getSafeLoadedShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : getGroundBodyId(level);
    }

    public static  LoadedShip getLoadedShipAtPos(ServerLevel level, BlockPos pos) {
        return VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
    }

    public static @Nullable Long getShipIdAtPos(Level level, BlockPos pos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
        return ship != null ? ship.getId() : null;
    }

    public static Ship getShipAtPos(Level level, BlockPos pos) {
        return VSGameUtilsKt.getShipManagingPos(level, pos);
    }

    public static boolean isCompatibleWithType(ServerLevel level, BlockPos pos, RopeUtil.SelectType type) {
        return switch (type) {
            case NORMAL -> !(isPhysPulley(level, pos) || isPulleyAnchor(level, pos));
            case PULLEY -> isPulleyAnchor(level, pos);
        };
    }

    public static RopeUtil.BlockType getBlockType(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PhysPulleyBlockEntity) return RopeUtil.BlockType.PULLEY;
        if (be instanceof PulleyAnchorBlockEntity) return RopeUtil.BlockType.PULLEY_ANCHOR;
        return RopeUtil.BlockType.NORMAL;
    }

    public static boolean isPhysPulley(BlockState state) {
        return state.getBlock().equals(VStuffBlocks.PHYS_PULLEY.get());
    }

    public static boolean isPulleyAnchor(BlockState state) {
        return state.getBlock().equals(VStuffBlocks.PULLEY_ANCHOR.get());
    }

    public static boolean isPhysPulley(Level level, BlockPos pos) {
        return isPhysPulley(level.getBlockState(pos));
    }

    public static boolean isPulleyAnchor(Level level, BlockPos pos) {
        return isPulleyAnchor(level.getBlockState(pos));
    }
}
