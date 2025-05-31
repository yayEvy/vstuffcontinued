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
import java.util.Map;

public class ConstraintPersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_constraints";

    private final Map<String, PersistedConstraintData> persistedConstraints = new HashMap<>();

    public static class PersistedConstraintData {
        public Long shipA;
        public Long shipB;
        public Vector3d localPosA;
        public Vector3d localPosB;
        public double maxLength;
        public double compliance;
        public double maxForce;

        public PersistedConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                       double maxLength, double compliance, double maxForce) {
            this.shipA = shipA;
            this.shipB = shipB;
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

    public static ConstraintPersistence load(CompoundTag tag) {
        ConstraintPersistence data = new ConstraintPersistence();
        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);

        for (int i = 0; i < constraintsList.size(); i++) {
            CompoundTag constraintTag = constraintsList.getCompound(i);
            String id = constraintTag.getString("id");

            long shipALong = constraintTag.getLong("shipA");
            long shipBLong = constraintTag.getLong("shipB");
            Long shipA = shipALong == 0L ? null : shipALong;
            Long shipB = shipBLong == 0L ? null : shipBLong;

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
                    ", shipA: " + shipA + ", shipB: " + shipB);

            data.persistedConstraints.put(id, new PersistedConstraintData(
                    shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce
            ));
        }
        System.out.println("Loaded " + data.persistedConstraints.size() + " persisted constraints");
        return data;
    }


    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag constraintsList = new ListTag();
        for (Map.Entry<String, PersistedConstraintData> entry : persistedConstraints.entrySet()) {
            CompoundTag constraintTag = new CompoundTag();
            PersistedConstraintData data = entry.getValue();

            constraintTag.putString("id", entry.getKey());
            constraintTag.putLong("shipA", data.shipA != null ? data.shipA : 0L);
            constraintTag.putLong("shipB", data.shipB != null ? data.shipB : 0L);
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
                    ", shipA: " + data.shipA + ", shipB: " + data.shipB);

            constraintsList.add(constraintTag);
        }
        tag.put("constraints", constraintsList);
        System.out.println("Saved " + persistedConstraints.size() + " constraints to disk");
        return tag;
    }


    public void addConstraint(String id, Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                              double maxLength, double compliance, double maxForce, ServerLevel level) {
        Long persistShipA = shipA;
        Long persistShipB = shipB;

        try {
            Long currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                    .getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));

            System.out.println("PERSISTENCE DEBUG - Current ground body ID: " + currentGroundBodyId);
            System.out.println("PERSISTENCE DEBUG - Input shipA: " + shipA + ", shipB: " + shipB);

            if (shipA != null && shipA.equals(currentGroundBodyId)) {
                persistShipA = null;
                System.out.println("Converting shipA ground body " + shipA + " to null for persistence");
            }
            if (shipB != null && shipB.equals(currentGroundBodyId)) {
                persistShipB = null;
                System.out.println("Converting shipB ground body " + shipB + " to null for persistence");
            }
        } catch (Exception e) {
            System.err.println("Failed to check ground body IDs: " + e.getMessage());
            e.printStackTrace();
        }

        persistedConstraints.put(id, new PersistedConstraintData(
                persistShipA, persistShipB, localPosA, localPosB, maxLength, compliance, maxForce
        ));
        setDirty();

        System.out.println("PERSISTENCE DEBUG - Added constraint to persistence: " + id +
                " (original: " + shipA + "," + shipB + " -> stored: " + persistShipA + "," + persistShipB + ")");
    }




    public void removeConstraint(String id) {
        if (persistedConstraints.remove(id) != null) {
            setDirty();
            System.out.println("Removed constraint from persistence: " + id);
        }
    }

    public Map<String, PersistedConstraintData> getAllConstraints() {
        return new HashMap<>(persistedConstraints);
    }


    public void restoreConstraints(ServerLevel level) {
        System.out.println("Restoring " + persistedConstraints.size() + " constraints...");
        Long groundBodyId = null;
        try {
            groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            System.out.println("Ground body ID for dimension: " + groundBodyId);
        } catch (Exception e) {
            System.err.println("Failed to get ground body ID: " + e.getMessage());
            return;
        }

        if (groundBodyId == null) {
            System.err.println("Ground body ID is null, cannot restore constraints");
            return;
        }

        int successCount = 0;
        int failCount = 0;
        Map<String, PersistedConstraintData> constraintsCopy = new HashMap<>(persistedConstraints);
        java.util.List<String> constraintsToRemove = new java.util.ArrayList<>();

        for (Map.Entry<String, PersistedConstraintData> entry : constraintsCopy.entrySet()) {
            String persistenceId = entry.getKey();
            PersistedConstraintData data = entry.getValue();

            try {
                Long actualShipA = (data.shipA == null || data.shipA == 0) ? groundBodyId : data.shipA;
                Long actualShipB = (data.shipB == null || data.shipB == 0) ? groundBodyId : data.shipB;

                System.out.println("Processing constraint: " + persistenceId +
                        " (originalA: " + data.shipA + " -> actualA: " + actualShipA +
                        ", originalB: " + data.shipB + " -> actualB: " + actualShipB + ")");

                boolean shipAValid = false;
                boolean shipBValid = false;

                if (actualShipA.equals(groundBodyId)) {
                    shipAValid = true; // Ground body always exists
                    System.out.println("Ship A is ground body - valid");
                } else {
                    Ship shipA = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipA);
                    shipAValid = (shipA != null);
                    System.out.println("Ship A (" + actualShipA + ") exists: " + shipAValid);
                }

                if (actualShipB.equals(groundBodyId)) {
                    shipBValid = true; // Ground body always exists
                    System.out.println("Ship B is ground body - valid");
                } else {
                    Ship shipB = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipB);
                    shipBValid = (shipB != null);
                    System.out.println("Ship B (" + actualShipB + ") exists: " + shipBValid);
                }

                if (!shipAValid || !shipBValid) {
                    System.err.println("Constraint " + persistenceId + " references missing ships (A:" + actualShipA +
                            " valid:" + shipAValid + ", B:" + actualShipB + " valid:" + shipBValid + ")");
                    constraintsToRemove.add(persistenceId);
                    failCount++;
                    continue;
                }

                VSRopeConstraint ropeConstraint = new VSRopeConstraint(
                        actualShipA, actualShipB,
                        data.compliance,
                        data.localPosA, data.localPosB,
                        data.maxForce,
                        data.maxLength
                );

                Integer constraintId = VSGameUtilsKt.getShipObjectWorld(level).createNewConstraint(ropeConstraint);
                if (constraintId != null) {
                    ConstraintTracker.addConstraintToTracker(constraintId, actualShipA, actualShipB,
                            data.localPosA, data.localPosB, data.maxLength, data.compliance, data.maxForce);
                    System.out.println("Successfully restored constraint " + persistenceId + " -> " + constraintId);
                    successCount++;
                } else {
                    System.err.println("Failed to create VS constraint for " + persistenceId);
                    failCount++;
                }
            } catch (Exception e) {
                System.err.println("Error restoring constraint " + persistenceId + ": " + e.getMessage());
                e.printStackTrace();
                failCount++;
            }
        }

        for (String deadConstraintId : constraintsToRemove) {
            removeConstraint(deadConstraintId);
            System.out.println("Removed dead constraint: " + deadConstraintId);
        }

        System.out.println("Constraint restoration complete: " + successCount + " success, " + failCount + " failed, " + constraintsToRemove.size() + " cleaned up");
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

        System.out.println("Cleaned up " + toRemove.size() + " invalid constraints");
    }


}
