package dev.flarelog.vstuff.content.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.network.packets.rope.AddRopePacket;
import dev.flarelog.vstuff.network.packets.rope.ClearAllRopesPacket;
import dev.flarelog.vstuff.network.packets.rope.RemoveRopePacket;
import dev.flarelog.vstuff.network.VStuffPackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RopeManager extends SavedData {

    private static final String DATA_NAME = "vstuff_phys_ropes";

    private static int nextId = 0;

    public final Map<Integer, Rope> ropes = new HashMap<>();

    public static RopeManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(RopeManager::load, RopeManager::new, DATA_NAME);
    }

    public static RopeManager load(CompoundTag tag) {
        RopeManager data = new RopeManager();

        ListTag ropeList = tag.getList("ropes", Tag.TAG_COMPOUND);
        for (Tag ropeTag : ropeList) {
            Rope rope = RopeFactory.ropeFromTag((CompoundTag) ropeTag);

            data.ropes.put(rope.ropeId, rope);
        }

        VStuff.LOGGER.info("Loaded {} ropes from saved data.", data.ropes.size());
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag ropeList = new ListTag();
        for (Map.Entry<Integer, Rope> entry : ropes.entrySet()) {
            ropeList.add(RopeFactory.ropeToTag(entry.getValue()));
        }
        tag.put("ropes", ropeList);
        return tag;
    }


    public void addRope(Rope rope) {
        rope.setRopeId(nextId++);

        ropes.put(rope.ropeId, rope);

        VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new AddRopePacket(rope));

        setDirty();
    }

    public void removeRope(Integer id) {
        ropes.remove(id);

        VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new RemoveRopePacket(id));

        setDirty();
    }

    public boolean hasRope(Integer id) {
        return ropes.containsKey(id);
    }

    public Rope getRope(Integer id) {
        return ropes.get(id);
    }

    public List<Rope> getRopeList() {
        return ropes.values().stream().toList();
    }

    public List<Integer> getIdList() {
        return ropes.keySet().stream().toList();
    }

    public void saveNow(ServerLevel level) {
        setDirty();

        level.getDataStorage().save();
    }

    public static void syncAllRopesToPlayer(ServerPlayer player) {
        RopeManager manager = RopeManager.get(player.serverLevel());
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new ClearAllRopesPacket());

        VStuff.LOGGER.info("Syncing all ropes to player {} ({})", player.getName().getString(), player.getUUID());


        for (Rope rope : manager.getRopeList()) {
            //VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new AddRopePacket(rope, ClientRopeStyle.fromStyle(RopeStyleManager.resolveStyle(rope.styleKey, player.level().registryAccess()))));
        }
    }
}
