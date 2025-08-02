package yay.evy.everest.vstuff.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.client.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    public static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();
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
        public final net.minecraft.core.BlockPos sourceBlockPos;


        public enum ConstraintType {
            ROPE_PULLEY,
            GENERIC
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
            this.constraintType = ConstraintType.GENERIC;
            this.sourceBlockPos = null;
        }


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
                                                    double compliance, double maxForce,
                                                    RopeConstraintData.ConstraintType constraintType,
                                                    net.minecraft.core.BlockPos sourceBlockPos) {

        // ADD THIS: Check for existing constraints for the same source block
        if (constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY && sourceBlockPos != null) {
            boolean existingConstraintFound = activeConstraints.values().stream()
                    .anyMatch(existing -> existing.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY
                            && existing.sourceBlockPos != null
                            && existing.sourceBlockPos.equals(sourceBlockPos));

            if (existingConstraintFound) {
                System.out.println("Constraint already exists for rope pulley at " + sourceBlockPos + ", not creating duplicate");
                return;
            }
        }

        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, constraintType, sourceBlockPos);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = java.util.UUID.randomUUID().toString();

        // Map the persistence ID BEFORE adding to persistence to avoid race conditions
        constraintToPersistenceId.put(constraintId, persistenceId);

        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level, constraintType, sourceBlockPos);
        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added " + constraintType + " constraint " + constraintId + " with source block " + sourceBlockPos);
    }


    // Keep the old method for backward compatibility
    public static void addConstraintWithPersistence(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                                    Vector3d localPosA, Vector3d localPosB, double maxLength,
                                                    double compliance, double maxForce) {
        addConstraintWithPersistence(level, constraintId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                RopeConstraintData.ConstraintType.GENERIC, null);
    }


    private static boolean isRopePulleyBlock(net.minecraft.world.level.block.state.BlockState state) {
        // Replace this with your actual rope pulley block check
        // For example: return state.getBlock() instanceof RopePulleyBlock;
        return state.getBlock().toString().contains("rope_pulley"); // Temporary implementation
    }

    public static boolean constraintExists(ServerLevel level, Integer constraintId) {
        if (constraintId == null) return false;

        try {
            // Check if constraint exists in the physics world
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            // You'll need to implement this check based on your constraint tracking system
            // This is a placeholder - implement according to your ConstraintTracker design
            return getActiveConstraints().containsKey(constraintId);
        } catch (Exception e) {
            return false;
        }
    }

    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {
        RopeConstraintData data = activeConstraints.remove(constraintId);
        if (data != null) {
            VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);

            ConstraintPersistence persistence = ConstraintPersistence.get(level);
            String persistenceId = constraintToPersistenceId.remove(constraintId);
            if (persistenceId != null) {
                persistence.markConstraintAsRemoved(persistenceId);
                persistence.setDirty();
            }

            if (level.getServer() != null) {
                for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                    NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    level.getServer().tell(new net.minecraft.server.TickTask(0, () -> {
                        NetworkHandler.sendConstraintRemoveToPlayer(player, constraintId);
                    }));
                }
            }

            NetworkHandler.sendConstraintRemove(constraintId);

            if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY && data.sourceBlockPos != null) {
                cleanupOrphanedConstraints(level, data.sourceBlockPos);
            }
        }
    }


    public static void syncAllConstraintsToPlayer(ServerPlayer player) {
        // First clear their client state
        NetworkHandler.sendClearAllConstraintsToPlayer(player);

        // Then send all active constraints
        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            RopeConstraintData data = entry.getValue();
            NetworkHandler.sendConstraintAddToPlayer(
                    player,
                    entry.getKey(),
                    data.shipA,
                    data.shipB,
                    data.localPosA,
                    data.localPosB,
                    data.maxLength
            );
        }
    }



    public static void mapConstraintToPersistenceId(Integer constraintId, String persistenceId) {
        constraintToPersistenceId.put(constraintId, persistenceId);
     //   System.out.println("Mapped constraint " + constraintId + " to persistence ID " + persistenceId);
    }

    public static Map<Integer, RopeConstraintData> getActiveConstraints() {
        return new HashMap<>(activeConstraints);
    }


    public static void addConstraintToTracker(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce,
                                              RopeConstraintData.ConstraintType constraintType,
                                              net.minecraft.core.BlockPos sourceBlockPos) {
        // Check if constraint already exists
        if (activeConstraints.containsKey(constraintId)) {
            System.out.println("Constraint " + constraintId + " already exists in tracker, skipping");
            return;
        }

        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, constraintType, sourceBlockPos);
        activeConstraints.put(constraintId, data);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        //System.out.println("Added " + constraintType + " constraint " + constraintId + " to tracker (restoration) with source block " + sourceBlockPos);
    }

    // Keep the old method for backward compatibility
    public static void addConstraintToTracker(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce) {
        addConstraintToTracker(constraintId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                RopeConstraintData.ConstraintType.GENERIC, null);
    }





    private static boolean isShipValid(ServerLevel level, Long shipId, Long groundBodyId) {
        if (shipId == null) return false;

        if (shipId.equals(groundBodyId)) {
            return true;
        }

        try {
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);
            var ship = shipWorld.getAllShips().getById(shipId);
            boolean exists = ship != null;

            if (!exists) {
              //  System.out.println("Ship " + shipId + " not found in ship world");
                // Try alternative lookup methods
                var allShips = shipWorld.getAllShips();
                //System.out.println("Available ships: " + allShips.stream().map(s -> s.getId()).toList());
            }

            return exists;
        } catch (Exception e) {
            System.err.println("Exception checking ship validity for " + shipId + ": " + e.getMessage());
            return false;
        }
    }

    private static final Map<Integer, Long> delayedValidations = new ConcurrentHashMap<>();

    private static void scheduleDelayedValidation(ServerLevel level, Integer constraintId, long delayMs) {
        delayedValidations.put(constraintId, System.currentTimeMillis() + delayMs);
      //  System.out.println("Scheduled delayed validation for constraint " + constraintId + " in " + (delayMs/1000) + " seconds");
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
           // System.out.println("Synced " + activeConstraints.size() + " constraints to player " + player.getName().getString());
        }
    }
    public static void validateAndCleanupConstraints(ServerLevel level) {
      //  System.out.println("=== CONSTRAINT VALIDATION START ===");
      //  System.out.println("Validating " + activeConstraints.size() + " active constraints...");
        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        Long groundBodyId = null;
        try {
            groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
         //   System.out.println("Ground body ID: " + groundBodyId);
        } catch (Exception e) {
           // System.err.println("Failed to get ground body ID, skipping validation: " + e.getMessage());
            return;
        }

        if (groundBodyId == null) {
           // System.err.println("Ground body ID is null, skipping validation");
            return;
        }

        // Process delayed validations first
        long currentTime = System.currentTimeMillis();
        java.util.List<Integer> delayedToProcess = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Long> entry : delayedValidations.entrySet()) {
            if (currentTime >= entry.getValue()) {
                delayedToProcess.add(entry.getKey());
            }
        }

        for (Integer constraintId : delayedToProcess) {
            delayedValidations.remove(constraintId);
            if (activeConstraints.containsKey(constraintId)) {
             //   System.out.println("Processing delayed validation for constraint " + constraintId);
                RopeConstraintData data = activeConstraints.get(constraintId);
                boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
                boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

                if (!shipAExists || !shipBExists) {
                //    System.out.println("Delayed validation failed - removing constraint " + constraintId);
                    constraintsToRemove.add(constraintId);
                }
            }
        }

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            // Skip if already scheduled for delayed validation
            if (delayedValidations.containsKey(constraintId)) {
                continue;
            }

            try {
                boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
                boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

                if (!shipAExists || !shipBExists) {
                //    System.out.println("Constraint " + constraintId + " references missing ships - scheduling delayed validation");
                    scheduleDelayedValidation(level, constraintId, 5000);
                    continue;
                }

                // For rope pulleys, validate the source block instead of attachment points
                if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY) {
                    if (data.sourceBlockPos != null) {
                        if (!level.isLoaded(data.sourceBlockPos)) {
                            System.out.println("Rope pulley constraint " + constraintId + " source block chunk not loaded - skipping validation");
                            continue;
                        }

                        net.minecraft.world.level.block.state.BlockState sourceState = level.getBlockState(data.sourceBlockPos);
                        // Check if it's still a rope pulley block (you'll need to replace this with your actual pulley block check)
                        if (sourceState.isAir() || !isRopePulleyBlock(sourceState)) {
                        //    System.out.println("Rope pulley constraint " + constraintId + " source block is no longer valid - marking for removal");
                            constraintsToRemove.add(constraintId);
                        }
                    } else {
                    //    System.out.println("Rope pulley constraint " + constraintId + " has no source block position - marking for removal");
                        constraintsToRemove.add(constraintId);
                    }
                    continue;
                }

                // For generic constraints, check if chunks are loaded before validating attachment points
                if (!areAttachmentChunksLoaded(level, data, groundBodyId)) {
               //     System.out.println("Constraint " + constraintId + " has attachment points in unloaded chunks - skipping validation");
                    continue;
                }

                boolean validA = isValidAttachmentPoint(level, data.localPosA, data.shipA, groundBodyId);
                boolean validB = isValidAttachmentPoint(level, data.localPosB, data.shipB, groundBodyId);
                boolean isValid = validA && validB;

                if (!isValid) {
                //    System.out.println("Generic constraint " + constraintId + " is invalid - marking for removal");
                    constraintsToRemove.add(constraintId);
                }
            } catch (Exception e) {
             //   System.err.println("Error validating constraint " + constraintId + ": " + e.getMessage());
                if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY) {
                    // For rope pulleys, schedule delayed validation instead of immediate removal
                    scheduleDelayedValidation(level, constraintId, 5000);
                } else {
                    scheduleDelayedValidation(level, constraintId, 5000);
                }
            }
        }

        for (Integer constraintId : constraintsToRemove) {
            try {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
            } catch (Exception e) {
        //        System.err.println("Error removing invalid constraint " + constraintId + ": " + e.getMessage());
            }
        }

       // System.out.println("=== CONSTRAINT VALIDATION END ===");
      //  System.out.println("Constraint validation complete. Removed " + constraintsToRemove.size() + " invalid constraints.");
    }

    // Add this method to the ConstraintTracker class
    public static void cleanupOrphanedConstraints(ServerLevel level, net.minecraft.core.BlockPos sourceBlockPos) {
       // System.out.println("Cleaning up orphaned constraints for block at " + sourceBlockPos);

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            // Check if this constraint is associated with the given block position
            if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY &&
                    data.sourceBlockPos != null &&
                    data.sourceBlockPos.equals(sourceBlockPos)) {

             //   System.out.println("Found orphaned constraint " + constraintId + " for block " + sourceBlockPos);
                constraintsToRemove.add(constraintId);
            }
        }

        // Remove the orphaned constraints
        for (Integer constraintId : constraintsToRemove) {
            try {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
             //   System.out.println("Cleaned up orphaned constraint " + constraintId);
            } catch (Exception e) {
             //   System.err.println("Error cleaning up orphaned constraint " + constraintId + ": " + e.getMessage());
            }
        }
    }


    private static boolean areAttachmentChunksLoaded(ServerLevel level, RopeConstraintData data, Long groundBodyId) {
        try {
            // Check chunk loading for attachment point A
            Vector3d worldPosA = data.getWorldPosA(level, 0.0f);
            net.minecraft.core.BlockPos blockPosA = new net.minecraft.core.BlockPos(
                    (int) Math.floor(worldPosA.x),
                    (int) Math.floor(worldPosA.y),
                    (int) Math.floor(worldPosA.z)
            );

            // Check chunk loading for attachment point B
            Vector3d worldPosB = data.getWorldPosB(level, 0.0f);
            net.minecraft.core.BlockPos blockPosB = new net.minecraft.core.BlockPos(
                    (int) Math.floor(worldPosB.x),
                    (int) Math.floor(worldPosB.y),
                    (int) Math.floor(worldPosB.z)
            );

            boolean chunkALoaded = level.isLoaded(blockPosA);
            boolean chunkBLoaded = level.isLoaded(blockPosB);

            return chunkALoaded && chunkBLoaded;
        } catch (Exception e) {
            System.err.println("Error checking chunk loading status: " + e.getMessage());
            return false; // Assume not loaded if we can't check
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

                // Critical fix: Don't validate if chunk isn't loaded
                if (!level.isLoaded(blockPos)) {
                    System.out.println("World block at " + blockPos + " is not loaded - skipping validation");
                    return true; // Assume valid if chunk not loaded, don't remove constraint
                }

                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(blockPos);
                boolean valid = !state.isAir();
                return valid;
            } else {
                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) {
                    return false;
                }

                boolean looksLikeWorldCoords = Math.abs(localPos.x) > 1000000 || Math.abs(localPos.z) > 1000000;

                if (looksLikeWorldCoords) {
                    Vector3d actualShipLocal = new Vector3d();
                    ship.getTransform().getWorldToShip().transformPosition(localPos, actualShipLocal);

                    Vector3d worldPos = new Vector3d();
                    ship.getTransform().getShipToWorld().transformPosition(actualShipLocal, worldPos);
                    net.minecraft.core.BlockPos worldBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(worldPos.x),
                            (int) Math.floor(worldPos.y),
                            (int) Math.floor(worldPos.z)
                    );

                    // Critical fix: Don't validate if chunk isn't loaded
                    if (!level.isLoaded(worldBlockPos)) {
                        System.out.println("Ship block world position " + worldBlockPos + " is not loaded - skipping validation");
                        return true; // Assume valid if chunk not loaded, don't remove constraint
                    }

                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                    boolean valid = !state.isAir();
                    return valid;
                } else {
                    Vector3d worldPos = new Vector3d();
                    ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                    net.minecraft.core.BlockPos worldBlockPos = new net.minecraft.core.BlockPos(
                            (int) Math.floor(worldPos.x),
                            (int) Math.floor(worldPos.y),
                            (int) Math.floor(worldPos.z)
                    );

                    // Critical fix: Don't validate if chunk isn't loaded
                    if (!level.isLoaded(worldBlockPos)) {
                    //    System.out.println("Ship block world position " + worldBlockPos + " is not loaded - skipping validation");
                        return true; // Assume valid if chunk not loaded, don't remove constraint
                    }

                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                    boolean valid = !state.isAir();
                    return valid;
                }
            }
        } catch (Exception e) {
            //System.err.println("Error checking attachment point validity for shipId " + shipId + ", localPos " + localPos + ": " + e.getMessage());
            return true; // Assume valid on error to prevent accidental removal
        }
    }








}
