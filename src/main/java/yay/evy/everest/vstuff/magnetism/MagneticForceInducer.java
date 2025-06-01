package yay.evy.everest.vstuff.magnetism;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MagneticForceInducer implements ShipForcesInducer {
    private final ConcurrentLinkedQueue<Vector3d> forces = new ConcurrentLinkedQueue<>();
    private Vector3d lastPosition = null;
    private long lastApplyForcesCall = 0;
    private int totalForceApplications = 0;

    public void addForce(Vector3d force) {
        forces.add(new Vector3d(force));
        long currentTime = System.currentTimeMillis();
        long timeSinceLastApply = currentTime - lastApplyForcesCall;
        System.out.println("Force added to queue. Queue size: " + forces.size() +
                ", Time since last applyForces: " + timeSinceLastApply + "ms" +
                ", Total applications: " + totalForceApplications);
    }

    @JsonIgnore
    public int getQueueSize() {
        return forces.size();
    }

    @JsonIgnore
    public void clearQueue() {
        forces.clear();
        System.out.println("Cleared force queue");
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lastApplyForcesCall = System.currentTimeMillis();
        totalForceApplications++;

        System.out.println("=== applyForces called for ship " + physShip.getId() +
                " (call #" + totalForceApplications + ") ===");

        Vector3d netForce = new Vector3d();
        int forceCount = 0;

        // Process all queued forces
        while (!forces.isEmpty()) {
            Vector3d force = forces.poll();
            if (force != null) {
                netForce.add(force);
                forceCount++;
            }
        }

        if (forceCount > 0 && !netForce.equals(new Vector3d(0, 0, 0))) {
            Vector3d currentPos = new Vector3d(physShip.getTransform().getPositionInWorld());
            Vector3d amplifiedForce = new Vector3d(netForce).mul(1.3);

            if (amplifiedForce.length() > 1000.0) {
                physShip.applyInvariantForce(amplifiedForce);
                System.out.println("Applied " + forceCount + " forces totaling: " + amplifiedForce +
                        " (magnitude: " + amplifiedForce.length() + ") to ship " + physShip.getId());
            } else {
                System.out.println("Force too small, not applying: " + amplifiedForce.length());
            }

            lastPosition = new Vector3d(currentPos);
        } else if (forceCount > 0) {
            System.out.println("Had " + forceCount + " forces but net force was zero");
        } else {
            System.out.println("No forces to apply for ship " + physShip.getId());
        }
    }

    public static MagneticForceInducer getOrCreate(ServerShip ship) {
        MagneticForceInducer attachment = ship.getAttachment(MagneticForceInducer.class);
        if (attachment == null) {
            attachment = new MagneticForceInducer();
            ship.saveAttachment(MagneticForceInducer.class, attachment);
            System.out.println("Created new MagneticForceInducer attachment for ship " + ship.getId());
        } else {
            System.out.println("Retrieved existing MagneticForceInducer for ship " + ship.getId() +
                    " (total applications: " + attachment.totalForceApplications + ")");
        }
        return attachment;
    }
}
