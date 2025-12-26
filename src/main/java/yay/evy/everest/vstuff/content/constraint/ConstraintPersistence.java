package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.event.RegisteredListener;
import org.valkyrienskies.core.api.events.ShipLoadEvent;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.VStuff;

import java.util.*;
import java.util.stream.Stream;

public class ConstraintPersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_constraints";
    private final Set<Integer> removedConstraints = new HashSet<>();

    private final Map<Integer, Rope> persistedConstraints = new HashMap<>();

    public static ConstraintPersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ConstraintPersistence::load, ConstraintPersistence::new, DATA_NAME);
    }

    public void markConstraintAsRemoved(Integer id) {
        removedConstraints.add(id);
        persistedConstraints.remove(id);
        setDirty();
    }

    public static ConstraintPersistence load(CompoundTag tag) {
        ConstraintPersistence data = new ConstraintPersistence();
        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);
        ListTag removedList = tag.getList("removedConstraints", Tag.TAG_INT);

        VStuff.LOGGER.info("ConstraintPersistence found {} removed constraints", removedList.size());
        VStuff.LOGGER.info("ConstraintPersistence found {} constraints", constraintsList.size());

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

        VStuff.LOGGER.info("ConstraintPersistence loaded with {} constraints", data.persistedConstraints.size() );
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag constraintsList = new ListTag();
        ListTag removedList = new ListTag();

        VStuff.LOGGER.info("Found {} removed constraints to save to persistence", removedConstraints.size());
        VStuff.LOGGER.info("Found {} constraints to save to persistence", persistedConstraints.size());

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
            markConstraintAsRemoved(id);
            setDirty();
        }
    }

    public static void onShipLoad(ShipLoadEvent shipLoadEvent, RegisteredListener registeredListener) {
        Long loadedId = shipLoadEvent.getShip().getId();

        MinecraftServer server = ValkyrienSkiesMod.getCurrentServer();
        if (server == null) return;

        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, shipLoadEvent.getShip().getChunkClaimDimension());
        if (level == null) return;

        ConstraintPersistence constraintPersistence = ConstraintPersistence.get(level);

        constraintPersistence.persistedConstraints.values().stream()
                .filter(rope -> Objects.equals(rope.shipA, loadedId) || Objects.equals(rope.shipB, loadedId))
                .forEach(rope -> {
                    rope.restoreJoint(level);
                });
    }

}