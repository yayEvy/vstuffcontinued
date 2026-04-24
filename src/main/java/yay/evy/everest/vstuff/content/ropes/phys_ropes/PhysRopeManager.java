package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.content.ropes.packet.AddRopePacket;
import yay.evy.everest.vstuff.content.ropes.packet.PhysRopeSegmentsPacket;
import yay.evy.everest.vstuff.content.ropes.packet.RemoveRopePacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.utility.TagUtils;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;

import java.util.*;

public class PhysRopeManager extends SavedData {

    private static final String DATA_NAME = "vstuff_phys_ropes";
    private int nextId = 100_000;
    private ServerLevel level;

    private final Map<Integer, PhysRopeConstraint> physRopes = new HashMap<>();

    public static PhysRopeManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        PhysRopeManager manager = storage.computeIfAbsent(
                tag -> PhysRopeManager.load(tag, level),
                PhysRopeManager::new,
                DATA_NAME
        );
        manager.level = level;
        return manager;
    }

    public static PhysRopeManager load(CompoundTag tag, ServerLevel level) {
        PhysRopeManager manager = new PhysRopeManager();
        if (tag.contains("nextId")) {
            manager.nextId = tag.getInt("nextId");
        }

        ListTag ropeList = tag.getList("physRopes", Tag.TAG_COMPOUND);
        for (Tag t : ropeList) {
            CompoundTag ropeTag = (CompoundTag) t;

            int ropeId = ropeTag.getInt("ropeId");
            RopePosData posData0 = TagUtils.readPosData(ropeTag.getCompound("posData0"));
            RopePosData posData1 = TagUtils.readPosData(ropeTag.getCompound("posData1"));

            posData0.attach(level, ropeId);
            posData1.attach(level, ropeId);

            long[] segIds = ropeTag.getLongArray("segmentShipIds");
            List<Long> segmentShipIds = new ArrayList<>();
            for (long id : segIds) segmentShipIds.add(id);

            int segments = ropeTag.getInt("segments");
            double segmentLength = ropeTag.getDouble("segmentLength");
            ListTag savedSegmentData = ropeTag.getList("segmentData", Tag.TAG_COMPOUND);

            PhysRopeConstraint constraint = new PhysRopeConstraint(posData0, posData1);
            constraint.setRopeId(ropeId);
            constraint.restoreSegmentIds(segmentShipIds);
            constraint.restoreSegmentData(segments, segmentLength);

            level.getServer().tell(new net.minecraft.server.TickTask(
                    level.getServer().getTickCount() + 1,
                    () -> {
                        constraint.restorePhysEntities(level, savedSegmentData);
                        level.getServer().tell(new net.minecraft.server.TickTask(
                                level.getServer().getTickCount() + 2,
                                () -> constraint.restoreJoints(level)
                        ));
                    }
            ));

            manager.physRopes.put(ropeId, constraint);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("nextId", nextId);
        var loadedEntities = VSGameUtilsKt.getShipObjectWorld(level).retrieveLoadedPhysicsEntities();

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

            constraint.captureSegmentData(loadedEntities);
            ropeTag.put("segmentData", constraint.saveSegmentDataToNBT());

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

        for (PhysRopeConstraint rope : physRopes.values()) {
            List<Vector3d> positions = rope.getSegmentWorldPositions(loadedEntities);
            if (positions != null) {
                rope.updateLastKnownPositions(positions);
                VStuffPackets.channel().send(PacketDistributor.ALL.noArg(),
                        new PhysRopeSegmentsPacket(rope.getRopeId(), positions));
            }
        }
        setDirty();
    }

    public void syncAllToPlayer(ServerLevel level, ServerPlayer player) {
        var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        for (Map.Entry<Integer, PhysRopeConstraint> entry : physRopes.entrySet()) {
            PhysRopeConstraint c = entry.getValue();

            double actualLength = c.posData0.getWorldPos(level)
                    .distance(c.posData1.getWorldPos(level));

            VStuffPackets.channel().send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new AddRopePacket(
                            entry.getKey(),
                            c.posData0.shipId(),
                            c.posData1.shipId(),
                            c.posData0.localPos(),
                            c.posData1.localPos(),
                            actualLength,
                            new ResourceLocation("vstuff", "normal")
                    )
            );

            level.getServer().tell(new net.minecraft.server.TickTask(
                    level.getServer().getTickCount() + 2,
                    () -> {
                        List<Vector3d> positions = c.getSegmentWorldPositions(
                                shipWorld.retrieveLoadedPhysicsEntities()
                        );
                        if (positions != null && !positions.isEmpty()) {
                            VStuffPackets.channel().send(
                                    PacketDistributor.PLAYER.with(() -> player),
                                    new PhysRopeSegmentsPacket(entry.getKey(), positions)
                            );
                        }
                    }
            ));
        }
    }

    public int allocateId() {
        return nextId++;
    }
}