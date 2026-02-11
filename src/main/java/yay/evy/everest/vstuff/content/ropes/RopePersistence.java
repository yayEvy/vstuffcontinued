package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.event.RegisteredListener;
import org.valkyrienskies.core.api.events.ShipLoadEvent;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

import java.util.*;

public class RopePersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_constraints";

    private final Map<Integer, ReworkedRope> persistedConstraints = new HashMap<>();

    public static RopePersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(RopePersistence::load, RopePersistence::new, DATA_NAME);
    }

    public void markConstraintAsRemoved(Integer id) {
        persistedConstraints.remove(id);
        setDirty();
    }


    public static RopePersistence load(CompoundTag tag) {
        RopePersistence data = new RopePersistence();

        if (tag.contains("lastUsedId")) {
            RopeManager.setLastUsedId(tag.getInt("lastUsedId"));
        }

        ListTag constraintsList = tag.getList("constraints", Tag.TAG_COMPOUND);
        for (int i = 0; i < constraintsList.size(); i++) {
            CompoundTag constraintTag = constraintsList.getCompound(i);
            ReworkedRope rope = ReworkedRope.fromTag(constraintTag);

            data.persistedConstraints.put(rope.ropeId, rope);

            RopeManager.addRopeToManager(rope);
        }
        VStuff.LOGGER.info("RopePersistence loaded {} ropes from saved data", data.persistedConstraints.size());
        return data;
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {

        tag.putInt("lastUsedId", RopeManager.getNextId() - 1);

        ListTag constraintsList = new ListTag();
        for (Map.Entry<Integer, ReworkedRope> entry : persistedConstraints.entrySet()) {
            constraintsList.add(entry.getValue().toTag());
        }
        tag.put("constraints", constraintsList);
        return tag;
    }

    public void saveNow(ServerLevel level) {
        setDirty();
        level.getDataStorage().save();
    }

    public void addConstraint(ReworkedRope rope) {
        persistedConstraints.put(rope.ropeId, rope);
        setDirty();
    }

    public void removeConstraint(Integer id) {
        ReworkedRope data = persistedConstraints.get(id);
        if (data == null) return;

        persistedConstraints.remove(id);
        setDirty();

        //VStuff.LOGGER.info("Removed constraint {} from persistence", id);
    }


    public static void onShipLoad(ShipLoadEvent shipLoadEvent, RegisteredListener registeredListener) {
        Long loadedId = shipLoadEvent.getShip().getId();

        MinecraftServer server = ValkyrienSkiesMod.getCurrentServer();
        if (server == null) return;

        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, shipLoadEvent.getShip().getChunkClaimDimension());
        if (level == null) return;

        RopePersistence ropePersistence = RopePersistence.get(level);

        ropePersistence.persistedConstraints.values().stream()
                .filter(rope -> (Objects.equals(rope.posData0.shipId(), loadedId) || Objects.equals(rope.posData1.shipId(), loadedId))
                        && !rope.hasRestored) // shallow check just for the boolean, not ids
                .forEach(rope -> {
                    //VStuff.LOGGER.info("Restoring rope with ropeId {} type {} id0: {}, id1: {} due to ship {} load", rope.ropeId, rope.type, rope.posData0.getShipIdSafe(level), rope.posData1.getShipIdSafe(level), loadedId);
                    rope.restoreJoint(level);
                });

    }

}