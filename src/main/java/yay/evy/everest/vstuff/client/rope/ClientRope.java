package yay.evy.everest.vstuff.client.rope;

import kotlin.Pair;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

@OnlyIn(Dist.CLIENT)
public class ClientRope {

    public final Long ship0;
    public final Long ship1;
    public final Vector3d localPos0;
    public final Vector3d localPos1;
    private double maxLength;
    private RegistryRopeStyle style;

    public ClientRope(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, RegistryRopeStyle style) {
        this.ship0 = ship0;
        this.ship1 = ship1;
        this.localPos0 = new Vector3d(localPos0);
        this.localPos1 = new Vector3d(localPos1);
        this.maxLength = maxLength;
        this.style = style;
    }

    public boolean canRender(Level level) {
        if (level == null) return false;

        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        boolean canRender = false;

        if (ship0 != null && ship0 != 0L) {
            Ship a = shipWorld.getAllShips().getById(ship0);
            canRender = a instanceof ClientShip;
        }

        if (ship1 != null && ship1 != 0L) {
            Ship b = shipWorld.getAllShips().getById(ship1);
            canRender = b instanceof ClientShip;
        }

        Pair<Vector3d, Vector3d> worldPositions = transformToWorld(level);

        return canRender;
    }

    public Pair<Vector3d, Vector3d> transformToWorld(Level level) {
        return new Pair<>(
                RopeUtils.renderLocalToWorld(level, localPos0, ship0),
                RopeUtils.renderLocalToWorld(level, localPos1, ship1)
        );
    }

    public double getLength() {
        return maxLength;
    }

    public RegistryRopeStyle getStyle() {
        return style;
    }

    public void setLength(double newLength) {
        this.maxLength = newLength;
    }

    public void setStyle(RegistryRopeStyle newStyle) {
        this.style = newStyle;
    }

}