package yay.evy.everest.vstuff.content.roperework;

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

import java.util.HashMap;
import java.util.Map;

public class RopePersistence extends SavedData {

    public static final String NAME = "vstuff_ropes";

    private final Map<Integer, NewRope> persistenceRopes = new HashMap<>();

    @Override
    public CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        ListTag ropeTag = new ListTag();

        for (Map.Entry<Integer, NewRope> entry : persistenceRopes.entrySet()) {
            ropeTag.add(entry.getValue().toTag());
        }

        pCompoundTag.put("constraints", ropeTag);
        VStuff.LOGGER.info("RopePersistence saved {} ropes to " + NAME + ".dat", ropeTag.size());
        return pCompoundTag;
    }

    public static RopePersistence getOrCreate(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> RopePersistence.load(tag, level), RopePersistence::new, NAME);
    }

    public static RopePersistence load(CompoundTag tag, ServerLevel level) {
        VStuff.LOGGER.info("Loading persistence...");
        RopeManager.resetId();
        RopeManager.clearAllRopes();
        RopePersistence ropePersistence = new RopePersistence();

        ListTag ropeList = tag.getList("constraints", Tag.TAG_COMPOUND);
        for (int i = 0; i < ropeList.size(); i++) {
            CompoundTag constraintTag = ropeList.getCompound(i);
            NewRope rope = NewRope.fromTag(level, constraintTag);

            RopeManager.addRopeToManager(rope);

            ropePersistence.persistenceRopes.put(rope.ropeId, rope);
        }
        VStuff.LOGGER.info("RopePersistence loaded {} ropes from data", ropePersistence.persistenceRopes.size());
        return ropePersistence;
    }

    public void saveNow(ServerLevel level) {
        setDirty();
        level.getDataStorage().save();
    }

    public void addRope(NewRope rope) {
        persistenceRopes.put(rope.ropeId, rope);
        setDirty();
    }

    public void removeRope(Integer ropeId) {
        persistenceRopes.remove(ropeId);
        setDirty();
    }

    public static void onShipLoad(ShipLoadEvent shipLoadEvent, RegisteredListener registeredListener) {
        Long loadedId = shipLoadEvent.getShip().getId();

        MinecraftServer server = ValkyrienSkiesMod.getCurrentServer();
        if (server == null) return;

        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, shipLoadEvent.getShip().getChunkClaimDimension());
        if (level == null) return;

        RopePersistence ropePersistence = RopePersistence.getOrCreate(level);

        ropePersistence.persistenceRopes.values().stream()
                .filter(rope -> rope.isRopeOnShip(loadedId) && !rope.hasRestored)
                .forEach(rope -> rope.restoreJoint(level));

    }
}
