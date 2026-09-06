package dev.flarelog.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

public class VSUtils {

    public static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    public static GameToPhysicsAdapter getGTPA(ServerLevel level) {
        String dimId = ValkyrienSkies.getDimensionId(level);
        return ValkyrienSkiesMod.getOrCreateGTPA(dimId);
    }

    public static Long getShipIdAtPos(Level level, BlockPos pos) {
        Ship loadedShip = VSGameUtilsKt.getShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public static Long getLoadedShipIdAtPos(Level level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public static LoadedServerShip getLoadedServerShipAtPos(ServerLevel level, BlockPos pos) {
        return VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
    }

    public static Vector3d getLocalPos(Level level, BlockPos pos) {
        Vector3d blockPos;
        try {
            VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
            Vec3 vec = shape.bounds().getCenter().add(pos.getCenter());
            blockPos = new Vector3d(vec.x - 0.5, vec.y - 0.5, vec.z - 0.5);
        } catch (UnsupportedOperationException ex) {
            blockPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        return blockPos;
    }

    public static Vector3d getWorldPos(Level level, BlockPos pos, Long shipId) {
        Vector3d localPos = getLocalPos(level, pos);
        if (shipId != null) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                return shipObject.getTransform().getShipToWorld().transformPosition(localPos, new Vector3d());
            }
        }
        return localPos;
    }

    public static Vector3d getWorldPos(Level level, BlockPos pos) {
        return getWorldPos(level, pos,  getLoadedShipIdAtPos(level, pos));
    }

    public static BlockPos containingBlockPos(Vector3d pos) {
        return BlockPos.containing(pos.x, pos.y, pos.z);
    }

    public static Vector3d convertLocalToWorld(Level level, Vector3d localPos, Long ship) {
        if (ship == null || level == null) return localPos;

        try {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(ship);
            if (shipObject != null) {
                Vector3d worldPos = new Vector3d();
                shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                return worldPos;
            }

            return new Vector3d(localPos);
        } catch (Exception e) {
            return new Vector3d(localPos);
        }
    }

}
