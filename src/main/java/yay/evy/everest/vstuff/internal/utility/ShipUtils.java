package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShipUtils {

    public static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
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

    public static Float getMassForShip(ServerLevel level, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId == null || shipId.equals(groundBodyId)) {
            return 1e12f;
        }

        Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
        if (shipObject != null) {
            try {
                float mass = 1000.0f;
                var bounds = shipObject.getShipAABB();
                if (bounds != null) {
                    float volume = (bounds.maxX() - bounds.minX()) *
                            (bounds.maxY() - bounds.minY()) *
                            (bounds.maxZ() - bounds.minZ());
                    mass = Math.max(volume * 10.0f, 1000.0f);
                }
                return Math.min(mass, 1e9f);
            } catch (Exception e) {
                return 1000.0f;
            }
        }
        return 1000.0f;
    }
}
