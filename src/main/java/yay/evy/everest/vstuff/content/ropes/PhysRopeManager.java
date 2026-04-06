package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.packet.AddRopePacket;
import yay.evy.everest.vstuff.content.ropes.packet.PhysRopeSegmentsPacket;
import yay.evy.everest.vstuff.content.ropes.packet.RemoveRopePacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.RopePosData;
import yay.evy.everest.vstuff.internal.utility.TagUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.*;

public class PhysRopeManager extends SavedData {

    private static final String DATA_NAME = "vstuff_phys_ropes";
    private int nextId = 100_000; // todo not do this


    private final Map<Integer, PhysRopeConstraint> physRopes = new HashMap<>();

    public static PhysRopeManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(
                tag -> PhysRopeManager.load(tag, level),
                PhysRopeManager::new,
                DATA_NAME
        );
    }

    public static PhysRopeManager load(CompoundTag tag, ServerLevel level) {
        PhysRopeManager manager = new PhysRopeManager();
        if (tag.contains("nextId")) {
            manager.nextId = tag.getInt("nextId");
        }
        ListTag ropeList = tag.getList("physRopes", Tag.TAG_COMPOUND);
        for (Tag ropeTag : ropeList) {
            CompoundTag ropeCompound = (CompoundTag) ropeTag;

            int ropeId = ropeCompound.getInt("ropeId");
            RopePosData posData0 = TagUtils.readPosData(ropeCompound.getCompound("posData0"));
            RopePosData posData1 = TagUtils.readPosData(ropeCompound.getCompound("posData1"));

            long[] segIds = ropeCompound.getLongArray("segmentShipIds");
            List<Long> segmentShipIds = new ArrayList<>();
            for (long id : segIds) segmentShipIds.add(id);


            posData0.attach(level, ropeId);
            posData1.attach(level, ropeId);



            // yes
            int segments = ropeCompound.getInt("segments");
            double segmentLength = ropeCompound.getDouble("segmentLength");

            PhysRopeConstraint constraint = new PhysRopeConstraint(posData0, posData1);
            constraint.setRopeId(ropeId);
            constraint.restoreSegmentIds(segmentShipIds);
            constraint.segments = segments;
            constraint.segmentLength = segmentLength;
            constraint.restoreSegmentData(segments, segmentLength);

            manager.physRopes.put(ropeId, constraint);
        }

        VStuff.LOGGER.info("Loaded {} phys ropes from saved data.", manager.physRopes.size());
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("nextId", nextId);
        ListTag ropeList = new ListTag();
        for (Map.Entry<Integer, PhysRopeConstraint> entry : physRopes.entrySet()) {
            PhysRopeConstraint constraint = entry.getValue();
            CompoundTag ropeTag = new CompoundTag();

            ropeTag.putInt("ropeId", entry.getKey());
            ropeTag.put("posData0", TagUtils.writePosData(constraint.posData0));
            ropeTag.put("posData1", TagUtils.writePosData(constraint.posData1));
            ropeTag.putLongArray("segmentShipIds", constraint.getSegmentShipIds());

            ropeTag.putInt("segments", constraint.getSegments());
            ropeTag.putDouble("segmentLength", constraint.getSegmentLength());

            ropeList.add(ropeTag);
        }
        tag.put("physRopes", ropeList);
        return tag;
    }

    public void addRope(PhysRopeConstraint constraint) {
        physRopes.put(constraint.getRopeId(), constraint);
        setDirty();
    }

    public void removeRope(ServerLevel level, int id) {
        PhysRopeConstraint constraint = physRopes.remove(id);
        if (constraint != null) {
            constraint.destroy(level);
            VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new RemoveRopePacket(id));
            setDirty();
        }
    }

    public Map<Integer, PhysRopeConstraint> getPhysRopes() {
        return physRopes;
    }

    public void tickSegmentSync(ServerLevel level) {
        if (physRopes.isEmpty()) return;
        var loadedEntities = VSGameUtilsKt.getShipObjectWorld(level).retrieveLoadedPhysicsEntities();
        for (Map.Entry<Integer, PhysRopeConstraint> entry : physRopes.entrySet()) {
            List<Vector3d> positions = entry.getValue().getSegmentWorldPositions(loadedEntities);
            if (positions == null) continue;
            VStuffPackets.channel().send(PacketDistributor.ALL.noArg(),
                    new PhysRopeSegmentsPacket(entry.getKey(), positions));
        }
    }

    public void syncAllToPlayer(ServerLevel level, ServerPlayer player) {
        for (Map.Entry<Integer, PhysRopeConstraint> entry : physRopes.entrySet()) {
            PhysRopeConstraint c = entry.getValue();
            double actualLength = c.posData0.getWorldPos(level).distance(c.posData1.getWorldPos(level));
            VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player),
                    new AddRopePacket(entry.getKey(), c.posData0.shipId(), c.posData1.shipId(),
                            c.posData0.localPos(), c.posData1.localPos(),
                            actualLength, new ResourceLocation("vstuff", "normal")));
        }
    }

    public int allocateId() {
        return nextId++;
    }
    // yes
}