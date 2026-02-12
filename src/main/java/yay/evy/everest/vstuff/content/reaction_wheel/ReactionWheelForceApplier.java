package yay.evy.everest.vstuff.content.reaction_wheel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joml.Vector3d;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;


import net.minecraft.core.BlockPos;
import yay.evy.everest.vstuff.VStuffConfig;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public class ReactionWheelForceApplier {
    private ReactionWheelData data;
    @JsonIgnore
    private static final double TORQUE_STRENGTH = 50000.0;
    @JsonIgnore
    private static final double MAX_SPEED = 256.0;

    public ReactionWheelForceApplier(ReactionWheelData data) {
        this.data = data;
    }

    public ReactionWheelForceApplier() {}

    public void applyForces(BlockPos pos, PhysShipImpl ship) {
        if (data.rotationSpeed == 0 || data.facing == null) return;

        if (data.mode == ReactionWheelData.ReactionWheelMode.DIRECT) {
            applyDirectTorque(ship);
        } else {
            applyStabilizationTorque(ship);
        }
    }

    private void applyDirectTorque(PhysShipImpl ship) {

        double maxSpeed = VStuffConfig.REACTION_WHEEL_MAX_SPEED.get();
        double torqueStrength = VStuffConfig.REACTION_WHEEL_TORQUE_STRENGTH.get();

        if (maxSpeed <= 0) return;

        double torqueMagnitude = (data.rotationSpeed / maxSpeed) * torqueStrength;

        Vector3d worldTorque = new Vector3d(
                data.facing.x(),
                data.facing.y(),
                data.facing.z()
        ).mul(torqueMagnitude);

        Vector3d shipTorque =
                ship.getTransform().getWorldToShip()
                        .transformDirection(worldTorque, new Vector3d());

        ship.applyInvariantTorque(shipTorque);
    }


    private void applyStabilizationTorque(PhysShipImpl ship) {

    }
}
