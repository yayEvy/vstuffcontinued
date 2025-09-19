package yay.evy.everest.vstuff.thruster;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import net.minecraft.core.BlockPos;
import yay.evy.everest.vstuff.VstuffConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ThrusterForceApplier {
    ThrusterData data;

    public ThrusterForceApplier(ThrusterData data){
        this.data = data;
    }

    //Cached vectors to reduce allocations on physics thread
    private Vector3d relativePos = new Vector3d();
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

        final int maxSpeed = VstuffConfig.THRUSTER_MAX_SPEED.get();
        final ShipTransform transform = ship.getTransform();
        final Vector3dc shipCenterOfMass = transform.getPositionInShip();

        relativePos = VectorConversionsMCKt.toJOMLD(pos)
                .add(0.5, 0.5, 0.5)
                .sub(shipCenterOfMass);

        // Get thruster direction, fallback to +Z if invalid
        Vector3d thrusterDir = new Vector3d(data.getDirection());
        if (thrusterDir.lengthSquared() < 1e-6) {
            thrusterDir.set(0, 0, 1); // default forward
        }

        transform.getShipToWorld().transformDirection(thrusterDir, worldForceDirection);
        worldForceDirection.normalize();
        worldForce.set(worldForceDirection).mul(thrust);

        System.out.println(
                "[ThrusterForceApplier] thrust=" + thrust +
                        " dir=" + thrusterDir +
                        " worldForce=" + worldForce +
                        " relPos=" + relativePos
        );


        final Vector3dc linearVelocity = ship.getPoseVel().getVel();
        if (linearVelocity.lengthSquared() >= maxSpeed * maxSpeed) {
            double dot = worldForce.dot(linearVelocity);
            if (dot > 0) {
                System.out.println(
                        "[ThrusterForceApplier] capped by velocity, dot=" + dot +
                                " linearVel=" + linearVelocity
                );
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
        if (physTps <= 0) return; // Sometimes physics runs backwards and this results in timeline splitting, we try to avoid that with this line
        double deltaTime = 1.0 / physTps;
        double mass = ship.getInertia().getShipMass();
        if (mass <= 0) return; //Same with tps but in case of negative mass we can accidentally create alcubierre bubble

        forceToScale.mul(deltaTime / mass, scaledForce_temp1);
        linearVelocity.add(scaledForce_temp1, scaledForce_temp2);
        scaledForce_temp2.normalize(maxSpeed, scaledForce_temp3);
        scaledForce_temp3.sub(linearVelocity, scaledForce_temp1);
        scaledForce_temp1.mul(mass / deltaTime, scaledForce_temp2);
        ship.applyInvariantForce(scaledForce_temp2);
    }
}