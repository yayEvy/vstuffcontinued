package yay.evy.everest.vstuff.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3d;
import org.valkyrienskies.core.apigame.constraints.VSRopeConstraint;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConstraintPersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_constraints";
    private final Set<String> removedConstraints = new HashSet<>();


    private final Map<String, PersistedConstraintData> persistedConstraints = new HashMap<>();
    private final Set<String> restoredConstraints = new HashSet<>();
    private boolean hasAttemptedRestore = false;

    private boolean cleanupScheduled = false;
    private int ticksUntilCleanup = 0;
    private ServerLevel cleanupLevel = null;

    public static class PersistedConstraintData {
        public Long shipA;
        public Long shipB;
        public Vector3d localPosA;
        public Vector3d localPosB;
        public double maxLength;
        public double compliance;
        public double maxForce;
        public boolean shipAIsGround;
        public boolean shipBIsGround;
        public ConstraintTracker.RopeConstraintData.ConstraintType constraintType;
        public net.minecraft.core.BlockPos sourceBlockPos;

        public PersistedConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                       double maxLength, double compliance, double maxForce,
                                       boolean shipAIsGround, boolean shipBIsGround) {
            this(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                    shipAIsGround, shipBIsGround, ConstraintTracker.RopeConstraintData.ConstraintType.GENERIC, null);
        }

        public PersistedConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                       double maxLength, double compliance, double maxForce,
                                       boolean shipAIsGround, boolean shipBIsGround,
                                       ConstraintTracker.RopeConstraintData.ConstraintType constraintType,
                                       net.minecraft.core.BlockPos sourceBlockPos) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.shipAIsGround = shipAIsGround;
            this.shipBIsGround = shipBIsGround;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
            this.constraintType = constraintType != null ? constraintType : ConstraintTracker.RopeConstraintData.ConstraintType.GENERIC;
            this.sourceBlockPos = sourceBlockPos;
        }
    }

    public static ConstraintPersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ConstraintPersistence::load, ConstraintPersistence::new, DATA_NAME);
    }
    public void markConstraintAsRemoved(String id) {
        removedConstraints.add(id);
        persistedConstraints.remove(id);
        restoredConstraints.remove(id);
        setDirty();
        System.out.println("Marked constraint as permanently removed: " + id);
        System.out.println("Removed constraints now contains: " + removedConstraints.size() + " entries");
        System.out.println("Persisted constraints now contains: " + persistedConstraints.size() + " entries");
    }

    public static ConstraintPersistence load(CompoundTag tag) {
        ConstraintPersistence data = new ConstraintPersistence();
        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);
        ListTag removedList = tag.getList("removedConstraints", Tag.TAG_STRING); // ✅ now matches save()

        // Load removed constraints first
        for (int i = 0; i < removedList.size(); i++) {
            data.removedConstraints.add(removedList.getString(i));
        }

        for (int i = 0; i < constraintsList.size(); i++) {
            CompoundTag constraintTag = constraintsList.getCompound(i);
            String id = constraintTag.getString("id");

            // Skip loading removed constraints entirely
            if (data.removedConstraints.contains(id)) {
                System.out.println("Skipping removed constraint during load: " + id);
                continue;
            }

            long shipALong = constraintTag.getLong("shipA");
            long shipBLong = constraintTag.getLong("shipB");
            Long shipA = shipALong == 0L ? null : shipALong;
            Long shipB = shipBLong == 0L ? null : shipBLong;

            boolean shipAIsGround = constraintTag.getBoolean("shipAIsGround");
            boolean shipBIsGround = constraintTag.getBoolean("shipBIsGround");

            Vector3d localPosA = new Vector3d(
                    constraintTag.getDouble("localPosA_x"),
                    constraintTag.getDouble("localPosA_y"),
                    constraintTag.getDouble("localPosA_z")
            );
            Vector3d localPosB = new Vector3d(
                    constraintTag.getDouble("localPosB_x"),
                    constraintTag.getDouble("localPosB_y"),
                    constraintTag.getDouble("localPosB_z")
            );

            double maxLength = constraintTag.getDouble("maxLength");
            double compliance = constraintTag.getDouble("compliance");
            double maxForce = constraintTag.getDouble("maxForce");

            // Load constraint type (default to GENERIC for backward compatibility)
            ConstraintTracker.RopeConstraintData.ConstraintType constraintType =
                    ConstraintTracker.RopeConstraintData.ConstraintType.GENERIC;
            if (constraintTag.contains("constraintType")) {
                try {
                    constraintType = ConstraintTracker.RopeConstraintData.ConstraintType.valueOf(
                            constraintTag.getString("constraintType"));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid constraint type in save data, defaulting to GENERIC: " + e.getMessage());
                }
            }

            // Load source block position if present
            net.minecraft.core.BlockPos sourceBlockPos = null;
            if (constraintTag.contains("sourceBlockPos_x")) {
                sourceBlockPos = new net.minecraft.core.BlockPos(
                        constraintTag.getInt("sourceBlockPos_x"),
                        constraintTag.getInt("sourceBlockPos_y"),
                        constraintTag.getInt("sourceBlockPos_z")
                );
            }

            System.out.println("LOADING FROM NBT - ID: " + id +
                    ", shipA: " + shipA + ", shipB: " + shipB +
                    ", shipAIsGround: " + shipAIsGround + ", shipBIsGround: " + shipBIsGround +
                    ", constraintType: " + constraintType + ", sourceBlockPos: " + sourceBlockPos);

            data.persistedConstraints.put(id, new PersistedConstraintData(
                    shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                    shipAIsGround, shipBIsGround, constraintType, sourceBlockPos
            ));
        }

        System.out.println("Loaded " + data.persistedConstraints.size() + " persisted constraints");
        return data;
    }



    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag constraintsList = new ListTag();
        ListTag removedList = new ListTag();

        for (String removedId : removedConstraints) {
            removedList.add(net.minecraft.nbt.StringTag.valueOf(removedId));
        }
        tag.put("removedConstraints", removedList);

        for (Map.Entry<String, PersistedConstraintData> entry : persistedConstraints.entrySet()) {
            String constraintId = entry.getKey();

            // Double-check: don't save constraints that are marked as removed
            if (removedConstraints.contains(constraintId)) {
                System.out.println("Skipping removed constraint during save: " + constraintId);
                continue;
            }

            CompoundTag constraintTag = new CompoundTag();
            PersistedConstraintData data = entry.getValue();

            constraintTag.putString("id", constraintId);
            constraintTag.putLong("shipA", data.shipA != null ? data.shipA : 0L);
            constraintTag.putLong("shipB", data.shipB != null ? data.shipB : 0L);
            constraintTag.putBoolean("shipAIsGround", data.shipAIsGround);
            constraintTag.putBoolean("shipBIsGround", data.shipBIsGround);

            constraintTag.putDouble("localPosA_x", data.localPosA.x);
            constraintTag.putDouble("localPosA_y", data.localPosA.y);
            constraintTag.putDouble("localPosA_z", data.localPosA.z);
            constraintTag.putDouble("localPosB_x", data.localPosB.x);
            constraintTag.putDouble("localPosB_y", data.localPosB.y);
            constraintTag.putDouble("localPosB_z", data.localPosB.z);

            constraintTag.putDouble("maxLength", data.maxLength);
            constraintTag.putDouble("compliance", data.compliance);
            constraintTag.putDouble("maxForce", data.maxForce);

            // Save constraint type
            constraintTag.putString("constraintType", data.constraintType.name());

            // Save source block position if present
            if (data.sourceBlockPos != null) {
                constraintTag.putInt("sourceBlockPos_x", data.sourceBlockPos.getX());
                constraintTag.putInt("sourceBlockPos_y", data.sourceBlockPos.getY());
                constraintTag.putInt("sourceBlockPos_z", data.sourceBlockPos.getZ());
            }

            System.out.println("SAVING TO NBT - ID: " + constraintId +
                    ", shipA: " + data.shipA + ", shipB: " + data.shipB +
                    ", shipAIsGround: " + data.shipAIsGround + ", shipBIsGround: " + data.shipBIsGround +
                    ", constraintType: " + data.constraintType + ", sourceBlockPos: " + data.sourceBlockPos);

            constraintsList.add(constraintTag);
        }

        tag.put("constraints", constraintsList);
        System.out.println("Saved " + persistedConstraints.size() + " constraints to disk");
        return tag;
    }


    public void addConstraint(String id, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                              double maxLength, double compliance, double maxForce, ServerLevel level,
                              ConstraintTracker.RopeConstraintData.ConstraintType constraintType,
                              net.minecraft.core.BlockPos sourceBlockPos) {
        boolean shipAIsGround = false;
        boolean shipBIsGround = false;

        try {
            Long currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            shipAIsGround = shipA != null && shipA.equals(currentGroundBodyId);
            shipBIsGround = shipB != null && shipB.equals(currentGroundBodyId);
        } catch (Exception e) {
            System.err.println("Failed to check ground body IDs: " + e.getMessage());
        }

        persistedConstraints.put(id, new PersistedConstraintData(
                shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                shipAIsGround, shipBIsGround, constraintType, sourceBlockPos
        ));
        setDirty();
    }








    public void removeConstraint(String id) {
        if (persistedConstraints.remove(id) != null) {
            restoredConstraints.remove(id);
            markConstraintAsRemoved(id);
            setDirty();
    //        System.out.println("Removed constraint from persistence: " + id);
        }
    }

    public Map<String, PersistedConstraintData> getAllConstraints() {
        return new HashMap<>(persistedConstraints);
    }

    private void restoreLoadedChunkConstraints(ServerLevel level, Long currentGroundBodyId) {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        Map<String, PersistedConstraintData> constraintsCopy = new HashMap<>(persistedConstraints);
        for (Map.Entry<String, PersistedConstraintData> entry : constraintsCopy.entrySet()) {
            String persistenceId = entry.getKey();
            PersistedConstraintData data = entry.getValue();

            // Skip removed constraints
            if (removedConstraints.contains(persistenceId)) {
                skipCount++;
                continue;
            }

            // Skip already restored constraints
            if (restoredConstraints.contains(persistenceId)) {
                skipCount++;
                continue;
            }

            // Check if constraint is in loaded chunks
            if (!isConstraintInLoadedChunks(level, data, currentGroundBodyId)) {
                System.out.println("Constraint " + persistenceId + " is not in loaded chunks, will restore when chunks load");
                skipCount++;
                continue;
            }

            // Rest of the restoration logic remains the same...
            // [Keep the existing restoration code from the original method]
        }

        System.out.println("Initial constraint restoration complete: " + successCount + " success, " +
                failCount + " failed, " + skipCount + " skipped (will restore when chunks load)");
    }

    private boolean isConstraintInLoadedChunks(ServerLevel level, PersistedConstraintData data, Long groundBodyId) {
        try {
            // For rope pulleys, check if source block chunk is loaded
            if (data.constraintType == ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY
                    && data.sourceBlockPos != null) {
                return level.isLoaded(data.sourceBlockPos);
            }

            // For generic constraints, check if either attachment point chunk is loaded
            boolean pointALoaded = isPositionChunkLoaded(level, data.localPosA, data.shipA, data.shipAIsGround, groundBodyId);
            boolean pointBLoaded = isPositionChunkLoaded(level, data.localPosB, data.shipB, data.shipBIsGround, groundBodyId);

            return pointALoaded && pointBLoaded;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPositionChunkLoaded(ServerLevel level, Vector3d localPos, Long shipId, boolean isGround, Long groundBodyId) {
        try {
            Vector3d worldPos;

            if (isGround) {
                worldPos = new Vector3d(localPos);
            } else if (shipId != null) {
                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) {
                    return false;
                }
                worldPos = new Vector3d();
                ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
            } else {
                return false;
            }

            net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                    (int) Math.floor(worldPos.x),
                    (int) Math.floor(worldPos.y),
                    (int) Math.floor(worldPos.z)
            );

            return level.isLoaded(blockPos);
        } catch (Exception e) {
            return false;
        }
    }


    public static void restoreConstraints(ServerLevel level) {
        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        persistence.restoreConstraintsInstance(level);
    }

    public void restoreConstraintsInstance(ServerLevel level) {
        Long currentGroundBodyId;
        try {
            currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
        } catch (Exception e) {
            System.err.println("Failed to get ground body ID: " + e.getMessage());
            return;
        }

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (Map.Entry<String, PersistedConstraintData> entry : persistedConstraints.entrySet()) {
            String persistenceId = entry.getKey();
            PersistedConstraintData data = entry.getValue();

            if (removedConstraints.contains(persistenceId)) {
                skipCount++;
                continue;
            }

            Long actualShipA = data.shipAIsGround ? currentGroundBodyId : data.shipA;
            Long actualShipB = data.shipBIsGround ? currentGroundBodyId : data.shipB;

            boolean shipAValid = data.shipAIsGround || (actualShipA != null &&
                    VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipA) != null);
            boolean shipBValid = data.shipBIsGround || (actualShipB != null &&
                    VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipB) != null);

            if (!shipAValid || !shipBValid) {
                System.out.println("Skipping constraint " + persistenceId + " due to invalid ships: " + actualShipA + ", " + actualShipB);
                skipCount++;
                continue;
            }

            VSRopeConstraint ropeConstraint = new VSRopeConstraint(actualShipA, actualShipB, data.compliance, data.localPosA, data.localPosB, data.maxForce, data.maxLength);
            Integer newConstraintId = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint(ropeConstraint);

            if (newConstraintId != null) {
                ConstraintTracker.addConstraintToTracker(newConstraintId, actualShipA, actualShipB, data.localPosA, data.localPosB, data.maxLength, data.compliance, data.maxForce, data.constraintType, data.sourceBlockPos);
                ConstraintTracker.mapConstraintToPersistenceId(newConstraintId, persistenceId);
                restoredConstraints.add(persistenceId);
                successCount++;
            } else {
                System.out.println("Failed to create constraint for " + persistenceId);
                failCount++;
            }
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            ConstraintTracker.syncAllConstraintsToPlayer(player);
        }

        System.out.println("Restoration complete: " + successCount + " success, " + failCount + " failed, " + skipCount + " skipped");
    }



    private Long resolveShipId(ServerLevel level, PersistedConstraintData data, boolean isShipA, Long currentGroundBodyId) {
        boolean isGround = isShipA ? data.shipAIsGround : data.shipBIsGround;
        Long originalShipId = isShipA ? data.shipA : data.shipB;

        if (isGround) {
            return currentGroundBodyId;
        }

        if (originalShipId == null) {
            return null;
        }

        // First try the original ship ID
        if (VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(originalShipId) != null) {
            return originalShipId;
        }

        // If original ship ID doesn't work, try to find ship by position
        // This is a fallback for when ship IDs change between sessions
        Vector3d searchPos = isShipA ? data.localPosA : data.localPosB;

        // For rope pulleys, we can use the source block position to find the right ship
        if (data.constraintType == ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY && data.sourceBlockPos != null) {
            try {
                // Check if the source block is on a ship
                org.valkyrienskies.core.api.ships.Ship shipAtBlock = VSGameUtilsKt.getShipManagingPos(level, data.sourceBlockPos);
                if (shipAtBlock != null) {
                    Long shipId = shipAtBlock.getId();
                    System.out.println("Resolved ship ID for rope pulley from " + originalShipId + " to " + shipId + " using source block position");
                    return shipId;
                }
            } catch (Exception e) {
                System.err.println("Error resolving ship ID by source block position: " + e.getMessage());
            }
        }

        System.err.println("Could not resolve ship ID " + originalShipId + " - ship may no longer exist");
        return null;
    }
    private static boolean isRopePulleyBlock(net.minecraft.world.level.block.state.BlockState state) {
        // Replace this with your actual rope pulley block check
        return state.getBlock().toString().contains("rope_pulley");
    }


    private void scheduleDeadConstraintCleanup(ServerLevel level, int delayTicks) {
        cleanupScheduled = true;
        ticksUntilCleanup = delayTicks;
        cleanupLevel = level;
        System.out.println("Scheduled constraint cleanup in " + (delayTicks / 20) + " seconds");
    }

    public void tickCleanup() {
        if (!cleanupScheduled || cleanupLevel == null) {
            return;
        }

        ticksUntilCleanup--;

        if (ticksUntilCleanup <= 0) {
            cleanupScheduled = false;
            cleanupDeadConstraints(cleanupLevel);
            cleanupLevel = null;
        }
    }

    private void cleanupDeadConstraints(ServerLevel level) {
      //  System.out.println("Starting cleanup of dead constraints...");

        try {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));

            if (groundBodyId == null) {
         //       System.err.println("Cannot cleanup - ground body ID is null");
                return;
            }

            java.util.List<String> toRemove = new java.util.ArrayList<>();

            for (Map.Entry<String, PersistedConstraintData> entry : persistedConstraints.entrySet()) {
                String persistenceId = entry.getKey();
                PersistedConstraintData data = entry.getValue();

                if (restoredConstraints.contains(persistenceId)) {
                    continue;
                }

                Long actualShipA = data.shipAIsGround ? groundBodyId : data.shipA;
                Long actualShipB = data.shipBIsGround ? groundBodyId : data.shipB;

                boolean shipAValid = data.shipAIsGround ||
                        (actualShipA != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipA) != null);
                boolean shipBValid = data.shipBIsGround ||
                        (actualShipB != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipB) != null);

                if (!shipAValid || !shipBValid) {
               //     System.out.println("Marking constraint " + persistenceId + " for removal - ships still don't exist");
                    toRemove.add(persistenceId);
                }
            }

            for (String deadId : toRemove) {
                removeConstraint(deadId);
              //  System.out.println("Cleaned up dead constraint: " + deadId);
            }

            if (toRemove.size() > 0) {
             //   System.out.println("Cleaned up " + toRemove.size() + " dead constraints");
            } else {
             //   System.out.println("No dead constraints found during cleanup");
            }

        } catch (Exception e) {
      //      System.err.println("Error during constraint cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
/*
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();

            level.getServer().execute(() -> {
                level.getServer().tell(new net.minecraft.server.TickTask(100, () -> {
                    try {
                        ConstraintPersistence persistence = ConstraintPersistence.get(level);
                        persistence.restoreConstraints(level);
                    } catch (Exception e) {
                        System.err.println("Error during constraint restoration: " + e.getMessage());
                    }
                }));
            });
        }
    }
    */

    public void restoreConstraintsForChunk(ServerLevel level, ChunkPos chunkPos) {
        if (persistedConstraints.isEmpty()) {
            return;
        }

        Long currentGroundBodyId = null;
        try {
            currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
        } catch (Exception e) {
            return;
        }

        if (currentGroundBodyId == null) {
            return;
        }

        int restoredCount = 0;
        Map<String, PersistedConstraintData> constraintsCopy = new HashMap<>(persistedConstraints);

        for (Map.Entry<String, PersistedConstraintData> entry : constraintsCopy.entrySet()) {
            String persistenceId = entry.getKey();
            PersistedConstraintData data = entry.getValue();

            // Enhanced validation checks
            if (removedConstraints.contains(persistenceId) ||
                    restoredConstraints.contains(persistenceId) ||
                    !isConstraintInChunk(level, data, chunkPos, currentGroundBodyId)) {
                continue;
            }

            // Validate ships and create constraint
            Long actualShipA = data.shipAIsGround ? currentGroundBodyId : data.shipA;
            Long actualShipB = data.shipBIsGround ? currentGroundBodyId : data.shipB;

            if (validateShipsAndCreateConstraint(level, persistenceId, data, actualShipA, actualShipB)) {
                restoredCount++;
                restoredConstraints.add(persistenceId);
            }
        }

        if (restoredCount > 0) {
            System.out.println("Restored " + restoredCount + " constraints for chunk " + chunkPos);
        }
    }


    private boolean validateShipsAndCreateConstraint(ServerLevel level, String persistenceId,
                                                     PersistedConstraintData data,
                                                     Long actualShipA, Long actualShipB) {
        try {
            boolean shipAValid = data.shipAIsGround ||
                    (actualShipA != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipA) != null);
            boolean shipBValid = data.shipBIsGround ||
                    (actualShipB != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipB) != null);

            if (!shipAValid || !shipBValid) {
                return false;
            }

            VSRopeConstraint ropeConstraint = new VSRopeConstraint(
                    actualShipA, actualShipB,
                    data.compliance,
                    data.localPosA, data.localPosB,
                    data.maxForce,
                    data.maxLength
            );

            Integer newConstraintId = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint(ropeConstraint);
            if (newConstraintId != null) {
                ConstraintTracker.addConstraintToTracker(
                        newConstraintId, actualShipA, actualShipB,
                        data.localPosA, data.localPosB, data.maxLength,
                        data.compliance, data.maxForce,
                        data.constraintType, data.sourceBlockPos
                );
                ConstraintTracker.mapConstraintToPersistenceId(newConstraintId, persistenceId);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error restoring constraint " + persistenceId + ": " + e.getMessage());
        }
        return false;
    }


    private boolean isConstraintInChunk(ServerLevel level, PersistedConstraintData data, ChunkPos chunkPos, Long groundBodyId) {
        try {
            // For rope pulleys, check if the source block is in this chunk
            if (data.constraintType == ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY
                    && data.sourceBlockPos != null) {
                ChunkPos sourceChunk = new ChunkPos(data.sourceBlockPos);
                return sourceChunk.equals(chunkPos);
            }

            // For generic constraints, check if either attachment point is in this chunk
            boolean pointAInChunk = isPositionInChunk(level, data.localPosA, data.shipA, data.shipAIsGround, chunkPos, groundBodyId);
            boolean pointBInChunk = isPositionInChunk(level, data.localPosB, data.shipB, data.shipBIsGround, chunkPos, groundBodyId);

            return pointAInChunk || pointBInChunk;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPositionInChunk(ServerLevel level, Vector3d localPos, Long shipId, boolean isGround,
                                      ChunkPos chunkPos, Long groundBodyId) {
        try {
            Vector3d worldPos;

            if (isGround) {
                worldPos = new Vector3d(localPos);
            } else if (shipId != null) {
                org.valkyrienskies.core.api.ships.Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (ship == null) {
                    return false;
                }
                worldPos = new Vector3d();
                ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
            } else {
                return false;
            }

            ChunkPos posChunk = new ChunkPos(
                    (int) Math.floor(worldPos.x) >> 4,
                    (int) Math.floor(worldPos.z) >> 4
            );

            return posChunk.equals(chunkPos);
        } catch (Exception e) {
            return false;
        }
    }


}
