package yay.evy.everest.vstuff.magnetism;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Stores forces to apply to a ship during the physics tick.
 */
public class MagneticForceInducer implements ShipForcesInducer {
    private final ConcurrentLinkedQueue<Vector3d> forces = new ConcurrentLinkedQueue<>();
    private Vector3d lastPosition = null;

    public void addForce(Vector3d force) {
        forces.add(new Vector3d(force));
        System.out.println("Force added to queue. Queue size: " + forces.size());
    }

    @JsonIgnore
    public int getQueueSize() {
        return forces.size();
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        Vector3d netForce = new Vector3d();
        int forceCount = 0;

        while (!forces.isEmpty()) {
            netForce.add(forces.poll());
            forceCount++;
        }

        if (!netForce.equals(new Vector3d(0, 0, 0))) {
            Vector3d currentPos = new Vector3d(physShip.getTransform().getPositionInWorld());

            Vector3d amplifiedForce = new Vector3d(netForce).mul(1.5); // Multiply force by 1.5
            physShip.applyInvariantForce(amplifiedForce);

            System.out.println("Applied " + forceCount + " forces totaling: " + amplifiedForce + " to ship " + physShip.getId());
            System.out.println("Ship " + physShip.getId() + " position: " + currentPos);

            if (lastPosition != null) {
                double moved = currentPos.distance(lastPosition);
                System.out.println("Ship " + physShip.getId() + " moved " + moved + " units since last check");
            }
            lastPosition = new Vector3d(currentPos);
        }
    }
}
