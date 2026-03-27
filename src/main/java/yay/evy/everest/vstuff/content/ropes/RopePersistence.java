package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import java.util.HashMap;
import java.util.Map;

public class RopePersistence extends SavedData {
    private static final String DATA_NAME = "vstuff_ropes";

    private final Map<Integer, ReworkedRope> ropes = new HashMap<>();

    public static RopePersistence get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        RopePersistence persistence = storage.computeIfAbsent(RopePersistence::load, RopePersistence::new, DATA_NAME);
        persistence.attachActors(level);
        return persistence;
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

    public void attachActors(ServerLevel level) {
        for (ReworkedRope rope : ropes.values()) {
            rope.attachActors(level);
        }
    }
}
