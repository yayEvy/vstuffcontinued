package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.type.RopeTypeRegistry;
import yay.evy.everest.vstuff.internal.network.NetworkHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RopeManager extends SavedData {

    private static final String DATA_NAME = "vstuff_ropes";

    private static int nextId = 0;

    private final Map<Integer, ReworkedRope> ropes = new HashMap<>();

    public static RopeManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        RopeManager persistence = storage.computeIfAbsent(RopeManager::load, RopeManager::new, DATA_NAME);
        persistence.attachActors(level);
        return persistence;
    }

    public static RopeManager load(CompoundTag tag) {
        RopeManager data = new RopeManager();

        ListTag ropeList = tag.getList("ropes", Tag.TAG_COMPOUND);
        for (Tag ropeTag : ropeList) {
            ReworkedRope rope = RopeFactory.ropeFromTag((CompoundTag) ropeTag);

            data.ropes.put(rope.ropeId, rope);
        }

        VStuff.LOGGER.info("Loaded {} ropes from saved data.", data.ropes.size());
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag ropeList = new ListTag();
        for (Map.Entry<Integer, ReworkedRope> entry : ropes.entrySet()) {
            ropeList.add(RopeFactory.ropeToTag(entry.getValue()));
        }
        tag.put("ropes", ropeList);
        return tag;
    }


    public void addRope(ReworkedRope rope) {
        rope.setRopeId(nextId++);

        ropes.put(rope.ropeId, rope);

        NetworkHandler.sendConstraintAdd(rope.ropeId, rope.posData0.shipId(), rope.posData1.shipId(), rope.posData0.localPos(), rope.posData1.localPos(), rope.jointValues.maxLength(), rope.type.id());

        setDirty();
    }

    public void removeRope(Integer id) {
        ropes.remove(id);

        NetworkHandler.sendConstraintRemove(id);

        setDirty();
    }

    public boolean hasRope(Integer id) {
        return ropes.containsKey(id);
    }

    public ReworkedRope getRope(Integer id) {
        return ropes.get(id);
    }

    public List<ReworkedRope> getRopeList() {
        return ropes.values().stream().toList();
    }

    public List<Integer> getIdList() {
        return ropes.keySet().stream().toList();
    }

    public void saveNow(ServerLevel level) {
        setDirty();

        level.getDataStorage().save();
    }

    public void attachActors(ServerLevel level) {
        for (ReworkedRope rope : ropes.values()) {
            rope.attachActors(level);
        }
    }

    public static void syncAllRopesToPlayer(ServerPlayer player) {
        RopeManager manager = RopeManager.get(player.serverLevel());
        NetworkHandler.sendClearAllConstraintsToPlayer(player);

        VStuff.LOGGER.info("Syncing all ropes to player {} ({})", player.getName().getString(), player.getUUID());

        for (Map.Entry<Integer, ReworkedRope> entry : manager.ropes.entrySet()) {
            ReworkedRope data = entry.getValue();
            NetworkHandler.sendConstraintAddToPlayer(
                    player,
                    entry.getKey(),
                    data.posData0.shipId(),
                    data.posData1.shipId(),
                    data.posData0.localPos(),
                    data.posData1.localPos(),
                    data.jointValues.maxLength(),
                    data.type.id()
            );
        }
    }
}
