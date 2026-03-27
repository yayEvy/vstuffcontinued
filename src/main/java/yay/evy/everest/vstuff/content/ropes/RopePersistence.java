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


// todo get rid of this class lmao, see mod init

public class RopePersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_ropes";

    private final Map<Integer, ReworkedRope> ropes = new HashMap<>();

    public static RopePersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(RopePersistence::load, RopePersistence::new, DATA_NAME);
    }

    public static RopePersistence load(CompoundTag tag) {
        RopePersistence data = new RopePersistence();

        RopeManager.resetId();

        ListTag ropeList = tag.getList("ropes", Tag.TAG_COMPOUND);
        for (Tag ropeTag : ropeList) {
            int nextId = RopeManager.getNextId();
            ReworkedRope rope = ReworkedRope.fromTag((CompoundTag) ropeTag, nextId);

            data.ropes.put(nextId, rope);

            RopeManager.addRopeToManager(rope);
        }

        VStuff.LOGGER.info("RopePersistence loaded {} ropes from saved data", data.ropes.size());
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag ropeList = new ListTag();
        for (Map.Entry<Integer, ReworkedRope> entry : ropes.entrySet()) {
            ropeList.add(entry.getValue().toTag());
        }
        tag.put("ropes", ropeList);
        return tag;
    }

    public void saveNow(ServerLevel level) {
        setDirty();
        level.getDataStorage().save();
    }

    public void addRope(ReworkedRope rope) {
        ropes.put(rope.ropeId, rope);
        setDirty();
    }

    public void removeRope(Integer id) {
        ropes.remove(id);
        setDirty();
    }

    /*

    public static void onShipLoad(ShipLoadEvent shipLoadEvent, RegisteredListener registeredListener) {
        Long loadedId = shipLoadEvent.getShip().getId();

        MinecraftServer server = ValkyrienSkiesMod.getCurrentServer();
        if (server == null) return;

        ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, shipLoadEvent.getShip().getChunkClaimDimension());
        if (level == null) return;

        RopePersistence ropePersistence = RopePersistence.get(level);

        ropePersistence.ropes.values().stream()
                .filter(rope -> (Objects.equals(rope.posData0.shipId(), loadedId) || Objects.equals(rope.posData1.shipId(), loadedId))
                        && !rope.hasRestored) // shallow check just for the boolean, not ids
                .forEach(rope -> {
                    //VStuff.LOGGER.info("Restoring rope with ropeId {} type {} id0: {}, id1: {} due to ship {} load", rope.ropeId, rope.type, rope.posData0.getShipIdSafe(level), rope.posData1.getShipIdSafe(level), loadedId);
                    rope.restoreJoint(level);
                });

    }


     */
}