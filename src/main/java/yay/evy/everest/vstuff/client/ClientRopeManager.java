package yay.evy.everest.vstuff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.constraintrework.ropes.AbstractRope;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientRopes = new HashMap<>();

    public record ClientRopeData(Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1,
                                  double maxLength, RopeStyles.RopeStyle style) {
            public ClientRopeData(Integer ropeId, Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1,
                                  double maxLength, RopeStyles.RopeStyle style) {
                this.ropeId = ropeId;
                this.ship0 = ship0;
                this.ship1 = ship1;
                this.localPos0 = new Vector3d(localPos0);
                this.localPos1 = new Vector3d(localPos1);
                this.maxLength = maxLength;
                this.style = style;
            }

            public ClientRopeData(AbstractRope rope) {
                this(rope.ID, rope.ship0, rope.ship1, rope.localPos0, rope.localPos1, rope.maxLength, rope.style);
            }
        }

    public static void addClientRope(AbstractRope rope) {
        clientRopes.put(rope.ID, new ClientRopeData(rope));
    }

    public static void removeClientRope(Integer ropeId) {
        clientRopes.remove(ropeId);
    }

    public static Map<Integer, ClientRopeData> getClientRopes() {
        return new HashMap<>(clientRopes);
    }

    public static void clearAllClientRopes() {
        clientRopes.clear();
    }
}