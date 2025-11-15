package yay.evy.everest.vstuff.content.physgrabber;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import yay.evy.everest.vstuff.content.thrust.AttachmentUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;


public class PhysGrabberServerAttachment implements ShipForcesInducer {

    private final Vector3d targetPos = new Vector3d();
    private boolean active = false;

    private static final double MAX_SPEED = 10.0;

    @Override
    public void applyForces(PhysShip physShip) {
        if (!active) return;

        PhysShipImpl ship = (PhysShipImpl) physShip;

        Vector3dc shipPos = ship.getTransform().getPositionInWorld();
        Vector3d toTarget = new Vector3d(targetPos).sub(shipPos);
        double distance = toTarget.length();
        if (distance < 0.01) return;

        var server = ValkyrienSkiesMod.getCurrentServer();
        if (server == null) return;
        var pipeline = VSGameUtilsKt.getVsPipeline(server);
        double physTps = pipeline.computePhysTps();
        if (physTps <= 0) return;
        double dt = 1.0 / physTps;

        Vector3d desiredVel = new Vector3d(toTarget).mul(1.0 / dt);
        if (desiredVel.length() > MAX_SPEED) desiredVel.normalize().mul(MAX_SPEED);

        Vector3d currentVel = new Vector3d(ship.getPoseVel().getVel());

        double smoothing = 0.2;
        Vector3d smoothedVel = new Vector3d(currentVel).lerp(desiredVel, smoothing);

        Vector3d velChange = new Vector3d(smoothedVel).sub(currentVel);

        double mass = ship.getInertia().getShipMass();
        if (mass <= 0) return;

        Vector3d force = new Vector3d(velChange).mul(mass / dt);
        ship.applyInvariantForce(force);
    }


    public void setTarget(Vector3d newTarget) {
        this.targetPos.set(newTarget);
        this.active = true;
    }

    public void release() {
        this.active = false;
    }

    public static PhysGrabberServerAttachment getOrCreate(ServerShip ship) {
        PhysGrabberServerAttachment grabber =
                AttachmentUtils.getOrCreate(ship, PhysGrabberServerAttachment.class, PhysGrabberServerAttachment::new);


        ship.saveAttachment(PhysGrabberServerAttachment.class, grabber);


        return grabber;
    }
}
