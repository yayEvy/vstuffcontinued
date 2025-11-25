package yay.evy.everest.vstuff.content.physgrabber;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import yay.evy.everest.vstuff.content.thrust.AttachmentUtils;

public class PhysGrabberServerAttachment implements ShipPhysicsListener {

    private final Vector3d targetPos = new Vector3d();
    private boolean active = false;
    private boolean isCreative = false;

    private static final double MAX_SPEED = 20.0;

    @Override
    public void physTick(PhysShip physShip, PhysLevel physLevel) {
        if (!active) return;

        PhysShipImpl ship = (PhysShipImpl) physShip;

        Vector3dc shipPos = ship.getTransform().getPositionInWorld();
        Vector3d toTarget = new Vector3d(targetPos).sub(shipPos);
        double distance = toTarget.length();

        if (distance < 0.05) return;

        double pullSpeed = Math.min(MAX_SPEED, distance * 2.0);
        Vector3d desiredVel = new Vector3d(toTarget).normalize().mul(pullSpeed);

        Vector3d currentVel = new Vector3d(ship.getVelocity());
        double smoothingFactor = 0.1;

        Vector3d velChange = desiredVel.sub(currentVel).mul(smoothingFactor);

        double mass = ship.getMass();
        Vector3d force = new Vector3d(velChange).mul(mass);

        double maxForce = isCreative ? Double.MAX_VALUE : 10000.0;

        force.x = Math.max(Math.min(force.x, maxForce), -maxForce);
        force.y = Math.max(Math.min(force.y, maxForce), -maxForce);
        force.z = Math.max(Math.min(force.z, maxForce), -maxForce);

        ship.applyInvariantForce(force);
    }

    @Override
    public void physTick(PhysShip physShip, PhysLevel physLevel, double delta) {
        physTick(physShip, physLevel);
    }

    public void setTarget(Vector3d newTarget) {
        this.targetPos.set(newTarget);
        this.active = true;
    }

    public void release() {
        this.active = false;
    }

    public void setCreative(boolean creative) {
        this.isCreative = creative;
    }

    public static PhysGrabberServerAttachment getOrCreate(LoadedServerShip ship) {
        return AttachmentUtils.getOrCreate(
                ship,
                PhysGrabberServerAttachment.class,
                PhysGrabberServerAttachment::new
        );
    }
}