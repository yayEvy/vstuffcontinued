package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ShipUtils {

    public static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    public static Long whythefuckisitsupposedtobenullwhyyyyy(Long shipId, ServerLevel level) {
        if (shipId == null) return null;
        if (shipId.equals(getGroundBodyId(level))) return null;
        return shipId;
    }

    public static Long getShipIdAtPos(Level level, BlockPos pos) {
        Ship loadedShip = VSGameUtilsKt.getShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public static Long getLoadedShipIdAtPos(Level level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : null;
    }

    public static @NotNull Long getSafeLoadedShipIdAtPos(ServerLevel level, BlockPos pos) {
        LoadedShip loadedShip = VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
        return loadedShip != null ? loadedShip.getId() : getGroundBodyId(level);
    }

    public static @NotNull Long getSafeShipIdAtPos(ServerLevel level, BlockPos pos) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
        return ship != null ? ship.getId() : getGroundBodyId(level);
    }

    public static LoadedShip getLoadedShipAtPos(ServerLevel level, BlockPos pos) {
        return VSGameUtilsKt.getLoadedShipManagingPos(level, pos);
    }

    public static Ship getShipAtPos(Level level, BlockPos pos) {
        return VSGameUtilsKt.getShipManagingPos(level, pos);
    }

    public static Double getMassForShip(ServerLevel level, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId == null || shipId.equals(groundBodyId)) {
            return 1e12;
        }

        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
        if (shipObject != null) {
            try {
                double mass = 1000.0;
                var bounds = shipObject.getShipAABB();
                if (bounds != null) {
                    double volume = (bounds.maxX() - bounds.minX()) *
                            (bounds.maxY() - bounds.minY()) *
                            (bounds.maxZ() - bounds.minZ());
                    mass = Math.max(volume * 10.0, 1000.0);
                }
                return Math.min(mass, 1e9);
            } catch (Exception e) {
                return 1000.0;
            }
        }
        return 1000.0;
    }

}
