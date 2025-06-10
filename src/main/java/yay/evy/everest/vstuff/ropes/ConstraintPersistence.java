package yay.evy.everest.vstuff.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
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
    private final Set<String> restoredConstraints = new HashSet<>(); // Track what we've already restored
    private boolean hasAttemptedRestore = false; // Prevent multiple restore attempts

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
        public boolean shipAIsGround; // Add these flags
        public boolean shipBIsGround;

        public PersistedConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                       double maxLength, double compliance, double maxForce,
                                       boolean shipAIsGround, boolean shipBIsGround) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.shipAIsGround = shipAIsGround;
            this.shipBIsGround = shipBIsGround;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
        }
    }

    public static ConstraintPersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ConstraintPersistence::load, ConstraintPersistence::new, DATA_NAME);
    }
    public void markConstraintAsRemoved(String id) {
        removedConstraints.add(id);
        persistedConstraints.remove(id);  // Remove from persisted constraints
        restoredConstraints.remove(id);   // Remove from restored tracking
        setDirty();
        System.out.println("Marked constraint as permanently removed: " + id);
        System.out.println("Removed constraints now contains: " + removedConstraints.size() + " entries");
        System.out.println("Persisted constraints now contains: " + persistedConstraints.size() + " entries");
    }

    public static ConstraintPersistence load(CompoundTag tag) {
        ConstraintPersistence data = new ConstraintPersistence();
        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);


        ListTag removedList = tag.getList("removedConstraints", Tag.TAG_STRING);
        for (int i = 0; i < removedList.size(); i++) {
            data.removedConstraints.add(removedList.getString(i));
        }

        for (int i = 0; i < constraintsList.size(); i++) {
            CompoundTag constraintTag = constraintsList.getCompound(i);
            String id = constraintTag.getString("id");
            long shipALong = constraintTag.getLong("shipA");
            long shipBLong = constraintTag.getLong("shipB");
            Long shipA = shipALong == 0L ? null : shipALong;
            Long shipB = shipBLong == 0L ? null : shipBLong;

            if (data.removedConstraints.contains(id)) {
                System.out.println("Skipping removed constraint during load: " + id);
                continue;
            }

            // Load the ground flags (default to false for backwards compatibility)
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

            System.out.println("LOADING FROM NBT - ID: " + id +
                    ", shipA: " + shipA + ", shipB: " + shipB +
                    ", shipAIsGround: " + shipAIsGround + ", shipBIsGround: " + shipBIsGround);

            data.persistedConstraints.put(id, new PersistedConstraintData(
                    shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce,
                    shipAIsGround, shipBIsGround  // Add the boolean parameters
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
            CompoundTag constraintTag = new CompoundTag();
            PersistedConstraintData data = entry.getValue();

            constraintTag.putString("id", entry.getKey());
            constraintTag.putLong("shipA", data.shipA != null ? data.shipA : 0L);
            constraintTag.putLong("shipB", data.shipB != null ? data.shipB : 0L);

            // Save the ground flags
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

            System.out.println("SAVING TO NBT - ID: " + entry.getKey() +
                    ", shipA: " + data.shipA + ", shipB: " + data.shipB +
                    ", shipAIsGround: " + data.shipAIsGround + ", shipBIsGround: " + data.shipBIsGround);

            constraintsList.add(constraintTag);
        }

        tag.put("constraints", constraintsList);
        System.out.println("Saved " + persistedConstraints.size() + " constraints to disk");
        return tag;
    }

    public void addConstraint(String id, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                              double maxLength, double compliance, double maxForce, ServerLevel level) {
        // Store the actual ship IDs, don't convert to null
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
                shipAIsGround, shipBIsGround
        ));
        setDirty();
    }






    public void removeConstraint(String id) {
        if (persistedConstraints.remove(id) != null) {
            restoredConstraints.remove(id); // Also remove from restored tracking
            markConstraintAsRemoved(id);
            setDirty();
    //        System.out.println("Removed constraint from persistence: " + id);
        }
    }

    public Map<String, PersistedConstraintData> getAllConstraints() {
        return new HashMap<>(persistedConstraints);
    }

    public void resetRestorationState() {
        hasAttemptedRestore = false;
        restoredConstraints.clear();
        cleanupScheduled = false;
        ticksUntilCleanup = 0;
        cleanupLevel = null;
    //    System.out.println("Reset restoration state for constraint persistence");
    }

    public void restoreConstraints(ServerLevel level) {
        if (hasAttemptedRestore) {
        //    System.out.println("Restoration already attempted for this session, skipping");
            return;
        }
        hasAttemptedRestore = true;
      //  System.out.println("Restoring " + persistedConstraints.size() + " constraints...");

        Long currentGroundBodyId = null;
        try {
            currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
         //   System.out.println("Current ground body ID for dimension: " + currentGroundBodyId);
        } catch (Exception e) {
          //  System.err.println("Failed to get ground body ID: " + e.getMessage());
            return;
        }

        if (currentGroundBodyId == null) {
       //     System.err.println("Ground body ID is null, scheduling retry...");
            scheduleDeadConstraintCleanup(level, 200);
            hasAttemptedRestore = false; // Allow retry
            return;
        }

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        Map<String, PersistedConstraintData> constraintsCopy = new HashMap<>(persistedConstraints);
        for (Map.Entry<String, PersistedConstraintData> entry : constraintsCopy.entrySet()) {
            String persistenceId = entry.getKey();
            PersistedConstraintData data = entry.getValue();

            // CHECK FOR REMOVED CONSTRAINTS FIRST - THIS IS THE KEY FIX
            if (removedConstraints.contains(persistenceId)) {
                System.out.println("Skipping removed constraint during restoration: " + persistenceId);
                skipCount++;
                continue;
            }

            if (restoredConstraints.contains(persistenceId)) {
                System.out.println("Constraint " + persistenceId + " already restored, skipping");
                skipCount++;
                continue;
            }

            try {
                // Fix the ground body ID conversion
                Long actualShipA = data.shipAIsGround ? currentGroundBodyId : data.shipA;
                Long actualShipB = data.shipBIsGround ? currentGroundBodyId : data.shipB;

             //   System.out.println("Processing constraint: " + persistenceId);
             //   System.out.println("  Stored: shipA=" + data.shipA + " (isGround=" + data.shipAIsGround +
              //          "), shipB=" + data.shipB + " (isGround=" + data.shipBIsGround + ")");
             //   System.out.println("  Actual: shipA=" + actualShipA + ", shipB=" + actualShipB);
              //  System.out.println("  Current ground body ID: " + currentGroundBodyId);

                boolean shipAValid = data.shipAIsGround ||
                        (actualShipA != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipA) != null);
                boolean shipBValid = data.shipBIsGround ||
                        (actualShipB != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipB) != null);

                System.out.println("  Ship validation - A: " + shipAValid + ", B: " + shipBValid);

                if (!shipAValid || !shipBValid) {
                    System.err.println("Constraint " + persistenceId + " references missing ships");
                    if (!data.shipAIsGround && !shipAValid) {
                        System.err.println("  Ship A (" + actualShipA + ") missing");
                        skipCount++;
                        continue;
                    }
                    if (!data.shipBIsGround && !shipBValid) {
                        System.err.println("  Ship B (" + actualShipB + ") missing");
                        skipCount++;
                        continue;
                    }
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
                    ConstraintTracker.addConstraintToTracker(newConstraintId, actualShipA, actualShipB,
                            data.localPosA, data.localPosB, data.maxLength, data.compliance, data.maxForce);

                    ConstraintTracker.mapConstraintToPersistenceId(newConstraintId, persistenceId);

                    restoredConstraints.add(persistenceId);
               //     System.out.println("Successfully restored constraint " + persistenceId + " -> " + newConstraintId);
                    successCount++;
                } else {
              //      System.err.println("Failed to create VS constraint for " + persistenceId);
                    failCount++;
                }
            } catch (Exception e) {
             //   System.err.println("Error restoring constraint " + persistenceId + ": " + e.getMessage());
                e.printStackTrace();
                failCount++;
            }
        }

        System.out.println("Constraint restoration complete: " + successCount + " success, " +
                failCount + " failed, " + skipCount + " skipped");

        if (failCount > 0 || skipCount > 0) {
            scheduleDeadConstraintCleanup(level, 600); // 30 seconds
        }
    }



    public boolean hasAttemptedRestoration() {
        return hasAttemptedRestore;
    }

    public void forceResetRestoration() {
        hasAttemptedRestore = false;
        restoredConstraints.clear();
        System.out.println("Forced reset of restoration state");
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

    private boolean isShipValid(ServerLevel level, Long shipId, boolean isGround) {
        if (isGround) {
            Long currentGroundId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            return currentGroundId != null;
        } else {
            return VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId) != null;
        }
    }




    public void validateAndCleanupConstraints(ServerLevel level) {
        System.out.println("Validating " + persistedConstraints.size() + " persisted constraints...");

        Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                .get(VSGameUtilsKt.getDimensionId(level));

        if (groundBodyId == null) {
            System.err.println("Cannot validate constraints: ground body ID is null");
            return;
        }

        java.util.List<String> toRemove = new java.util.ArrayList<>();

        for (Map.Entry<String, PersistedConstraintData> entry : persistedConstraints.entrySet()) {
            PersistedConstraintData data = entry.getValue();

            boolean shipAExists = data.shipA.equals(groundBodyId) ||
                    VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipA) != null;
            boolean shipBExists = data.shipB.equals(groundBodyId) ||
                    VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(data.shipB) != null;

            if (!shipAExists || !shipBExists) {
                toRemove.add(entry.getKey());
            }
        }

        for (String id : toRemove) {
            removeConstraint(id);
        }

      //  System.out.println("Cleaned up " + toRemove.size() + " invalid constraints");
    }


}
