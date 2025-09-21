package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.client.NetworkHandler;
import yay.evy.everest.vstuff.content.constraint.ConstraintPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    public static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();
    private static final Map<Integer, String> constraintToPersistenceId = new ConcurrentHashMap<>();
    private static long lastJoinTime = 0L;


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
        public final BlockPos anchorBlockPosA;
        public final BlockPos anchorBlockPosB;
        public final boolean isShipA;
        public final boolean isShipB;

        public enum ConstraintType {
            ROPE_PULLEY,
            GENERIC
        }

        // This is the primary constructor. Keep this one.
        public RopeConstraintData(ServerLevel level, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
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
            this.anchorBlockPosA = null;
            this.anchorBlockPosB = null;

            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable().get(VSGameUtilsKt.getDimensionId(level));
            this.isShipA = !shipA.equals(groundBodyId);
            this.isShipB = !shipB.equals(groundBodyId);
        }

        // This is the old constructor, which now calls the main constructor
        public RopeConstraintData(ServerLevel level, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce) {
            this(level, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, ConstraintType.GENERIC, null);
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

        RopeConstraintData data = new RopeConstraintData(level, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, constraintType, sourceBlockPos);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = java.util.UUID.randomUUID().toString();

        constraintToPersistenceId.put(constraintId, persistenceId);

        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level, constraintType, sourceBlockPos);
        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added " + constraintType + " constraint " + constraintId + " with source block " + sourceBlockPos);
    }


    public static void addConstraintWithPersistence(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                                    Vector3d localPosA, Vector3d localPosB, double maxLength,
                                                    double compliance, double maxForce) {
        addConstraintWithPersistence(level, constraintId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                RopeConstraintData.ConstraintType.GENERIC, null);
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
        NetworkHandler.sendClearAllConstraintsToPlayer(player);

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


    public static void addConstraintToTracker(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce,
                                              RopeConstraintData.ConstraintType constraintType,
                                              net.minecraft.core.BlockPos sourceBlockPos) {
        if (activeConstraints.containsKey(constraintId)) {
            System.out.println("Constraint " + constraintId + " already exists in tracker, skipping");
            return;
        }

        RopeConstraintData data = new RopeConstraintData(level, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, constraintType, sourceBlockPos);
        activeConstraints.put(constraintId, data);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        //System.out.println("Added " + constraintType + " constraint " + constraintId + " to tracker (restoration) with source block " + sourceBlockPos);
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
            // Sync all constraints to player
            NetworkHandler.sendClearAllConstraintsToPlayer(player);
            syncAllConstraintsToPlayer(player);

            // Update the last join time to delay the cleanup process
            lastJoinTime = System.currentTimeMillis();
            System.out.println("Player joined, setting lastJoinTime for delayed cleanup.");
        }
    }


    public static void cleanupOrphanedConstraints(ServerLevel level, net.minecraft.core.BlockPos sourceBlockPos) {
        // System.out.println("Cleaning up orphaned constraints for block at " + sourceBlockPos);

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            if (data.constraintType == RopeConstraintData.ConstraintType.ROPE_PULLEY &&
                    data.sourceBlockPos != null &&
                    data.sourceBlockPos.equals(sourceBlockPos)) {

                //   System.out.println("Found orphaned constraint " + constraintId + " for block " + sourceBlockPos);
                constraintsToRemove.add(constraintId);
            }
        }

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
            Vector3d worldPosA = data.getWorldPosA(level, 0.0f);
            net.minecraft.core.BlockPos blockPosA = new net.minecraft.core.BlockPos(
                    (int) Math.floor(worldPosA.x),
                    (int) Math.floor(worldPosA.y),
                    (int) Math.floor(worldPosA.z)
            );

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
            return false;
        }
    }

    public static void validateAndCleanupConstraints(ServerLevel level) {
        // Skip validation for 15 seconds after a player joins
        if (System.currentTimeMillis() - lastJoinTime < 15000) return;

        java.util.List<Integer> constraintsToRemove = new java.util.ArrayList<>();

        Long groundBodyId;
        try {
            groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
        } catch (Exception e) {
            return; // Cannot validate without ground body
        }
        if (groundBodyId == null) return;

        long currentTime = System.currentTimeMillis();

        // --- Process delayed validations ---
        java.util.List<Integer> delayedToProcess = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Long> entry : delayedValidations.entrySet()) {
            if (currentTime >= entry.getValue()) delayedToProcess.add(entry.getKey());
        }

        for (Integer constraintId : delayedToProcess) {
            delayedValidations.remove(constraintId);
            RopeConstraintData data = activeConstraints.get(constraintId);
            if (data == null) continue;

            boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
            boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

            if (!shipAExists || !shipBExists) {
                constraintsToRemove.add(constraintId);
            }
        }

        // --- Validate active constraints ---
        for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
            Integer constraintId = entry.getKey();
            RopeConstraintData data = entry.getValue();

            // Skip if already scheduled for delayed validation
            if (delayedValidations.containsKey(constraintId)) continue;

            boolean shipAExists = isShipValid(level, data.shipA, groundBodyId);
            boolean shipBExists = isShipValid(level, data.shipB, groundBodyId);

            if (!shipAExists || !shipBExists) {
                // Ship missing → schedule delayed validation instead of removing
                scheduleDelayedValidation(level, constraintId, 5000);
                continue;
            }

            // Skip validation if chunks are not loaded
            if (!areAttachmentChunksLoaded(level, data, groundBodyId)) continue;

            boolean validA = isValidAttachmentPoint(level, data.localPosA, data.shipA, groundBodyId, data.isShipA);
            boolean validB = isValidAttachmentPoint(level, data.localPosB, data.shipB, groundBodyId, data.isShipB);

            if (!validA || !validB) {
                // Invalid attachment → schedule delayed validation
                scheduleDelayedValidation(level, constraintId, 5000);
            }
        }

        // --- Remove constraints marked for removal by delayed validation ---
        for (Integer constraintId : constraintsToRemove) {
            try {
                VSGameUtilsKt.getShipObjectWorld(level).removeConstraint(constraintId);
                removeConstraintWithPersistence(level, constraintId);
            } catch (Exception ignored) {}
        }
    }
    public static boolean constraintExists(ServerLevel level, Integer constraintId) {
        if (constraintId == null) return false;

        try {
            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

            return getActiveConstraints().containsKey(constraintId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safe attachment point validation:
     * - Returns true if the block exists, or if chunk is not loaded
     * - Uses a small epsilon to avoid false invalidation
     */

    private static boolean isValidAttachmentPoint(ServerLevel level, Vector3d localPos, Long shipId, Long groundBodyId, boolean isShip) {
        try {
            if (!isShip) { // If it's a world attachment point
                // Logic for a world-side attachment point. No transformations needed.
                net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                        (int) Math.floor(localPos.x),
                        (int) Math.floor(localPos.y),
                        (int) Math.floor(localPos.z)
                );
                if (!level.isLoaded(blockPos)) return true;
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(blockPos);
                return !state.isAir();
            } else { // If it's a ship attachment point
                // Logic for a ship-side attachment point. Transformation is needed.
                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) return false;
                Vector3d worldPos = new Vector3d();
                ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                net.minecraft.core.BlockPos worldBlockPos = new net.minecraft.core.BlockPos(
                        (int) Math.floor(worldPos.x),
                        (int) Math.floor(worldPos.y),
                        (int) Math.floor(worldPos.z)
                );
                if (!level.isLoaded(worldBlockPos)) return true;
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(worldBlockPos);
                return !state.isAir();
            }
        } catch (Exception e) {
            return true;
        }
    }




}