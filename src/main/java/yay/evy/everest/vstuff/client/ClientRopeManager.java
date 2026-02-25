package yay.evy.everest.vstuff.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.Map;

public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientConstraints = new HashMap<>();
    private static ClientRopeData translucentRope = null;

    public record ClientRopeData(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength,
                                 ResourceLocation style) {
            public ClientRopeData(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, ResourceLocation style) {
                this.ship0 = ship0;
                this.ship1 = ship1;
                this.localPos0 = new Vector3d(localPos0);
                this.localPos1 = new Vector3d(localPos1);
                this.maxLength = maxLength;
                this.style = style;
            }

        public boolean isRenderable(Level level) {
            if (level == null) return false;

            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            if (shipWorld == null) return false;

            if (ship0 != null && ship0 != 0L) {
                Ship a = shipWorld.getAllShips().getById(ship0);
                if (!(a instanceof ClientShip csA)) return false;
                if (csA.getRenderTransform() == null) return false;
            }

            if (ship1 != null && ship1 != 0L) {
                Ship b = shipWorld.getAllShips().getById(ship1);
                if (!(b instanceof ClientShip csB)) return false;
                if (csB.getRenderTransform() == null) return false;
            }

            return true;
        }

    }

    public static boolean hasPreviewRope() {
        return !(translucentRope == null);
    }

    public static void setPreviewRope(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, float maxLength, ResourceLocation style) {
        translucentRope = new ClientRopeData(ship0, ship1, localPos0, localPos1, maxLength, style);
    }

    public static void clearPreviewRope() {
        translucentRope = null;
    }

    public static ClientRopeData getPreviewRope() {
        return translucentRope;
    }

    public static void addClientConstraint(Integer constraintId, Long shipA, Long shipB,
                                           Vector3d localPosA, Vector3d localPosB, double maxLength, ResourceLocation style) {
        clientConstraints.put(constraintId, new ClientRopeData(shipA, shipB, localPosA, localPosB, maxLength, style));
    }

    public static void removeClientConstraint(Integer constraintId) {
        if (constraintId == null) return;

        clientConstraints.remove(constraintId);
    }

    public static Map<Integer, ClientRopeData> getClientConstraints() {
        return clientConstraints;
    }

    public static void clearAllClientConstraints() {
        clientConstraints.clear();
        translucentRope = null;
    }

}