package yay.evy.everest.vstuff.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RopePhysicsManager {
    private static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();

    public static class RopeConstraintData {
        public final Long shipA;
        public final Long shipB;
        public final Vector3d localPosA;
        public final Vector3d localPosB;
        public final double ropeLength;
        public final double compliance;
        public final double maxForce;
        public final BlockPos pulleyPos;
        public final long creationTime;

        public RopeConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double ropeLength, double compliance, double maxForce,
                                  BlockPos pulleyPos) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.ropeLength = ropeLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
            this.pulleyPos = pulleyPos;
            this.creationTime = System.currentTimeMillis();
        }
    }

    public static Integer createRopeConstraint(ServerLevel level, BlockPos pulleyPos, BlockPos anchorPos,
                                               double ropeLength) {
        try {
            Long pulleyShipId = getShipIdAtPos(level, pulleyPos);
            Long anchorShipId = getShipIdAtPos(level, anchorPos);
            Long groundBodyId = getGroundBodyId(level);

            if (groundBodyId == null) {
                System.err.println("Could not get ground body ID!");
                return null;
            }

            if (pulleyShipId == null) pulleyShipId = groundBodyId;
            if (anchorShipId == null) anchorShipId = groundBodyId;

            if (pulleyShipId.equals(anchorShipId)) {
                return null;
            }

            Vector3d pulleyWorldPos = getWorldPosition(level, pulleyPos, pulleyShipId);
            pulleyWorldPos.add(0, -0.5, 0); // Rope attachment point

            Vector3d anchorWorldPos = getWorldPosition(level, anchorPos, anchorShipId);
            anchorWorldPos.add(0, 0.5, 0); // Top of anchor block

            Vector3d localPosA = convertWorldToLocal(level, pulleyWorldPos, pulleyShipId);
            Vector3d localPosB = convertWorldToLocal(level, anchorWorldPos, anchorShipId);

            double actualDistance = pulleyWorldPos.distance(anchorWorldPos);
            double constraintLength = Math.max(actualDistance, ropeLength);

            double compliance = calculateOptimalCompliance(constraintLength);
            double maxForce = calculateOptimalMaxForce(constraintLength);

            VSRopeConstraint ropeConstraint = new VSRopeConstraint(
                    pulleyShipId, anchorShipId,
                    compliance,
                    localPosA, localPosB,
                    maxForce,
                    constraintLength
            );

            Integer constraintId = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint(ropeConstraint);

            if (constraintId != null) {
                RopeConstraintData data = new RopeConstraintData(
                        pulleyShipId, anchorShipId, localPosA, localPosB,
                        constraintLength, compliance, maxForce, pulleyPos
                );
                activeConstraints.put(constraintId, data);

                ConstraintTracker.addRopePulleyConstraint(level, constraintId,
                        pulleyShipId, anchorShipId, localPosA, localPosB, constraintLength,
                        compliance, maxForce, pulleyPos);

                System.out.println("Created rope constraint " + constraintId +
                        " between ships " + pulleyShipId + " and " + anchorShipId +
                        " with length " + constraintLength);
            }

            return constraintId;

        } catch (Exception e) {
            System.err.println("Error creating rope constraint: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void removeRopeConstraint(ServerLevel level, Integer constraintId) {
        if (constraintId == null) return;

        try {
            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
            activeConstraints.remove(constraintId);
            ConstraintTracker.removeConstraintWithPersistence(level, constraintId);
            System.out.println("Removed rope constraint " + constraintId);
        } catch (Exception e) {
            System.err.println("Error removing rope constraint " + constraintId + ": " + e.getMessage());
        }
    }

    public static RopeConstraintData getConstraintData(Integer constraintId) {
        return activeConstraints.get(constraintId);
    }

    public static Vec3 getConstraintEndPosition(ServerLevel level, Integer constraintId,
                                                BlockPos pulleyPos, boolean getPulleyEnd) {
        RopeConstraintData data = activeConstraints.get(constraintId);
        if (data == null) return null;

        try {
            Long shipId = getPulleyEnd ? data.shipA : data.shipB;
            Vector3d localPos = getPulleyEnd ? data.localPosA : data.localPosB;
            Vector3d worldPos = getWorldPosition(level, localPos, shipId);

            // Convert to relative position from pulley
            return new Vec3(
                    worldPos.x - pulleyPos.getX() - 0.5,
                    worldPos.y - pulleyPos.getY() - 0.5,
                    worldPos.z - pulleyPos.getZ() - 0.5
            );
        } catch (Exception e) {
            System.err.println("Error getting constraint position: " + e.getMessage());
            return null;
        }
    }

    public static boolean shouldUsePhysicsRendering(Integer constraintId, boolean isExtending, boolean isRetracting, boolean hasFoundAnchor) {
        if (constraintId == null) return false;

        if (isExtending || isRetracting) return false;

        if (hasFoundAnchor) return false;

        RopeConstraintData data = activeConstraints.get(constraintId);
        if (data == null) return false;

        Long groundBodyId = getGroundBodyId(null); // We'll need to pass level here
        return data.shipB.equals(groundBodyId) && !data.shipA.equals(groundBodyId);
    }

    public static boolean shouldUsePhysicsRendering(ServerLevel level, Integer constraintId,
                                                    boolean isExtending, boolean isRetracting,
                                                    boolean hasFoundAnchor) {
        if (constraintId == null) return false;

        if (isExtending || isRetracting) return false;

        if (hasFoundAnchor) return false;

        RopeConstraintData data = activeConstraints.get(constraintId);
        if (data == null) return false;

        Long groundBodyId = getGroundBodyId(level);
        if (groundBodyId == null) return false;

        return data.shipB.equals(groundBodyId) && !data.shipA.equals(groundBodyId);
    }

    public static double getConstraintRopeLength(Integer constraintId) {
        RopeConstraintData data = activeConstraints.get(constraintId);
        return data != null ? data.ropeLength : 0.0;
    }


    private static double calculateOptimalCompliance(double ropeLength) {
        double baseCompliance = 1e-8;
        double lengthFactor = Math.min(ropeLength / 10.0, 2.0); // Cap at 2x for very long ropes
        return baseCompliance * (1.0 + lengthFactor * 0.5);
    }

    private static double calculateOptimalMaxForce(double ropeLength) {
        double baseForce = 5e7;
        double lengthFactor = Math.min(ropeLength / 10.0, 1.5); // Cap at 1.5x
        return baseForce * (1.0 + lengthFactor * 0.3);
    }

    private static Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        Ship shipObject = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
        return shipObject != null ? shipObject.getId() : null;
    }

    private static Long getGroundBodyId(ServerLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));
    }

    private static Vector3d getWorldPosition(ServerLevel level, BlockPos pos, Long shipId) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return getWorldPosition(level, localPos, shipId);
    }

    private static Vector3d getWorldPosition(ServerLevel level, Vector3d localPos, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId != null && !shipId.equals(groundBodyId)) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d worldPos = new Vector3d();
                shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                return worldPos;
            }
        }
        return new Vector3d(localPos);
    }

    private static Vector3d convertWorldToLocal(ServerLevel level, Vector3d worldPos, Long shipId) {
        Long groundBodyId = getGroundBodyId(level);
        if (shipId != null && !shipId.equals(groundBodyId)) {
            Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (shipObject != null) {
                Vector3d localPos = new Vector3d();
                shipObject.getTransform().getWorldToShip().transformPosition(worldPos, localPos);
                return localPos;
            }
        }
        return new Vector3d(worldPos);
    }

    public static void cleanupOldConstraints(ServerLevel level) {
        long currentTime = System.currentTimeMillis();
        activeConstraints.entrySet().removeIf(entry -> {
            RopeConstraintData data = entry.getValue();
            // Remove constraints older than 5 minutes that might be orphaned
            if (currentTime - data.creationTime > 300000) {
                try {
                    VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(entry.getKey());
                } catch (Exception e) {
                    // Constraint might already be removed
                }
                return true;
            }
            return false;
        });
    }

    public static Integer createFreeDanglingRopeConstraint(ServerLevel level, BlockPos pulleyPos, BlockPos virtualEndPos, double ropeLength) {
        try {
            Long pulleyShipId = getShipIdAtPos(level, pulleyPos);
            Long groundBodyId = getGroundBodyId(level);

            if (groundBodyId == null) {
                System.err.println("Could not get ground body ID!");
                return null;
            }

            if (pulleyShipId == null) pulleyShipId = groundBodyId;

            Vector3d pulleyWorldPos = getWorldPosition(level, pulleyPos, pulleyShipId);
            pulleyWorldPos.add(0, -0.5, 0);

            Vector3d virtualEndWorldPos = new Vector3d(pulleyWorldPos);
            virtualEndWorldPos.add(0, -ropeLength, 0);
            Vector3d localPosA = convertWorldToLocal(level, pulleyWorldPos, pulleyShipId);
            Vector3d localPosB = convertWorldToLocal(level, virtualEndWorldPos, groundBodyId);

            double compliance = calculateOptimalCompliance(ropeLength) * 10.0;
            double maxForce = calculateOptimalMaxForce(ropeLength) * 0.1;

            VSRopeConstraint ropeConstraint = new VSRopeConstraint(
                    pulleyShipId, groundBodyId,
                    compliance,
                    localPosA, localPosB,
                    maxForce,
                    ropeLength
            );

            Integer constraintId = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint(ropeConstraint);

            if (constraintId != null) {
                RopeConstraintData data = new RopeConstraintData(
                        pulleyShipId, groundBodyId, localPosA, localPosB,
                        ropeLength, compliance, maxForce, pulleyPos
                );
                activeConstraints.put(constraintId, data);

                System.out.println("Created free-dangling rope constraint " + constraintId +
                        " between ship " + pulleyShipId + " and ground " + groundBodyId +
                        " with length " + ropeLength);
            }

            return constraintId;
        } catch (Exception e) {
            System.err.println("Error creating free-dangling rope constraint: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
