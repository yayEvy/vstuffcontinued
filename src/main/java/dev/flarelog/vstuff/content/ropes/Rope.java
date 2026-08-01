package dev.flarelog.vstuff.content.ropes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.util.LocalPosAndBodyId;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Rope {

    public static final Codec<Rope> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ropeId").forGetter(rope -> rope.ropeId),
            LocalPosAndBodyId.CODEC.fieldOf("posData0").forGetter(rope -> rope.posData0),
            LocalPosAndBodyId.CODEC.fieldOf("posData1").forGetter(rope -> rope.posData1),
            ResourceKey.codec(VStuffRegistries.ROPE_STYLE).fieldOf("style").forGetter(rope -> rope.styleKey),
            RopeSegment.CODEC.listOf().fieldOf("segments").forGetter(rope -> rope.segments),
            Codec.INT.listOf().fieldOf("jointIds").forGetter(rope -> new ArrayList<>(rope.getJointIds()))
    ).apply(instance, (ropeId, posData0, posData1, styleKey, segments, jointIds) -> {
        Rope rope = new Rope(posData0, posData1, null, styleKey, segments).setRopeId(ropeId);
        rope.setJointIds(new LinkedList<>(jointIds));
        return rope;
    }));

    public static final Codec<Rope> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ropeId").forGetter(rope -> rope.ropeId),
            LocalPosAndBodyId.CODEC.fieldOf("posData0").forGetter(rope -> rope.posData0),
            LocalPosAndBodyId.CODEC.fieldOf("posData1").forGetter(rope -> rope.posData1),
            RopeType.CODEC.fieldOf("type").forGetter(rope -> rope.type),
            ResourceKey.codec(VStuffRegistries.ROPE_STYLE).fieldOf("style").forGetter(rope -> rope.styleKey),
            RopeSegment.CODEC.listOf().fieldOf("segments").forGetter(rope -> rope.segments),
            Codec.INT.listOf().fieldOf("jointIds").forGetter(rope -> new ArrayList<>(rope.getJointIds()))
    ).apply(instance, (ropeId, posData0, posData1, type, styleKey, segments, jointIds) -> {
        Rope rope = new Rope(posData0, posData1, type, styleKey, segments).setRopeId(ropeId);
        rope.setJointIds(new LinkedList<>(jointIds));
        return rope;
    }));

    @Getter
    Integer ropeId;
    public LocalPosAndBodyId posData0;
    public LocalPosAndBodyId posData1;
    public RopeType type;
    public ResourceKey<RopeStyle> styleKey;
    @Getter
    List<Integer> jointIds;
    public List<RopeSegment> segments;

    protected Rope(LocalPosAndBodyId posData0, LocalPosAndBodyId posData1, RopeType type, ResourceKey<RopeStyle> styleKey, List<RopeSegment> segments) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.styleKey = styleKey;
        this.type = type;
        this.segments = segments;
    }

    public Rope setJointIds(LinkedList<Integer> jointIds) {
        this.jointIds = jointIds;
        return this;
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

}
