package yay.evy.everest.vstuff.magnetism;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.block.RedstoneMagnetBlock;
import yay.evy.everest.vstuff.magnetism.MagnetRegistry.MagnetData;
import yay.evy.everest.vstuff.magnetism.MagnetRegistry.MagnetPair;

import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MagnetismAttachment implements ShipForcesInducer {
    private static final double MAX_FORCE = 500000.0;
    private static final double BASE_FORCE_STRENGTH = 150000.0;
    private static final double MIN_FORCE_THRESHOLD = 0.1;
    private static final double DOCKED_DISTANCE = 0.5; // Increased for easier docking
    private static final double CLOSE_DOCKING_DISTANCE = 1.0;
    private static final double SMOOTH_TRANSITION_DISTANCE = 3.0;
    private static final double DOCKING_FORCE_MULTIPLIER = 8.0;
    private static final double CLOSE_DOCKING_MULTIPLIER = 3.0;
    private static final double MULTI_MAGNET_BONUS = 2.0; // Reduced to prevent excessive forces

    private static final double MIN_POWER_MULTIPLIER = 0.1;
    private static final double MAX_POWER_MULTIPLIER = 1.0;

    private static final int CACHE_TICKS = 3; // Update every 3 ticks instead of every tick
    private int tickCounter = 0;
    private Vector3d cachedForce = new Vector3d();
    private Vector3d cachedTorque = new Vector3d();

    @JsonIgnore
    private ServerShip ship = null;
    @JsonIgnore
    private ServerLevel level = null;

    public MagnetismAttachment() {}

    public MagnetismAttachment(ServerShip ship, ServerLevel level) {
        this.ship = ship;
        this.level = level;
    }

    public static MagnetismAttachment getOrCreate(ServerShip ship, ServerLevel level) {
        MagnetismAttachment attachment = ship.getAttachment(MagnetismAttachment.class);
        if (attachment == null) {
            attachment = new MagnetismAttachment(ship, level);
            ship.saveAttachment(MagnetismAttachment.class, attachment);
        } else {
            attachment.ship = ship;
            attachment.level = level;
        }
        return attachment;
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        if (ship == null || level == null) return;

        tickCounter++;
        if (tickCounter < CACHE_TICKS) {
            // Apply cached forces
            if (cachedForce.length() > MIN_FORCE_THRESHOLD) {
                physShip.applyInvariantForce(cachedForce);
            }
            if (cachedTorque.length() > MIN_FORCE_THRESHOLD) {
                physShip.applyInvariantTorque(cachedTorque);
            }
            return;
        }
        tickCounter = 0;

        MagnetRegistry registry = MagnetRegistry.getInstance();
        Set<MagnetPair> pairs = registry.getPairsForShip(ship.getId());
        if (pairs.isEmpty()) {
            cachedForce.zero();
            cachedTorque.zero();
            return;
        }

        // Group pairs by target ship to combine forces from multiple magnets
        Map<Long, List<MagnetPair>> pairsByTargetShip = groupPairsByTargetShip(pairs);
        Vector3d totalForce = new Vector3d();
        Vector3d totalTorque = new Vector3d();

        for (Map.Entry<Long, List<MagnetPair>> entry : pairsByTargetShip.entrySet()) {
            Long targetShipId = entry.getKey();
            List<MagnetPair> shipPairs = entry.getValue();

            Vector3d combinedForce = calculateCombinedMagneticForce(shipPairs, physShip, registry);
            if (combinedForce != null && combinedForce.length() > MIN_FORCE_THRESHOLD) {
                totalForce.add(combinedForce);

                Vector3d combinedTorque = calculateCombinedTorque(shipPairs, combinedForce, physShip, registry);
                if (combinedTorque != null) {
                    totalTorque.add(combinedTorque);
                }
            }
        }

        cachedForce.set(totalForce);
        cachedTorque.set(totalTorque);

        // Apply forces
        if (totalForce.length() > MIN_FORCE_THRESHOLD) {
            physShip.applyInvariantForce(totalForce);
        }
        if (totalTorque.length() > MIN_FORCE_THRESHOLD) {
            physShip.applyInvariantTorque(totalTorque);
        }
    }

    private Map<Long, List<MagnetPair>> groupPairsByTargetShip(Set<MagnetPair> pairs) {
        Map<Long, List<MagnetPair>> grouped = new HashMap<>();
        for (MagnetPair pair : pairs) {
            Long targetShipId = null;
            if (Objects.equals(pair.ship1Id, ship.getId())) {
                targetShipId = pair.ship2Id;
            } else if (Objects.equals(pair.ship2Id, ship.getId())) {
                targetShipId = pair.ship1Id;
            }

            if (targetShipId != null) {
                grouped.computeIfAbsent(targetShipId, k -> new ArrayList<>()).add(pair);
            }
        }
        return grouped;
    }

    private Vector3d calculateCombinedMagneticForce(List<MagnetPair> pairs, PhysShip physShip, MagnetRegistry registry) {
        Vector3d combinedForce = new Vector3d();
        int validPairs = 0;
        boolean anyDocked = false;
        boolean anyCloseToDocking = false;
        double minDistance = Double.MAX_VALUE;

        for (MagnetPair pair : pairs) {
            minDistance = Math.min(minDistance, pair.distance);
            if (pair.distance <= DOCKED_DISTANCE) {
                anyDocked = true;
            } else if (pair.distance <= CLOSE_DOCKING_DISTANCE) {
                anyCloseToDocking = true;
            }

            Vector3d individualForce = calculateIndividualMagneticForce(pair, physShip, registry);
            if (individualForce != null) {
                combinedForce.add(individualForce);
                validPairs++;
            }
        }

        if (validPairs == 0) return null;

        if (pairs.size() > 1) {
            double bonus = 1.0 + (pairs.size() - 1) * MULTI_MAGNET_BONUS / Math.max(minDistance, 0.5);
            combinedForce.mul(Math.min(bonus, 5.0));
        }

        if (anyDocked) {
            double stabilityFactor = Math.max(0.2, Math.min(1.0, minDistance / (DOCKED_DISTANCE * 0.5)));
            combinedForce.mul(stabilityFactor * DOCKING_FORCE_MULTIPLIER);
        } else if (anyCloseToDocking) {
            combinedForce.mul(CLOSE_DOCKING_MULTIPLIER);
        }

        return combinedForce;
    }


    private Vector3d calculateIndividualMagneticForce(MagnetPair pair, PhysShip physShip, MagnetRegistry registry) {
        MagnetData shipMagnet = null;
        MagnetData otherMagnet = null;
        Long otherShipId = null;

        if (Objects.equals(pair.ship1Id, ship.getId())) {
            shipMagnet = pair.magnet1;
            otherMagnet = pair.magnet2;
            otherShipId = pair.ship2Id;
        } else if (Objects.equals(pair.ship2Id, ship.getId())) {
            shipMagnet = pair.magnet2;
            otherMagnet = pair.magnet1;
            otherShipId = pair.ship1Id;
        } else {
            return null;
        }

        Vector3d shipMagnetWorldPos = registry.getMagnetWorldPos(level, shipMagnet, ship.getId());
        Vector3d otherMagnetWorldPos = registry.getMagnetWorldPos(level, otherMagnet, otherShipId);

        if (shipMagnetWorldPos == null || otherMagnetWorldPos == null) {
            return null;
        }

        Vector3d direction = otherMagnetWorldPos.sub(shipMagnetWorldPos, new Vector3d());
        double distance = direction.length();
        if (distance < 0.01) return null;

        direction.normalize();

        boolean shouldAttract = shouldMagnetsAttract(shipMagnet, otherMagnet, physShip.getTransform(), otherShipId);

        double shipMagnetPower = getRedstonePowerMultiplier(shipMagnet.state);
        double otherMagnetPower = getRedstonePowerMultiplier(otherMagnet.state);
        double averagePower = (shipMagnetPower + otherMagnetPower) / 2.0;

        double forceMagnitude = calculateEnhancedForceMagnitude(distance, shouldAttract,
                pair.distance <= DOCKED_DISTANCE, averagePower);

        Vector3d force = direction.mul(shouldAttract ? forceMagnitude : -forceMagnitude);
        return force;
    }

    private double getRedstonePowerMultiplier(BlockState magnetState) {
        if (!(magnetState.getBlock() instanceof RedstoneMagnetBlock)) {
            return MAX_POWER_MULTIPLIER;
        }

        int powerLevel = RedstoneMagnetBlock.getPowerLevel(magnetState);
        if (powerLevel <= 0) {
            return 0.0;
        }

        double normalizedPower = (double) powerLevel / 15.0;
        return MIN_POWER_MULTIPLIER + (normalizedPower * (MAX_POWER_MULTIPLIER - MIN_POWER_MULTIPLIER));
    }

    private double calculateEnhancedForceMagnitude(double distance, boolean shouldAttract, boolean isDocked, double powerMultiplier) {
        double baseForce;

        if (distance < DOCKED_DISTANCE) {
            baseForce = BASE_FORCE_STRENGTH * (2.0 - distance / DOCKED_DISTANCE);
        } else if (distance < CLOSE_DOCKING_DISTANCE) {
            double factor = 1.0 / Math.max(distance, 0.1);
            baseForce = BASE_FORCE_STRENGTH * factor;
        } else {
            baseForce = BASE_FORCE_STRENGTH / Math.max(distance * distance, 1.0);
        }

        baseForce *= powerMultiplier;

        if (!shouldAttract) {
            double repulsionForce = Math.min(baseForce * 0.7, MAX_FORCE * 0.3);
            return repulsionForce;
        }

        if (isDocked) {
            baseForce *= DOCKING_FORCE_MULTIPLIER;
        } else if (distance < CLOSE_DOCKING_DISTANCE) {
            baseForce *= CLOSE_DOCKING_MULTIPLIER;
        }

        return Math.min(baseForce, MAX_FORCE * powerMultiplier);
    }

    private Vector3d calculateCombinedTorque(List<MagnetPair> pairs, Vector3d combinedForce, PhysShip physShip, MagnetRegistry registry) {
        Vector3d shipCenterWorldPos = new Vector3d(physShip.getTransform().getPositionInWorld());
        Vector3d magnetCenterOfMass = new Vector3d();
        int validMagnets = 0;

        for (MagnetPair pair : pairs) {
            MagnetData shipMagnet = getShipMagnetFromPair(pair);
            if (shipMagnet == null) continue;

            Vector3d shipMagnetWorldPos = registry.getMagnetWorldPos(level, shipMagnet, ship.getId());
            if (shipMagnetWorldPos != null) {
                magnetCenterOfMass.add(shipMagnetWorldPos);
                validMagnets++;
            }
        }

        if (validMagnets == 0) return null;
        magnetCenterOfMass.div(validMagnets);

        Vector3d leverArm = magnetCenterOfMass.sub(shipCenterWorldPos, new Vector3d());
        Vector3d torque = leverArm.cross(combinedForce, new Vector3d());

        double avgDistance = pairs.stream().mapToDouble(p -> p.distance).average().orElse(1.0);
        double torqueScaling = Math.min(0.05 * pairs.size(), 0.3 / Math.max(avgDistance, 1.0));
        torque.mul(torqueScaling);

        return torque;
    }

    private MagnetData getShipMagnetFromPair(MagnetPair pair) {
        if (Objects.equals(pair.ship1Id, ship.getId())) {
            return pair.magnet1;
        } else if (Objects.equals(pair.ship2Id, ship.getId())) {
            return pair.magnet2;
        }
        return null;
    }

    private boolean shouldMagnetsAttract(MagnetData shipMagnet, MagnetData otherMagnet, ShipTransform shipTransform, Long otherShipId) {
        Vec3i shipFacingVec = RedstoneMagnetBlock.getFacing(shipMagnet.state).getNormal();
        Vector3d shipFacing = new Vector3d(shipFacingVec.getX(), shipFacingVec.getY(), shipFacingVec.getZ());

        Vec3i otherFacingVec = RedstoneMagnetBlock.getFacing(otherMagnet.state).getNormal();
        Vector3d otherFacing = new Vector3d(otherFacingVec.getX(), otherFacingVec.getY(), otherFacingVec.getZ());

        shipTransform.getShipToWorld().transformDirection(shipFacing);

        if (otherShipId != null) {
            for (var shipObj : VSGameUtilsKt.getAllShips(level)) {
                if (shipObj instanceof ServerShip && ((ServerShip) shipObj).getId() == otherShipId) {
                    ServerShip otherShip = (ServerShip) shipObj;
                    otherShip.getTransform().getShipToWorld().transformDirection(otherFacing);
                    break;
                }
            }
        }

        double facingDot = shipFacing.dot(otherFacing);


        return facingDot < 0;
    }
}

