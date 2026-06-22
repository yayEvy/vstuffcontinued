package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.content.ropes.packet.PhysRopePosPacket;
import yay.evy.everest.vstuff.index.VStuffPackets;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.TagUtils;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PhysRopeManager extends SavedData {

    private static final String DATA_KEY = "vstuff_phys_ropes";

    private final Map<Integer, PhysRope> ropes = new HashMap<>();
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private PhysRopeManager() {}

    public static PhysRopeManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> load(level, tag),
                PhysRopeManager::new,
                DATA_KEY
        );
    }
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (PhysRope rope : ropes.values()) {
            try {
                CompoundTag ropeTag = new CompoundTag();
                ropeTag.put("pos0", savePosData(rope.posData0));
                ropeTag.put("pos1", savePosData(rope.posData1));
                ropeTag.put("style", TagUtils.writeResourceKey(rope.styleKey));
                ropeTag.putFloat("segLen", rope.segmentLength);

                ListTag segList = new ListTag();
                for (Vector3d p : rope.lastKnownSegmentPositions) {
                    CompoundTag pt = new CompoundTag();
                    pt.putDouble("x", p.x);
                    pt.putDouble("y", p.y);
                    pt.putDouble("z", p.z);
                    segList.add(pt);
                }
                ropeTag.put("segPositions", segList);

                list.add(ropeTag);
            } catch (Exception e) {
                VStuff.LOGGER.error("PhysRopeManager: failed to save rope {}: {}", rope.physRopeId, e.getMessage());
            }
        }
        tag.put("ropes", list);
        return tag;
    }

    public static PhysRopeManager load(ServerLevel level, CompoundTag tag) {
        PhysRopeManager manager = new PhysRopeManager();
        ListTag list = tag.getList("ropes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            try {
                CompoundTag ropeTag = list.getCompound(i);

                CompoundTag p0tag = ropeTag.getCompound("pos0");
                CompoundTag p1tag = ropeTag.getCompound("pos1");

                Long shipId0 = p0tag.contains("shipId") ? p0tag.getLong("shipId") : null;
                Long shipId1 = p1tag.contains("shipId") ? p1tag.getLong("shipId") : null;

                BlockPos bp0 = new BlockPos(p0tag.getInt("bx"), p0tag.getInt("by"), p0tag.getInt("bz"));
                BlockPos bp1 = new BlockPos(p1tag.getInt("bx"), p1tag.getInt("by"), p1tag.getInt("bz"));

                RopePosData pos0 = RopePosData.create(level, shipId0, bp0);
                RopePosData pos1 = RopePosData.create(level, shipId1, bp1);

                ResourceKey<RopeStyle> styleKey = TagUtils.readResourceKey(ropeTag.getCompound("style"));

                float segLen = ropeTag.getFloat("segLen");

                ListTag segList = ropeTag.getList("segPositions", Tag.TAG_COMPOUND);
                List<Vector3d> savedPositions = new ArrayList<>();
                for (int j = 0; j < segList.size(); j++) {
                    CompoundTag pt = segList.getCompound(j);
                    savedPositions.add(new Vector3d(pt.getDouble("x"), pt.getDouble("y"), pt.getDouble("z")));
                }

                if (!savedPositions.isEmpty()) {
                    PhysRopeFactory.createPhysRopeAtPositions(level, pos0, pos1, styleKey, savedPositions, segLen, null);
                } else {
                    PhysRopeFactory.createPhysRope(level, pos0, pos1, styleKey, null);
                }
            } catch (Exception e) {
                VStuff.LOGGER.error("PhysRopeManager: failed to load rope {}: {}", i, e.getMessage());
            }
        }
        return manager;
    }

    private static CompoundTag savePosData(RopePosData pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isWorld", pos.isWorld());
        if (pos.shipId() != null) tag.putLong("shipId", pos.shipId());
        tag.putInt("bx", pos.blockPos().getX());
        tag.putInt("by", pos.blockPos().getY());
        tag.putInt("bz", pos.blockPos().getZ());
        return tag;
    }


    public void addPhysRope(PhysRope rope) {
        rope.physRopeId = ID_COUNTER.getAndIncrement();
        ropes.put(rope.physRopeId, rope);
        setDirty();
        VStuff.LOGGER.debug("PhysRopeManager: added phys rope {}.", rope.physRopeId);
    }

    public void removePhysRope(int id) {
        PhysRope removed = ropes.remove(id);
        if (removed != null) {
            setDirty();
            VStuff.LOGGER.debug("PhysRopeManager: removed phys rope {}.", id);
        }
    }

    public PhysRope getRope(int id) {
        return ropes.get(id);
    }

    public Collection<PhysRope> getAllRopes() {
        return ropes.values();
    }

    public static void syncAllPhysRopesToPlayer(ServerPlayer player) {
        VStuff.LOGGER.debug("PhysRopeManager: syncAllPhysRopesToPlayer called for {} — implement sync packet.", player.getName().getString());
    }

    public static void destroyAll(ServerLevel level) {
        PhysRopeManager manager = get(level);
        for (PhysRope rope : manager.ropes.values()) {
            PhysRopeFactory.destroyPhysRope(level, rope);
        }
        manager.ropes.clear();
        manager.setDirty();
    }

    public static void tickPhysRopeClientPositions(ServerLevel level, PhysRope rope) {
        VsiServerShipWorld shipWorld = (VsiServerShipWorld) VSGameUtilsKt.getShipObjectWorld(level);
        if (shipWorld == null) return;

        List<Long> segBodyIds = rope.segmentBodies.stream()
                .map(ServerVsBody::getId)
                .toList();

        rope.lastKnownSegmentPositions.clear();
        for (Long id : segBodyIds) {
            rope.lastKnownSegmentPositions.add(PhysRopeFactory.getSegmentWorldPos(shipWorld, id));
        }

        int totalJoints = rope.clientRopeIds.size();

        for (int i = 0; i < totalJoints; i++) {
            Integer clientId = rope.clientRopeIds.get(i);

            boolean hasSegStart = i > 0;
            boolean hasSegEnd = i < totalJoints - 1;
            if (!hasSegStart && !hasSegEnd) continue;

            Long bodyId0 = hasSegStart ? segBodyIds.get(i - 1) : null;
            Long bodyId1 = hasSegEnd ? segBodyIds.get(i) : null;

            Vector3d lp0  = bodyId0 != null ? PhysRopeFactory.getSegmentWorldPos(shipWorld, bodyId0) : null;
            Vector3d vel0 = bodyId0 != null ? PhysRopeFactory.getSegmentVelocity(shipWorld, bodyId0) : null;
            Vector3d lp1  = bodyId1 != null ? PhysRopeFactory.getSegmentWorldPos(shipWorld, bodyId1) : null;
            Vector3d vel1 = bodyId1 != null ? PhysRopeFactory.getSegmentVelocity(shipWorld, bodyId1) : null;

            if (lp0 != null || lp1 != null) {
                VStuffPackets.channel().send(
                        PacketDistributor.ALL.noArg(),
                        new PhysRopePosPacket(clientId, level.getGameTime(), lp0, vel0, lp1, vel1)
                );
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Ticker {

        @SubscribeEvent
        public static void onServerTick(TickEvent.LevelTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (!(event.level instanceof ServerLevel level)) return;

            PhysRopeManager manager = get(level);
            for (PhysRope rope : manager.ropes.values()) {
                tickPhysRopeClientPositions(level, rope);
            }
        }
    }
}