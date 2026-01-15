package yay.evy.everest.vstuff.client;

import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ClientRopeUtil {

    public static Vector3f renderLocalToWorld(Level level, Vector3f localPos, Long shipId) {
        if (shipId == null || level == null) return localPos;

        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        ClientShip clientShip = (ClientShip) shipWorld.getAllShips().getById(shipId);
        if (clientShip == null) return localPos;
        Vector3d transformedPos = clientShip.getRenderTransform().getShipToWorld().transformPosition(new Vector3d(localPos), new Vector3d());
        return new Vector3f((float) transformedPos.x, (float) transformedPos.y, (float) transformedPos.z);
    }
}
