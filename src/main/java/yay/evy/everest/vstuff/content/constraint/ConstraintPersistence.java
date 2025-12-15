package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;

import java.util.*;

public class ConstraintPersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_constraints";
    private final Set<Integer> removedConstraints = new HashSet<>();


    private final Map<Integer, Rope> persistedConstraints = new HashMap<>();
    private final Set<Integer> restoredConstraints = new HashSet<>();
    private boolean hasAttemptedRestore = false;

    public static ConstraintPersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ConstraintPersistence::load, ConstraintPersistence::new, DATA_NAME);
    }
    public void markConstraintAsRemoved(Integer id) {
        removedConstraints.add(id);
        persistedConstraints.remove(id);
        restoredConstraints.remove(id);
        setDirty();
    }

    public static ConstraintPersistence load(CompoundTag tag) {
        ConstraintPersistence data = new ConstraintPersistence();
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
        VStuff.LOGGER.info("Saved {} constraints to /data/" + DATA_NAME + ".dat", persistedConstraints.size());
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

    public static void restoreConstraints(ServerLevel level) {
        ConstraintPersistence persistence = ConstraintPersistence.get(level);
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
                VStuff.LOGGER.error("Failed to get ground body id: {}", e.getMessage());
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

            VStuff.LOGGER.info("ConstraintPersistence successfully restored {} constraints, failed to restore {} constraints, and skipped restoring {} constraints", successCount, failCount, skipCount);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                ConstraintTracker.syncAllConstraintsToPlayer(player);
            }
        });
    }

    private boolean validateShipsAndCreateConstraint(ServerLevel level, Integer persistenceId, Rope rope,
                                                     Long actualShipA, Long actualShipB) {
        try {
            rope.restoreJoint(level);

            return true;
        } catch (Exception e) {
            VStuff.LOGGER.error("Error restoring constraint {}: {}", persistenceId, e.getMessage());
        }
        return false;
    }
}