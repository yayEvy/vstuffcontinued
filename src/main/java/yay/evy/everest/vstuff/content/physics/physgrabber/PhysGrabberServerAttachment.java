package yay.evy.everest.vstuff.content.physics.physgrabber;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public final class PhysGrabberServerAttachment implements ShipPhysicsListener {

    private final Vector3d targetPos = new Vector3d();
    private final Vector3d localHit = new Vector3d();
    private boolean active = false;
    private boolean isCreative = false;

    private static final double MAX_SPEED = 60.0;

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        if (!active) return;

        PhysShipImpl ship = (PhysShipImpl) physShip;

        Vector3d worldHitPos = ship.getTransform().getShipToWorld().transformPosition(new Vector3d(localHit), new Vector3d());

        Vector3d com = new Vector3d(ship.getTransform().getPositionInWorld());
        Vector3d r = new Vector3d(worldHitPos).sub(com);
        Vector3d omega = new Vector3d(ship.getOmega());
        Vector3d angularContrib = new Vector3d();
        omega.cross(r, angularContrib);
        Vector3d pointVel = new Vector3d(ship.getVelocity()).add(angularContrib);

        Vector3d toTarget = new Vector3d(targetPos).sub(worldHitPos);
        double distance = toTarget.length();

        if (distance < 0.01) return;

        double pullSpeed = Math.min(MAX_SPEED, distance * 8.0);
        Vector3d desiredVel = new Vector3d(toTarget).normalize().mul(pullSpeed);

        Vector3d velError = desiredVel.sub(pointVel);

        double maxVelError = 40.0;
        if (velError.length() > maxVelError) {
            velError.normalize().mul(maxVelError);
        }

        double mass = ship.getMass();

        Vector3d force = new Vector3d(velError)
                .mul(Math.min(mass, 20000.0))
                .mul(36.0);

        double maxForce = isCreative ? 1e10 : 8000000.0;
        if (force.lengthSquared() > maxForce * maxForce) {
            force.normalize().mul(maxForce);
        }

        ship.applyInvariantForce(force);

        Vector3d torque = new Vector3d(r).cross(force).mul(0.1);
        ship.applyInvariantTorque(torque);

        Vector3d angularVel = new Vector3d(ship.getOmega());
        Vector3d torqueDamping = new Vector3d(angularVel)
                .mul(-mass * 25.0);
        ship.applyInvariantTorque(torqueDamping);
    }

    public PhysGrabberServerAttachment setLocalHit(Vector3d localHit) {
        this.localHit.set(localHit);
        return this;
    }

    public PhysGrabberServerAttachment target(Vector3d newTarget) {
        this.targetPos.set(newTarget);
        this.active = true;
        return this;
    }

    public void release() {
        this.active = false;
    }

    public PhysGrabberServerAttachment creative(boolean creative) {
        this.isCreative = creative;
        return this;
    }
}