package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.*;

public class NewConstraintPersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_constraints";
    private final Set<Integer> removedConstraints = new HashSet<>();


    private final Map<Integer, Rope> persistedConstraints = new HashMap<>();
    private final Set<Integer> restoredConstraints = new HashSet<>();
    private boolean hasAttemptedRestore = false;

    private boolean cleanupScheduled = false;
    private int ticksUntilCleanup = 0;
    private ServerLevel cleanupLevel = null;


    public static NewConstraintPersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(NewConstraintPersistence::load, NewConstraintPersistence::new, DATA_NAME);
    }
    public void markConstraintAsRemoved(Integer id) {
        removedConstraints.add(id);
        persistedConstraints.remove(id);
        restoredConstraints.remove(id);
        setDirty();
    }

    public static NewConstraintPersistence load(CompoundTag tag) {
        NewConstraintPersistence data = new NewConstraintPersistence();
        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);
        ListTag removedList = tag.getList("removedConstraints", Tag.TAG_INT);

        for (int i = 0; i < removedList.size(); i++) {
            data.removedConstraints.add(removedList.getInt(i));
        }

        for (int i = 0; i < constraintsList.size(); i++) {
            CompoundTag constraintTag = constraintsList.getCompound(i);
            Integer id = constraintTag.getInt("id");

            if (data.removedConstraints.contains(id)) {
                continue;
            }

            data.persistedConstraints.put(id, Rope.fromTag(constraintTag));
        }
        return data;
    }



    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag constraintsList = new ListTag();
        ListTag removedList = new ListTag();

        for (Integer removedId : removedConstraints) {
            removedList.add(IntTag.valueOf(removedId));
        }
        tag.put("removedConstraints", removedList);

        for (Map.Entry<Integer, Rope> entry : persistedConstraints.entrySet()) {
            Integer constraintId = entry.getKey();

            if (removedConstraints.contains(constraintId)) continue;

            CompoundTag constraintTag = entry.getValue().toTag();

            constraintsList.add(constraintTag);
        }

        tag.put("constraints", constraintsList);
        System.out.println("Saved " + persistedConstraints.size() + " constraints to disk");
        return tag;
    }

    public void saveNow(ServerLevel level) {
        setDirty();
        level.getDataStorage().save();
    }

    public void addConstraint(Rope rope) {
        persistedConstraints.put(rope.ID, rope);
        setDirty();
    }

    public void removeConstraint(Integer id) {
        Rope data = persistedConstraints.get(id);
        if (data != null && (data.shipAIsGround || data.shipBIsGround)) return;

        if (persistedConstraints.remove(id) != null) {
            restoredConstraints.remove(id);
            markConstraintAsRemoved(id);
            setDirty();
        }
    }

    public Map<Integer, Rope> getAllConstraints() {
        return new HashMap<>(persistedConstraints);
    }

    private void restoreLoadedChunkConstraints(ServerLevel level, Long currentGroundBodyId) {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        Map<Integer, Rope> constraintsCopy = getAllConstraints();
        for (Map.Entry<Integer, Rope> entry : constraintsCopy.entrySet()) {
            Integer persistenceId = entry.getKey();
            Rope rope = entry.getValue();

            if (removedConstraints.contains(persistenceId)) {
                skipCount++;
                continue;
            }

            if (restoredConstraints.contains(persistenceId)) {
                skipCount++;
                continue;
            }

            if (!isConstraintInLoadedChunks(level, rope, currentGroundBodyId)) {
                System.out.println("Constraint " + persistenceId + " is not in loaded chunks, will restore when chunks load");
                skipCount++;
                continue;
            }


        }

        System.out.println("Initial constraint restoration complete: " + successCount + " success, " +
                failCount + " failed, " + skipCount + " skipped (will restore when chunks load)");
    }

    private boolean isConstraintInLoadedChunks(ServerLevel level, Rope rope, Long groundBodyId) {
        try {
            if (rope.constraintType == RopeUtil.ConstraintType.PULLEY && rope.sourceBlockPos != null) {
                return level.isLoaded(rope.sourceBlockPos);
            }

            boolean pointALoaded = isPositionChunkLoaded(level, rope.localPosA, rope.shipA, rope.shipAIsGround, groundBodyId);
            boolean pointBLoaded = isPositionChunkLoaded(level, rope.localPosB, rope.shipB, rope.shipBIsGround, groundBodyId);

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

            BlockPos blockPos = new BlockPos(
                    (int) Math.floor(worldPos.x),
                    (int) Math.floor(worldPos.y),
                    (int) Math.floor(worldPos.z)
            );

            return level.isLoaded(blockPos);
        } catch (Exception e) {
            return false;
        }
    }


    public static Integer getConstraintIdForBlock(ServerLevel level, BlockPos blockPos) {
        for (Map.Entry<Integer, ConstraintTracker.RopeConstraintData> entry : ConstraintTracker.getActiveConstraints().entrySet()) {
            ConstraintTracker.RopeConstraintData data = entry.getValue();
            if (data.sourceBlockPos != null && data.sourceBlockPos.equals(blockPos)) {
                return entry.getKey();
            }
        }
        return null;
    }


    public static void restoreConstraints(ServerLevel level) {
        NewConstraintPersistence persistence = NewConstraintPersistence.get(level);
        persistence.restoreConstraintsInstance(level);
    }

    public void restoreConstraintsInstance(ServerLevel level) {

        if (hasAttemptedRestore) return;
        hasAttemptedRestore = true;

        level.getServer().execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            Long currentGroundBodyId;
            try {
                currentGroundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));
            } catch (Exception e) {
                //  System.err.println("Failed to get ground body ID: " + e.getMessage());
                return;
            }

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;

            for (Map.Entry<Integer, Rope> entry : persistedConstraints.entrySet()) {
                Integer persistenceId = entry.getKey();
                Rope rope = entry.getValue();

                if (removedConstraints.contains(persistenceId)) { skipCount++; continue; }
                if (restoredConstraints.contains(persistenceId)) { skipCount++; continue; }

                Long actualShipA = rope.shipAIsGround ? currentGroundBodyId : rope.shipA;
                Long actualShipB = rope.shipBIsGround ? currentGroundBodyId : rope.shipB;

                if (validateShipsAndCreateConstraint(level, persistenceId, rope, actualShipA, actualShipB)) {
                    restoredConstraints.add(persistenceId);
                    successCount++;
                } else {
                    failCount++;
                }
            }

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                ConstraintTracker.syncAllConstraintsToPlayer(player);
            }
        });
    }

    private void cleanupDeadConstraints(ServerLevel level) {
        try {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));

            if (groundBodyId == null) return;

            List<Integer> toRemove = new ArrayList<>();

            for (Map.Entry<Integer, Rope> entry : persistedConstraints.entrySet()) {
                Integer persistenceId = entry.getKey();
                Rope rope = entry.getValue();

                if (restoredConstraints.contains(persistenceId)) continue;

                if (rope.shipAIsGround || rope.shipBIsGround) continue;

                Long actualShipA = rope.shipAIsGround ? groundBodyId : rope.shipA;
                Long actualShipB = rope.shipBIsGround ? groundBodyId : rope.shipB;

                boolean shipAValid = rope.shipAIsGround ||
                        (actualShipA != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipA) != null);
                boolean shipBValid = rope.shipBIsGround ||
                        (actualShipB != null && VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(actualShipB) != null);

                if (!shipAValid || !shipBValid) toRemove.add(persistenceId);
            }

            for (Integer deadId : toRemove) removeConstraint(deadId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
        Map<Integer, Rope> constraintsCopy = getAllConstraints();

        for (Map.Entry<Integer, Rope> entry : constraintsCopy.entrySet()) {
            Integer persistenceId = entry.getKey();
            Rope rope = entry.getValue();

            if (removedConstraints.contains(persistenceId) ||
                    restoredConstraints.contains(persistenceId) ||
                    !isConstraintInChunk(level, rope, chunkPos, currentGroundBodyId)) {
                continue;
            }

            Long actualShipA = rope.shipAIsGround ? currentGroundBodyId : rope.shipA;
            Long actualShipB = rope.shipBIsGround ? currentGroundBodyId : rope.shipB;

            if (validateShipsAndCreateConstraint(level, persistenceId, rope, actualShipA, actualShipB)) {
                restoredCount++;
                restoredConstraints.add(persistenceId);
            }
        }

        if (restoredCount > 0) {
            //  System.out.println("Restored " + restoredCount + " constraints for chunk " + chunkPos);
        }
    }

    private boolean validateShipsAndCreateConstraint(ServerLevel level, Integer persistenceId, Rope rope,
                                                     Long actualShipA, Long actualShipB) {
        try {
            rope.createJoint();

            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error restoring constraint {}: {}", persistenceId, e.getMessage());
        }
        return false;
    }



    private boolean isConstraintInChunk(ServerLevel level, Rope rope, ChunkPos chunkPos, Long groundBodyId) {
        try {
            if (rope.constraintType == RopeUtil.ConstraintType.PULLEY
                    && rope.sourceBlockPos != null) {
                ChunkPos sourceChunk = new ChunkPos(rope.sourceBlockPos);
                return sourceChunk.equals(chunkPos);
            }

            boolean pointAInChunk = isPositionInChunk(level, rope.localPosA, rope.shipA, rope.shipAIsGround, chunkPos, groundBodyId);
            boolean pointBInChunk = isPositionInChunk(level, rope.localPosB, rope.shipB, rope.shipBIsGround, chunkPos, groundBodyId);

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