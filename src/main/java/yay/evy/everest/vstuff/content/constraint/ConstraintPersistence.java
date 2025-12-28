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

    private final Map<Integer, Rope> persistedConstraints = new HashMap<>();

    public static ConstraintPersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ConstraintPersistence::load, ConstraintPersistence::new, DATA_NAME);
    }

    public void markConstraintAsRemoved(Integer id) {
        persistedConstraints.remove(id);
        setDirty();
    }


    public static ConstraintPersistence load(CompoundTag tag) {
        ConstraintPersistence data = new ConstraintPersistence();

        if (tag.contains("lastUsedId")) {
            ConstraintTracker.setLastUsedId(tag.getInt("lastUsedId"));
        }

        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);
        for (int i = 0; i < constraintsList.size(); i++) {
            CompoundTag constraintTag = constraintsList.getCompound(i);
            Integer id = constraintTag.getInt("id");
            data.persistedConstraints.put(id, Rope.fromTag(constraintTag));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {

        tag.putInt("lastUsedId", ConstraintTracker.getNextId() - 1);

        ListTag constraintsList = new ListTag();
        for (Map.Entry<Integer, Rope> entry : persistedConstraints.entrySet()) {
            constraintsList.add(entry.getValue().toTag());
        }
        tag.put("constraints", constraintsList);
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
        if (data == null) return;

        persistedConstraints.remove(id);
        setDirty();

        VStuff.LOGGER.info("Removed constraint {} from persistence", id);
    }


    public static void onShipLoad(ShipLoadEvent shipLoadEvent, RegisteredListener registeredListener) {
        Long loadedId = shipLoadEvent.getShip().getId();
        System.out.println("loaded ship " + loadedId);

        MinecraftServer server = ValkyrienSkiesMod.getCurrentServer();
        if (server == null) return;

        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, shipLoadEvent.getShip().getChunkClaimDimension());
        if (level == null) return;

        ConstraintPersistence constraintPersistence = ConstraintPersistence.get(level);

        constraintPersistence.persistedConstraints.values().stream()
                .filter(rope -> (Objects.equals(rope.shipA, loadedId) || Objects.equals(rope.shipB, loadedId))
                        && !rope.hasRestoredJoint)
                .forEach(rope -> {
                    VStuff.LOGGER.info("Restoring rope with ID {}", rope.ID);
                    rope.restoreJoint(level);
                });

    }

}