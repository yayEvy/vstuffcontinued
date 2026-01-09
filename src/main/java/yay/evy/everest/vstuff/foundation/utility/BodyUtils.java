package yay.evy.everest.vstuff.foundation.utility;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class BodyUtils {

    public static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
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



    public static Long whythefuckisitsupposedtobenullwhyyyyy(Long shipId, ServerLevel level) {
        if (shipId == null) return null;
        if (shipId.equals(getGroundBodyId(level))) return null;
        return shipId;
    }
}
