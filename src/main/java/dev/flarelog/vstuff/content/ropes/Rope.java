package dev.flarelog.vstuff.content.ropes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.content.ropes.type.RopeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.util.RopePosData;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Rope {

    public static final Codec<Rope> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ropeId").forGetter(rope -> rope.ropeId),
            RopePosData.CODEC.fieldOf("posData0").forGetter(rope -> rope.posData0),
            RopePosData.CODEC.fieldOf("posData1").forGetter(rope -> rope.posData1),
            ResourceKey.codec(VStuffRegistries.ROPE_TYPE).fieldOf("type").forGetter(rope -> rope.typeKey),
            ResourceKey.codec(VStuffRegistries.ROPE_STYLE).fieldOf("style").forGetter(rope -> rope.styleKey),
            RopeSegment.CODEC.listOf().fieldOf("segments").forGetter(rope -> rope.segments),
            Codec.INT.listOf().fieldOf("jointIds").forGetter(rope -> new ArrayList<>(rope.getJointIds()))
    ).apply(instance, (ropeId, posData0, posData1, typeKey, styleKey, segments, jointIds) -> {
        Rope rope = new Rope(posData0, posData1, typeKey, styleKey, segments).setRopeId(ropeId);
        rope.setJointIds(new LinkedList<>(jointIds));
        return rope;
    }));

    Integer ropeId;
    public RopePosData posData0;
    public RopePosData posData1;
    public ResourceKey<RopeType> typeKey;
    public ResourceKey<RopeStyle> styleKey;
    List<Integer> jointIds;
    public List<RopeSegment> segments;

    protected Rope(RopePosData posData0, RopePosData posData1, ResourceKey<RopeType> typeKey, ResourceKey<RopeStyle> styleKey, List<RopeSegment> segments) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.styleKey = styleKey;
        this.typeKey = typeKey;
        this.segments = segments;
    }

    public Integer getRopeId() {
        return ropeId;
    }

    public List<Integer> getJointIds() {
        return jointIds;
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
        return this.posData0.blockPos().equals(blockPos) || this.posData1.blockPos().equals(blockPos);
    }

    public RopeStyle getStyle(RegistryAccess regAccess) {
        return regAccess.registryOrThrow(VStuffRegistries.ROPE_STYLE).get(styleKey);
    }

}
