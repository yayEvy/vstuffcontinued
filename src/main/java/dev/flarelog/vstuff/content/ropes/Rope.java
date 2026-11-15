package dev.flarelog.vstuff.content.ropes;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.content.physics.VSUtil;
import dev.flarelog.vstuff.content.ropes.type.JointValuesContext;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.data.LocalPosAndBodyId;
import dev.flarelog.vstuff.content.ropes.data.RopeSegment;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.core.internal.ships.VsiQueryableShipData;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class Rope {
    public static final Codec<Rope> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ropeId").forGetter(Rope::getRopeId),
            LocalPosAndBodyId.CODEC.fieldOf("posData0").forGetter(Rope::getPosData0),
            LocalPosAndBodyId.CODEC.fieldOf("posData1").forGetter(Rope::getPosData1),
            RopeType.CODEC.fieldOf("type").forGetter(Rope::getType),
            ResourceKey.codec(VStuffRegistries.ROPE_STYLE).fieldOf("style").forGetter(Rope::getStyleKey),
            RopeSegment.CODEC.listOf().fieldOf("segments").forGetter(Rope::getSegments),
            Codec.FLOAT.fieldOf("length").forGetter(Rope::getLength)
    ).apply(instance, Rope::new));

    private Integer ropeId;
    private final LocalPosAndBodyId posData0;
    private final LocalPosAndBodyId posData1;
    private final RopeType type;
    private final ResourceKey<RopeStyle> styleKey;
    private final CopyOnWriteArrayList<RopeSegment> segments;
    private volatile float length;
    @Setter
    private boolean lengthDirty = false;

    protected Rope(Integer ropeId, LocalPosAndBodyId posData0, LocalPosAndBodyId posData1, RopeType type, ResourceKey<RopeStyle> styleKey, List<RopeSegment> segments, float length) {
         this(posData0, posData1, type, styleKey, new CopyOnWriteArrayList<>(segments), length);
         this.setRopeId(ropeId);
    }

    protected Rope(LocalPosAndBodyId posData0, LocalPosAndBodyId posData1, RopeType type, ResourceKey<RopeStyle> styleKey, CopyOnWriteArrayList<RopeSegment> segments, float length) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.styleKey = styleKey;
        this.type = type;
        this.segments = segments;
        this.length = length;
    }

    public Rope setRopeId(Integer to) {
        if (ropeId != null) {
            VStuff.LOGGER.warn("Blocking attempt to set ropeId when it has already been set.");
        } else {
            this.ropeId = Objects.requireNonNull(to, "Cannot set ropeId to a null value!");
        }

        return this;
    }

    public boolean atBlockPos(BlockPos blockPos) {
        Vector3d pos = VectorConversionsMCKt.toJOMLD(blockPos);
        return this.posData0.pos().equals(pos) || this.posData1.pos().equals(pos);
    }

    public RopeStyle getStyle(RegistryAccess regAccess) {
        return regAccess.registryOrThrow(VStuffRegistries.ROPE_STYLE).get(styleKey);
    }

    public void cleanup(ServerLevel level) {
        VsiServerShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        for (RopeSegment seg : getSegments()) {
            seg.joints().forEach(j -> j.left()
                    .ifPresent(id -> VSUtil.getGTPA(level).removeJoint(id)));
        }
        for (Long bodyId : this.getSegmentBodyIds()) {
            if (bodyId != null) {
                ServerVsBody body = shipWorld.getAllBodies().getById(bodyId);
                if (body != null && shipWorld.getAllBodies().contains(body)) {
                    shipWorld.deleteBody(body);
                }
            }
        }
        this.segments.clear();
    }

    public boolean isWorldToWorld() {
        return this.posData0.id() == null && this.posData1.id() == null;
    }

    public boolean isAttachedToShip(ServerLevel level) {
        VsiQueryableShipData<Ship> allShips = VSGameUtilsKt.getAllShips(level);
        return allShips.contains(this.posData0.id()) || allShips.contains(this.posData1.id());
    }

    public boolean isAttachedToBlockPos(@NotNull BlockPos pos, @NotNull ServerLevel level) {
        VStuff.LOGGER.warn("{} {} {}", pos, this.posData0.blockPos(level), this.posData1.blockPos(level));
        if (pos.equals(this.posData0.blockPos(level))) {
            return true;
        }
        return pos.equals(this.posData1.blockPos(level));
    }

    public boolean allJointsCreated() {
        for (RopeSegment segment : this.segments) {
            for (Either<Integer,VSJoint> s : segment.joints()) {
                if (s.left().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean allBodiesCreated(PhysLevel level) {
        for (RopeSegment segment : this.segments) {
            if (!segment.pos0().isWorld()) {
                if (level.getBodyById(segment.pos0().id()) == null) {
                    return false;
                }
            }
            if (!segment.pos1().isWorld()) {
                if (level.getBodyById(segment.pos1().id()) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Long> getSegmentBodyIds() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < segments.size() - 1; i++) {
            ids.add(segments.get(i).pos1().id());
        }
        return ids;
    }

    public void setLength(float length) {
        if (length == this.length) return;
        this.length = length;
        this.setLengthDirty(true);
    }

    public void physTick(VsiPhysLevel level) {
        if (!this.isLengthDirty()) return;
        if (!this.allJointsCreated()) return;

        this.setLengthDirty(false);
        int n = this.segments.size();
        JointValuesContext ctx = JointValuesContext.create(length / n, n);

        for (int i = 0; i < n; i++) {
            RopeSegment s = segments.get(i);
            updateSegmentJoints(level, s, buildJoints(s, i == 0 || i == n - 1, ctx));
        }
    }

    private void updateSegmentJoints(VsiPhysLevel level, RopeSegment s, List<VSJoint> joints) {
        for (int k = 0; k < joints.size(); k++) {
            level.updateJoint(s.joints().get(k).orThrow(), joints.get(k));
        }
    }

    private List<VSJoint> buildJoints(RopeSegment s, boolean end, JointValuesContext ctx) {
        VSJointPose p0 = new VSJointPose(s.pos0().pos(), new Quaterniond());
        VSJointPose p1 = new VSJointPose(s.pos1().pos(), new Quaterniond());
        List<VSJoint> raw = end
                ? type.getEndJointsWith(s.pos0().id(), p0, s.pos1().id(), p1, ctx)
                : type.getConnectingJointsWith(s.pos0().id(), p0, s.pos1().id(), p1, ctx);
        return raw.stream().map(VSJoint::serialized).toList();
    }
}
