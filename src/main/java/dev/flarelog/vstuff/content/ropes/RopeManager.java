package dev.flarelog.vstuff.content.ropes;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import dev.flarelog.vstuff.client.ClientRopeStyle;
import dev.flarelog.vstuff.content.ropes.data.RopeSegment;
import dev.flarelog.vstuff.content.ropes.style.RopeStyleManager;
import dev.flarelog.vstuff.content.ropes.util.IRopeActor;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.network.packets.rope.AddRopePacket;
import dev.flarelog.vstuff.network.packets.rope.ClearAllRopesPacket;
import dev.flarelog.vstuff.network.packets.rope.RemoveRopePacket;
import dev.flarelog.vstuff.network.VStuffPackets;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.flarelog.vstuff.content.physics.VSUtil.getGTPA;
import static dev.flarelog.vstuff.content.ropes.RopeFactory.removeAndCleanupRope;

@RequiredArgsConstructor
public class RopeManager extends SavedData {

    private static final String DATA_NAME = "vstuff_phys_ropes";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String FORMAT_VERSION = "v1";

    private final ServerLevel level;

    private final AtomicInteger nextId = new AtomicInteger(0);

    private final ConcurrentHashMap<Integer, Rope> ropes = new ConcurrentHashMap<>();
    private final Set<Rope> queued = ConcurrentHashMap.newKeySet();

    public static RopeManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> RopeManager.load(tag, level), () -> new RopeManager(level), DATA_NAME);
    }

    public static RopeManager load(CompoundTag tag, ServerLevel level) {
        RopeManager data = new RopeManager(level);

        ListTag ropeList = tag.getList("ropes", Tag.TAG_COMPOUND);
        for (Tag ropeTag : ropeList) {

            Rope rope = CodecUtil.decodeFromTag(ropeTag, Rope.FULL_CODEC);

            data.ropes.put(rope.getRopeId(), rope);
        }
        data.nextId.set(tag.getInt("nextId"));
        VStuff.LOGGER.info("Loaded {} ropes from saved data. Format Version: {}", data.ropes.size(), tag.getString("formatVersion"));
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag ropeList = new ListTag();
        for (Map.Entry<Integer, Rope> entry : ropes.entrySet()) {
            ropeList.add(CodecUtil.encodeToTag(entry.getValue(), Rope.FULL_CODEC));
        }
        tag.put("ropes", ropeList);
        tag.putInt("nextId", nextId.get());
        tag.putString("formatVersion", FORMAT_VERSION);
        return tag;
    }


    private void addRope(Rope rope) {
        rope.setRopeId(nextId.incrementAndGet());

        ropes.put(rope.getRopeId(), rope);

        IRopeActor actor0 = IRopeActor.getAt(level, rope.getPosData0());
        IRopeActor actor1 = IRopeActor.getAt(level, rope.getPosData1());
        if (actor0 != null) {
            if (!actor0.canAttach()) {
                RopeFactory.removeAndCleanupRope(rope, level);
                return;
            }
            actor0.connectRope(rope.getRopeId());
        }

        if (actor1 != null) {
            if (!actor1.canAttach()) {
                RopeFactory.removeAndCleanupRope(rope, level);
                return;
            }
            actor1.connectRope(rope.getRopeId());
        }

        VStuffPackets.channel().send(PacketDistributor.ALL.noArg(), new AddRopePacket(rope));
        setDirty();
    }

    public void removeRope(Integer id) {
        Rope rope = ropes.remove(id);

        IRopeActor actor0 = IRopeActor.getAt(level, rope.getPosData0());
        IRopeActor actor1 = IRopeActor.getAt(level, rope.getPosData1());
        if (actor0 != null) {
            actor0.removeRope(id);
        }

        if (actor1 != null) {
            actor1.removeRope(id);
        }

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
            VStuffPackets.channel().send(PacketDistributor.PLAYER.with(() -> player), new AddRopePacket(rope));
        }
    }

    public void queue(Rope rope) {
        this.queued.add(rope);
    }

    public void physTick(PhysTickEvent event) {
        VsiPhysLevel physLevel = (VsiPhysLevel) event.getWorld();

        for (Rope rope : this.ropes.values()) {
            rope.physTick(physLevel);
        }

        for (Rope rope : new HashSet<>(this.queued)) {
            if (rope.allBodiesCreated(physLevel)) {
                if (createJoints(physLevel, rope)) {
                    if (!rope.getType().doTheVodiesCollideWithEachOther()) {
                        for (Long body : rope.getSegmentBodyIds()) {
                            for (Long id : rope.getSegmentBodyIds()) {
                                physLevel.disableCollisionBetween(body, id);
                            }
                        }
                    }
                    this.level.getServer().execute(() -> this.addRope(rope));
                }
                this.queued.remove(rope);
            }
        }
    }

    public static void physTicker(PhysTickEvent event) {
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(VSGameUtilsKt.getResourceKey(event.getWorld().getDimension()));
        if (level == null) return;
        RopeManager manager = RopeManager.get(level);
        manager.physTick(event);
    }

    private boolean createJoints(VsiPhysLevel level, Rope rope) {
        for (RopeSegment segment : rope.getSegments()) {
            VSJoint joint = segment.joint().right().orElseThrow();

            int id = level.addJoint(joint);
            if (id == -1) {
                LOGGER.warn("Invalid joint id received when creating phys rope! Discarding it....");
                this.cleanupRope(rope, level);
                return false;
            } else {
                segment.joint(Either.left(id));
            }
        }

        return true;
    }

    private void cleanupRope(Rope rope, VsiPhysLevel level) {
        if (rope.getRopeId() != null) {
            this.ropes.remove(rope.getRopeId());
        }
        for (RopeSegment segment : rope.getSegments()) {
            if (segment.joint().left().isPresent()) {
                level.removeJoint(segment.joint().left().get());
            }
        }
    }

}
