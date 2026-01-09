package yay.evy.everest.vstuff.client;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.foundation.RopeStyles;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientRopes = new HashMap<>();
    private static ClientRopeData translucentRope = null;

    public record ClientRopeData(Long shipA, Long shipB, Vector3f localPosA, Vector3f localPosB, float maxLength,
                                 RopeStyles.RopeStyle style) {
            public ClientRopeData(Long shipA, Long shipB, Vector3f localPosA, Vector3f localPosB, float maxLength, RopeStyles.RopeStyle style) {
                this.shipA = shipA;
                this.shipB = shipB;
                this.localPosA = new Vector3f(localPosA);
                this.localPosB = new Vector3f(localPosB);
                this.maxLength = maxLength;
                this.style = style;
            }
        public boolean isRenderable(Level level) {
            if (level == null) return false;

            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            if (shipWorld == null) return false;

            if (shipA != null && shipA != 0L) {
                Ship a = shipWorld.getAllShips().getById(shipA);
                if (!(a instanceof ClientShip csA)) return false;
                if (csA.getRenderTransform() == null) return false;
            }

            if (shipB != null && shipB != 0L) {
                Ship b = shipWorld.getAllShips().getById(shipB);
                if (!(b instanceof ClientShip csB)) return false;
                if (csB.getRenderTransform() == null) return false;
            }

            return true;
        }

    }

    public static boolean hasPreviewRope() {
        return !(translucentRope == null);
    }

    public static void setPreviewRope(Long ship0, Long ship1, Vector3f localPos0, Vector3f localPos1, float maxLength, RopeStyles.RopeStyle style) {
        translucentRope = new ClientRopeData(ship0, ship1, localPos0, localPos1, maxLength, style);
    }

    public static void clearPreviewRope() {
        translucentRope = null;
    }

    public static void addClientRope(Integer constraintId, Long shipA, Long shipB,
                                     Vector3f localPosA, Vector3f localPosB, float maxLength, RopeStyles.RopeStyle style) {
        clientRopes.put(constraintId, new ClientRopeData(shipA, shipB, localPosA, localPosB, maxLength, style));
    }

    public static void removeClientRope(Integer constraintId) {
        if (constraintId == null) return;

        clientRopes.remove(constraintId);

        RopeRendererClient.positionCache.remove(constraintId);
    }

    public static Map<Integer, ClientRopeData> getClientRopes() {
        return clientRopes;
    }
    public static ClientRopeData getPreviewRope() {
        return translucentRope;
    }

    public static void clearAllClientConstraints() {
        clientRopes.clear();
        translucentRope = null;
        RopeRendererClient.clearCache();
    }

}