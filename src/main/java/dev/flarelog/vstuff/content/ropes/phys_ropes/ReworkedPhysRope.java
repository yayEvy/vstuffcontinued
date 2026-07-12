package dev.flarelog.vstuff.content.ropes.phys_ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.styling.data.RopeStyle;
import dev.flarelog.vstuff.internal.utility.records.RopePosData;
import dev.flarelog.vstuff.internal.utility.records.RopeSegment;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ReworkedPhysRope {

    Integer ropeId;
    public RopePosData posData0;
    public RopePosData posData1;
    public ResourceKey<RopeStyle> styleKey;
    List<Integer> jointIds;
    public List<RopeSegment> segments;

    protected ReworkedPhysRope(RopePosData posData0, RopePosData posData1, ResourceKey<RopeStyle> styleKey, List<RopeSegment> segments) {
        this.posData0 = posData0;
        this.posData1 = posData1;
        this.styleKey = styleKey;
        this.segments = segments;
    }

    public Integer getRopeId() {
        return ropeId;
    }

    public List<Integer> getJointIds() {
        return jointIds;
    }

    public ReworkedPhysRope setJointIds(LinkedList<Integer> jointIds) {
        this.jointIds = jointIds;
        return this;
    }

    public ReworkedPhysRope setRopeId(Integer to) {
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
