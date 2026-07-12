package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeStyle;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeFactory;
import yay.evy.everest.vstuff.content.ropes.packet.AddPhysRopePacket;
import yay.evy.everest.vstuff.content.ropes.packet.AddRopePacket;
import yay.evy.everest.vstuff.content.ropes.packet.ClearAllRopesPacket;
import yay.evy.everest.vstuff.content.ropes.packet.RemoveRopePacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReworkedPhysRopeManager extends SavedData {

    private static final String DATA_NAME = "vstuff_phys_ropes";

    private static int nextId = 0;

    public final Map<Integer, ReworkedPhysRope> ropes = new HashMap<>();

    public static ReworkedPhysRopeManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(ReworkedPhysRopeManager::load, ReworkedPhysRopeManager::new, DATA_NAME);
    }

    public static ReworkedPhysRopeManager load(CompoundTag tag) {
        ReworkedPhysRopeManager data = new ReworkedPhysRopeManager();

        ListTag ropeList = tag.getList("ropes", Tag.TAG_COMPOUND);
        for (Tag ropeTag : ropeList) {
            ReworkedPhysRope rope = ReworkedPhysRopeFactory.ropeFromTag((CompoundTag) ropeTag);

            data.ropes.put(rope.ropeId, rope);
        }

        VStuff.LOGGER.info("Loaded {} ropes from saved data.", data.ropes.size());
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag ropeList = new ListTag();
        for (Map.Entry<Integer, ReworkedPhysRope> entry : ropes.entrySet()) {
            ropeList.add(ReworkedPhysRopeFactory.ropeToTag(entry.getValue()));
        }
        tag.put("ropes", ropeList);
        return tag;
    }


    public void addRope(ReworkedPhysRope rope) {
        rope.setRopeId(nextId++);

        ropes.put(rope.ropeId, rope);

        VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new AddPhysRopePacket(rope));

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

    public ReworkedPhysRope getRope(Integer id) {
        return ropes.get(id);
    }

    public List<ReworkedPhysRope> getRopeList() {
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
        ReworkedPhysRopeManager manager = ReworkedPhysRopeManager.get(player.serverLevel());
        VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new ClearAllRopesPacket());

        VStuff.LOGGER.info("Syncing all ropes to player {} ({})", player.getName().getString(), player.getUUID());


        for (ReworkedPhysRope rope : manager.getRopeList()) {
            //VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new AddRopePacket(rope, ClientRopeStyle.fromStyle(RopeStyleManager.resolveStyle(rope.styleKey, player.level().registryAccess()))));
        }
    }
}
