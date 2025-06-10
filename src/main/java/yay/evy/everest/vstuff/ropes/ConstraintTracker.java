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
    private static final Map<Integer, String> constraintToPersistenceId = new ConcurrentHashMap<>();

    public static class RopeConstraintData {
        public final Long shipA;
        public final Long shipB;
        public final Vector3d localPosA;
        public final Vector3d localPosB;
        public final double maxLength;
        public final double compliance;
        public final double maxForce;
        public final ConstraintType constraintType;
        public final net.minecraft.core.BlockPos sourceBlockPos; // Track the source block

        public enum ConstraintType {
            ROPE_PULLEY,    // From rope pulley blocks
            GENERIC         // Other constraint types
        }

        public RopeConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
            this.constraintType = ConstraintType.GENERIC; // Default to generic
            this.sourceBlockPos = null; // No source block for generic constraints
        }

        // ADDED: Extended constructor with constraint type and source block
        public RopeConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce,
                                  ConstraintType constraintType, net.minecraft.core.BlockPos sourceBlockPos) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
            this.constraintType = constraintType;
            this.sourceBlockPos = sourceBlockPos;
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
        // Use the original constructor - it now sets GENERIC type and null sourceBlockPos by default
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = java.util.UUID.randomUUID().toString();
        constraintToPersistenceId.put(constraintId, persistenceId);
        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level);
        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
    }

    public static void addRopePulleyConstraint(net.minecraft.server.level.ServerLevel level, Integer constraintId,
                                               Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                               double maxLength, double compliance, double maxForce,
                                               net.minecraft.core.BlockPos pulleyBlockPos) {
        // Use the extended constructor with rope pulley type and source block position
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength,
                compliance, maxForce,
                RopeConstraintData.ConstraintType.ROPE_PULLEY,
                pulleyBlockPos);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = java.util.UUID.randomUUID().toString();
        constraintToPersistenceId.put(constraintId, persistenceId);
        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level);
        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);

        System.out.println("Added rope pulley constraint " + constraintId + " from block at " + pulleyBlockPos);
    }

    public static void removeConstraint(Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            NetworkHandler.sendConstraintRemove(constraintId);
           // System.out.println("Removed constraint " + constraintId + " from tracker and synced to clients");
        }
    }

    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            ConstraintPersistence persistence = ConstraintPersistence.get(level);

            // Use the ACTUAL persistence ID that was stored
            String persistenceId = constraintToPersistenceId.get(constraintId);
            if (persistenceId != null) {
                // Mark as removed using the correct ID
                persistence.markConstraintAsRemoved(persistenceId);
                // Clean up mapping
                constraintToPersistenceId.remove(constraintId);
             //   System.out.println("Removed constraint " + constraintId + " from tracker, marked as removed in persistence (ID: " + persistenceId + "), and synced to clients");
            } else {
            //    System.err.println("Warning: No persistence ID found for constraint " + constraintId + " - this constraint may not have been properly persisted");
                // Still remove it from the tracker and sync to clients
            }

            NetworkHandler.sendConstraintRemove(constraintId);
        }
    }


    public static void mapConstraintToPersistenceId(Integer constraintId, String persistenceId) {
        constraintToPersistenceId.put(constraintId, persistenceId);
     //   System.out.println("Mapped constraint " + constraintId + " to persistence ID " + persistenceId);
    }

    public static Map<Integer, RopeConstraintData> getActiveConstraints() {
        return new HashMap<>(activeConstraints);
    }

    public static void clearAllConstraints() {
        activeConstraints.clear();
        NetworkHandler.sendConstraintClearAll();
    //    System.out.println("Cleared all constraints and synced to clients");
    }
    public static void addConstraintToTracker(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce) {
        // Check if constraint already exists
        if (activeConstraints.containsKey(constraintId)) {
      //      System.out.println("Constraint " + constraintId + " already exists in tracker, skipping");
            return;
        }

        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        // DON'T create a persistence mapping here - it should be done separately via mapConstraintToPersistenceId
        // The persistence ID should come from the restoration process, not be generated here

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
  //      System.out.println("Added constraint " + constraintId + " to tracker (restoration) and synced to clients");
    }

    public static void clearAllConstraintsAndMappings() {
        activeConstraints.clear();
        constraintToPersistenceId.clear(); // Clear the mappings too
        NetworkHandler.sendConstraintClearAll();
       // System.out.println("Cleared all constraints, mappings, and synced to clients");
    }

    // Add a method to get the persistence ID for debugging
    public static String getPersistenceId(Integer constraintId) {
        return constraintToPersistenceId.get(constraintId);
    }

    // Add a method to check if a constraint has a valid persistence mapping
    public static boolean hasValidPersistenceMapping(Integer constraintId) {
        return constraintToPersistenceId.containsKey(constraintId);
    }
    private static void validateSingleConstraint(ServerLevel level, Integer constraintId) {
        RopeConstraintData data = activeConstraints.get(constraintId);
        if (data == null) return;

        try {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));

            boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
            boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

            if (!shipAExists || !shipBExists) {
                System.out.println("Delayed validation: Constraint " + constraintId + " still references missing ships - removing");
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
            } else {
                System.out.println("Delayed validation: Constraint " + constraintId + " is now valid");
            }
        } catch (Exception e) {
            System.err.println("Error in delayed validation for constraint " + constraintId + ": " + e.getMessage());
        }
    }
    private static boolean isShipValid(ServerLevel level, Long shipId, Long groundBodyId) {
        if (shipId == null) return false;

        if (shipId.equals(groundBodyId)) {
            return true; // Ground is always valid
        }

        try {
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            var ship = shipWorld.getAllShips().getById(shipId);
            boolean exists = ship != null;

            if (!exists) {
                System.out.println("Ship " + shipId + " not found in ship world");
                // Try alternative lookup methods
                var allShips = shipWorld.getAllShips();
                System.out.println("Available ships: " + allShips.stream().map(s -> s.getId()).toList());
            }

            return exists;
        } catch (Exception e) {
            System.err.println("Exception checking ship validity for " + shipId + ": " + e.getMessage());
            return false;
        }
    }

    // Add delayed validation mechanism
    private static final Map<Integer, Long> delayedValidations = new ConcurrentHashMap<>();

    private static void scheduleDelayedValidation(ServerLevel level, Integer constraintId, long delayMs) {
        delayedValidations.put(constraintId, System.currentTimeMillis() + delayMs);
        System.out.println("Scheduled delayed validation for constraint " + constraintId + " in " + (delayMs/1000) + " seconds");
    }

    // Call this method periodically (e.g., from a tick event)
    public static void processDelayedValidations(ServerLevel level) {
        long currentTime = System.currentTimeMillis();
        java.util.List<Integer> toValidate = new java.util.ArrayList<>();

        delayedValidations.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                toValidate.add(entry.getKey());
                return true;
            }
            return false;
        });

        for (Integer constraintId : toValidate) {
            if (activeConstraints.containsKey(constraintId)) {
                System.out.println("Processing delayed validation for constraint " + constraintId);
                validateSingleConstraint(level, constraintId);
            }
        }
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

    public static void validateAndCleanupConstraints(ServerLevel level) {
        System.out.println("=== CONSTRAINT VALIDATION START ===");
        System.out.println("Validating " + activeConstraints.size() + " active constraints...");

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();
        Long groundBodyId = null;
        try {
            groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            System.out.println("Ground body ID: " + groundBodyId);
        } catch (Exception e) {
            System.err.println("Failed to get ground body ID, skipping validation: " + e.getMessage());
            return;
        }

        if (groundBodyId == null) {
            System.err.println("Ground body ID is null, skipping validation");
            return;
        }

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();
            System.out.println("--- Validating constraint " + constraintId + " (Type: " + data.constraintType + ") ---");

            try {
                // Check ship existence first
                boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
                boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);
                System.out.println("Ship existence - A: " + shipAExists + ", B: " + shipBExists);

                if (!shipAExists || !shipBExists) {
                    System.out.println("Constraint " + constraintId + " references missing ships");
                    scheduleDelayedValidation(level, constraintId, 5000);
                    continue;
                }

                // Different validation logic based on constraint type
                boolean isValid = false;
                if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY) {
                    // For rope pulleys, validate the source pulley block exists
                    isValid = validateRopePulleyConstraint(level, data);
                    System.out.println("Rope pulley constraint validation: " + isValid);

                    // Give rope pulleys more chances before removing
                    if (!isValid) {
                        System.out.println("Rope pulley constraint " + constraintId + " failed validation - scheduling delayed recheck");
                        scheduleDelayedValidation(level, constraintId, 10000);
                        continue; // Don't mark for immediate removal
                    }
                } else {
                    // For generic constraints, use the old validation logic
                    boolean validA = isValidAttachmentPoint(level, data.localPosA, data.shipA, groundBodyId);
                    boolean validB = isValidAttachmentPoint(level, data.localPosB, data.shipB, groundBodyId);
                    isValid = validA && validB;
                    System.out.println("Generic constraint validation - A: " + validA + ", B: " + validB);
                }

                if (!isValid) {
                    System.out.println("Constraint " + constraintId + " is invalid - marking for removal");
                    constraintsToRemove.add(constraintId);
                } else {
                    System.out.println("Constraint " + constraintId + " is valid");
                }
            } catch (Exception e) {
                System.err.println("Error validating constraint " + constraintId + ": " + e.getMessage());
                // For rope pulleys, be more lenient with errors
                if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY) {
                    System.out.println("Rope pulley constraint error - scheduling delayed validation instead of removal");
                    scheduleDelayedValidation(level, constraintId, 15000);
                } else {
                    scheduleDelayedValidation(level, constraintId, 5000);
                }
            }
        }

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


    private static boolean validateRopePulleyConstraint(ServerLevel level, RopeConstraintData data) {
        try {
            if (data.sourceBlockPos != null) {
                if (!level.isLoaded(data.sourceBlockPos)) {
                    System.out.println("Rope pulley block at " + data.sourceBlockPos + " is not loaded - keeping constraint");
                    return true;
                }

                var blockEntity = level.getBlockEntity(data.sourceBlockPos);
                if (blockEntity instanceof RopePulleyBlockEntity) {
                    RopePulleyBlockEntity pulley = (RopePulleyBlockEntity) blockEntity;
                    System.out.println("Found valid rope pulley block entity at " + data.sourceBlockPos);
                    return true;
                }

                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(data.sourceBlockPos);
                if (state.isAir()) {
                    System.out.println("Block at " + data.sourceBlockPos + " is air - rope pulley was removed");
                    return false;
                }

                String blockClassName = state.getBlock().getClass().getSimpleName();
                String blockRegistryName = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

                if (blockClassName.toLowerCase().contains("rope") && blockClassName.toLowerCase().contains("pulley")) {
                    System.out.println("Found rope pulley block by class name: " + blockClassName);
                    return true;
                }

                if (blockRegistryName.contains("rope_pulley")) {
                    System.out.println("Found rope pulley block by registry name: " + blockRegistryName);
                    return true;
                }

                System.out.println("Block at " + data.sourceBlockPos + " is not a rope pulley: " + blockClassName + " (" + blockRegistryName + ")");
                return false;

            } else {
                System.out.println("No source block position stored for rope pulley constraint - assuming valid (legacy)");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error validating rope pulley constraint: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }



    private static boolean isValidAttachmentPoint(ServerLevel level, Vector3d localPos, Long shipId, Long groundBodyId) {
        try {
            if (shipId.equals(groundBodyId)) {
                net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                        (int) Math.floor(localPos.x),
                        (int) Math.floor(localPos.y),
                        (int) Math.floor(localPos.z)
                );

                if (!level.isLoaded(blockPos)) {
               //     System.out.println("World block at " + blockPos + " is not loaded");
                    return false;
                }
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(blockPos);
                boolean valid = !state.isAir();
              //  System.out.println("World validation - localPos: " + localPos + " -> blockPos: " + blockPos + " -> " + (valid ? "valid" : "air"));
                return valid;
            } else {
                boolean looksLikeWorldCoords = Math.abs(localPos.x) > 1000000 || Math.abs(localPos.z) > 1000000;

                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) {
                   // System.out.println("Ship " + shipId + " no longer exists");
                    return false;
                }

                if (looksLikeWorldCoords) {
                    Vector3d actualShipLocal = new Vector3d();
                    ship.getTransform().getWorldToShip().transformPosition(localPos, actualShipLocal);

                    net.minecraft.core.BlockPos shipLocalBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(actualShipLocal.x),
                            (int) Math.floor(actualShipLocal.y),
                            (int) Math.floor(actualShipLocal.z)
                    );

                    Vector3d worldPos = new Vector3d();
                    ship.getTransform().getShipToWorld().transformPosition(actualShipLocal, worldPos);
                    net.minecraft.core.BlockPos worldBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(worldPos.x),
                            (int) Math.floor(worldPos.y),
                            (int) Math.floor(worldPos.z)
                    );

                    if (!level.isLoaded(worldBlockPos)) {
                        return false;
                    }

                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                    boolean valid = !state.isAir();
               //     System.out.println("Ship validation (corrected) - worldPos: " + localPos + " -> shipLocal: " + actualShipLocal + " -> worldBlock: " + worldBlockPos + " -> " + (valid ? "valid" : "air"));
                    return valid;
                } else {
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
                      //  System.out.println("Ship block world position " + worldBlockPos + " is not loaded");
                        return false;
                    }

                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                    boolean valid = !state.isAir();
                //    System.out.println("Ship validation - shipLocal: " + localPos + " -> worldBlock: " + worldBlockPos + " -> " + (valid ? "valid" : "air"));
                    return valid;
                }
            }
        } catch (Exception e) {
          //  System.err.println("Error checking attachment point validity for shipId " + shipId + ", localPos " + localPos + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }






}
