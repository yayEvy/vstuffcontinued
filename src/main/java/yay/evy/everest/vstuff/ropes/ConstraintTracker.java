package yay.evy.everest.vstuff.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    private static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();

    public static class RopeConstraintData {
        public final Long shipA;
        public final Long shipB;
        public final Vector3d localPosA;
        public final Vector3d localPosB;
        public final double maxLength;
        public final double compliance;
        public final double maxForce;

        public RopeConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
        }

        public Vector3d getWorldPosA(ServerLevel level, float partialTick) {
            try {
                Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                        .getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));

                if (shipA.equals(groundBodyId)) {
                    return new Vector3d(localPosA);
                } else {
                    Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipA);
                    if (shipObject != null) {
                        Vector3d worldPos = new Vector3d();
                        shipObject.getTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                        return worldPos;
                    }
                }
                return new Vector3d(localPosA);
            } catch (Exception e) {
                return new Vector3d(localPosA);
            }
        }

        public Vector3d getWorldPosB(ServerLevel level, float partialTick) {
            try {
                Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                        .getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));

                if (shipB.equals(groundBodyId)) {
                    return new Vector3d(localPosB);
                } else {
                    Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipB);
                    if (shipObject != null) {
                        Vector3d worldPos = new Vector3d();
                        shipObject.getTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                        return worldPos;
                    }
                }
                return new Vector3d(localPosB);
            } catch (Exception e) {
                return new Vector3d(localPosB);
            }
        }
    }

    public static void addConstraintWithPersistence(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                                    Vector3d localPosA, Vector3d localPosB, double maxLength,
                                                    double compliance, double maxForce) {
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = "constraint_" + constraintId;
        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added constraint " + constraintId + " to tracker, persistence, and synced to clients");
    }


    public static void removeConstraint(Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            NetworkHandler.sendConstraintRemove(constraintId);
            System.out.println("Removed constraint " + constraintId + " from tracker and synced to clients");
        }
    }

    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            ConstraintPersistence persistence = ConstraintPersistence.get(level);
            String persistenceId = "constraint_" + constraintId;
            persistence.removeConstraint(persistenceId);

            NetworkHandler.sendConstraintRemove(constraintId);
            System.out.println("Removed constraint " + constraintId + " from tracker, persistence, and synced to clients");
        }
    }

    public static Map<Integer, RopeConstraintData> getActiveConstraints() {
        return new HashMap<>(activeConstraints);
    }

    public static void clearAllConstraints() {
        activeConstraints.clear();
        NetworkHandler.sendConstraintClearAll();
        System.out.println("Cleared all constraints and synced to clients");
    }
    public static void addConstraintToTracker(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce) {
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added constraint " + constraintId + " to tracker (restoration) and synced to clients");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
                Integer constraintId = entry.getKey();
                RopeConstraintData data = entry.getValue();

                NetworkHandler.sendConstraintAddToPlayer(player, constraintId, data.shipA, data.shipB,
                        data.localPosA, data.localPosB, data.maxLength);
            }
            System.out.println("Synced " + activeConstraints.size() + " constraints to player " + player.getName().getString());
        }
    }

    // clean up constraints that aren't on valid blocks anymore, i think this might get rid of that dumb bug idk

    public static void validateAndCleanupConstraints(ServerLevel level) {
        System.out.println("=== CONSTRAINT VALIDATION START ===");
        System.out.println("Validating " + activeConstraints.size() + " active constraints...");
        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();
        Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                .getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));

        System.out.println("Ground body ID: " + groundBodyId);

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            System.out.println("--- Validating constraint " + constraintId + " ---");
            System.out.println("ShipA: " + data.shipA + ", ShipB: " + data.shipB);
            System.out.println("LocalPosA: " + data.localPosA + ", LocalPosB: " + data.localPosB);

            try {
                boolean shipAExists = data.shipA.equals(groundBodyId) ||
                        VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipA) != null;
                boolean shipBExists = data.shipB.equals(groundBodyId) ||
                        VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipB) != null;

                System.out.println("Ship existence - A: " + shipAExists + ", B: " + shipBExists);

                if (!shipAExists || !shipBExists) {
                    System.out.println("Constraint " + constraintId + " references missing ships - marking for removal");
                    constraintsToRemove.add(constraintId);
                    continue;
                }

                boolean validA = isValidAttachmentPoint(level, data.localPosA, data.shipA, groundBodyId);
                boolean validB = isValidAttachmentPoint(level, data.localPosB, data.shipB, groundBodyId);

                System.out.println("Attachment point validity - A: " + validA + ", B: " + validB);

                if (!validA || !validB) {
                    System.out.println("Constraint " + constraintId + " has invalid attachment points - marking for removal");
                    constraintsToRemove.add(constraintId);
                } else {
                    System.out.println("Constraint " + constraintId + " is valid");
                }
            } catch (Exception e) {
                System.err.println("Error validating constraint " + constraintId + ": " + e.getMessage());
                e.printStackTrace();
                constraintsToRemove.add(constraintId);
            }
        }

        // Remove invalid constraints
        for (Integer constraintId : constraintsToRemove) {
            try {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
                System.out.println("Cleaned up invalid constraint: " + constraintId);
            } catch (Exception e) {
                System.err.println("Error removing invalid constraint " + constraintId + ": " + e.getMessage());
            }
        }

        System.out.println("=== CONSTRAINT VALIDATION END ===");
        System.out.println("Constraint validation complete. Removed " + constraintsToRemove.size() + " invalid constraints.");
    }


    private static boolean isValidAttachmentPoint(ServerLevel level, Vector3d localPos, Long shipId, Long groundBodyId) {
        try {
            if (shipId.equals(groundBodyId)) {
                // For world attachments, localPos should be world coordinates
                net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                        (int) Math.floor(localPos.x),
                        (int) Math.floor(localPos.y),
                        (int) Math.floor(localPos.z)
                );

                if (!level.isLoaded(blockPos)) {
                    System.out.println("World block at " + blockPos + " is not loaded");
                    return false;
                }
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(blockPos);
                boolean valid = !state.isAir();
                System.out.println("World validation - localPos: " + localPos + " -> blockPos: " + blockPos + " -> " + (valid ? "valid" : "air"));
                return valid;
            } else {
                // For ship attachments - check if localPos looks like world coordinates (huge numbers)
                // If the coordinates are huge (> 1000000), they're probably world coordinates stored incorrectly
                boolean looksLikeWorldCoords = Math.abs(localPos.x) > 1000000 || Math.abs(localPos.z) > 1000000;

                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) {
                    System.out.println("Ship " + shipId + " no longer exists");
                    return false;
                }

                if (looksLikeWorldCoords) {
                    // These are world coordinates stored incorrectly - convert to ship-local
                    System.out.println("Detected world coordinates stored as ship-local, converting...");
                    Vector3d actualShipLocal = new Vector3d();
                    ship.getTransform().getWorldToShip().transformPosition(localPos, actualShipLocal);

                    net.minecraft.core.BlockPos shipLocalBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(actualShipLocal.x),
                            (int) Math.floor(actualShipLocal.y),
                            (int) Math.floor(actualShipLocal.z)
                    );

                    // Transform back to world to check the block
                    Vector3d worldPos = new Vector3d();
                    ship.getTransform().getShipToWorld().transformPosition(actualShipLocal, worldPos);
                    net.minecraft.core.BlockPos worldBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(worldPos.x),
                            (int) Math.floor(worldPos.y),
                            (int) Math.floor(worldPos.z)
                    );

                    if (!level.isLoaded(worldBlockPos)) {
                        System.out.println("Ship block world position " + worldBlockPos + " is not loaded");
                        return false;
                    }

                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                    boolean valid = !state.isAir();
                    System.out.println("Ship validation (corrected) - worldPos: " + localPos + " -> shipLocal: " + actualShipLocal + " -> worldBlock: " + worldBlockPos + " -> " + (valid ? "valid" : "air"));
                    return valid;
                } else {
                    // These are proper ship-local coordinates
                    net.minecraft.core.BlockPos shipLocalBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(localPos.x),
                            (int) Math.floor(localPos.y),
                            (int) Math.floor(localPos.z)
                    );

                    Vector3d worldPos = new Vector3d();
                    ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                    net.minecraft.core.BlockPos worldBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(worldPos.x),
                            (int) Math.floor(worldPos.y),
                            (int) Math.floor(worldPos.z)
                    );

                    if (!level.isLoaded(worldBlockPos)) {
                        System.out.println("Ship block world position " + worldBlockPos + " is not loaded");
                        return false;
                    }

                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                    boolean valid = !state.isAir();
                    System.out.println("Ship validation - shipLocal: " + localPos + " -> worldBlock: " + worldBlockPos + " -> " + (valid ? "valid" : "air"));
                    return valid;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking attachment point validity for shipId " + shipId + ", localPos " + localPos + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }






}
