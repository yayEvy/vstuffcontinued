package yay.evy.everest.vstuff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientConstraints = new HashMap<>();

    public record ClientRopeData(Long shipA, Long shipB, Vector3f localPosA, Vector3f localPosB, double maxLength,
                                 RopeStyles.RopeStyle style) {
            public ClientRopeData(Long shipA, Long shipB, Vector3f localPosA, Vector3f localPosB, double maxLength, RopeStyles.RopeStyle style) {
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

    public static void addClientConstraint(Integer constraintId, Long shipA, Long shipB,
                                           Vector3f localPosA, Vector3f localPosB, double maxLength, RopeStyles.RopeStyle style) {
        clientConstraints.put(constraintId, new ClientRopeData(shipA, shipB, localPosA, localPosB, maxLength, style));
    }

    public static void removeClientConstraint(Integer constraintId) {
        if (constraintId == null) return;

        clientConstraints.remove(constraintId);


        RopeRendererClient.positionCache.remove(constraintId);
    }

    public static Map<Integer, ClientRopeData> getClientConstraints() {
        return clientConstraints;
    }

    public static void clearAllClientConstraints() {
        clientConstraints.clear();
        RopeRendererClient.clearCache();
    }

}