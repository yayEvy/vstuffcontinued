package yay.evy.everest.vstuff.content.ships.thrust;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import net.minecraft.core.BlockPos;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;


public class ThrusterForceApplier {
    ThrusterData data;

    public ThrusterForceApplier(ThrusterData data){
        this.data = data;
    }

    private final Vector3d worldForceDirection = new Vector3d();
    private final Vector3d worldForce = new Vector3d();
    private final Vector3d parallelForce = new Vector3d();
    private final Vector3d perpendicularForce = new Vector3d();
    private Vector3d velocityDirection = new Vector3d();

    private static final Vector3d scaledForce_temp1 = new Vector3d();
    private static final Vector3d scaledForce_temp2 = new Vector3d();
    private static final Vector3d scaledForce_temp3 = new Vector3d();

    public void applyForces(BlockPos pos, PhysShipImpl ship) {
        float thrust = data.getThrust();
        if (thrust == 0) return;

        final int maxSpeed = VStuffConfigs.server().thrusterMaxSpeed.get();
        final ShipTransform transform = ship.getTransform();
        final Vector3dc shipCenterOfMass = transform.getPositionInShip();

        Vector3d relativePos = VectorConversionsMCKt.toJOMLD(pos)
                .add(0.5, 0.5, 0.5)
                .sub(shipCenterOfMass);

        Vector3d thrusterDir = new Vector3d(data.getDirection());
        if (thrusterDir.lengthSquared() < 1e-6) {
            thrusterDir.set(0, 0, 1);
        }

        transform.getShipToWorld().transformDirection(thrusterDir, worldForceDirection);
        worldForceDirection.normalize();
        worldForce.set(worldForceDirection).mul(thrust);



        Vector3dc linearVelocity = ship.getVelocity();
        if (linearVelocity.lengthSquared() >= maxSpeed * maxSpeed) {
            double dot = worldForce.dot(linearVelocity);
            if (dot > 0) {

                double forceLengthSq = worldForce.lengthSquared();
                if (forceLengthSq > 1e-9) {
                    velocityDirection = velocityDirection.set(linearVelocity).normalize();
                    double parallelMagnitude = worldForce.dot(velocityDirection);
                    parallelForce.set(velocityDirection).mul(parallelMagnitude);
                    perpendicularForce.set(worldForce).sub(parallelForce);
                    ship.applyInvariantForceToPos(perpendicularForce, relativePos);
                    applyScaledForce(ship, linearVelocity, parallelForce, maxSpeed);
                }
                return;
            }
        }

        ship.applyInvariantForceToPos(worldForce, relativePos);
    }

    private static void applyScaledForce(PhysShipImpl ship, Vector3dc linearVelocity, Vector3d forceToScale, float maxSpeed){
        var currentServer = ValkyrienSkiesMod.getCurrentServer();
        if (currentServer == null) return;

        var pipeline = VSGameUtilsKt.getVsPipeline(currentServer);
        double physTps = pipeline.computePhysTps();
        if (physTps <= 0) return;
        double deltaTime = 1.0 / physTps;
        double mass = ship.getMass();
        if (mass <= 0) return;

        forceToScale.mul(deltaTime / mass, scaledForce_temp1);
        linearVelocity.add(scaledForce_temp1, scaledForce_temp2);
        scaledForce_temp2.normalize(maxSpeed, scaledForce_temp3);
        scaledForce_temp3.sub(linearVelocity, scaledForce_temp1);
        scaledForce_temp1.mul(mass / deltaTime, scaledForce_temp2);
        ship.applyInvariantForce(scaledForce_temp2);
    }
}