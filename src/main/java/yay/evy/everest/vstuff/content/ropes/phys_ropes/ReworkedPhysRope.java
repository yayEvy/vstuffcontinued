package yay.evy.everest.vstuff.content.ropes.phys_ropes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.bodies.VsBody;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.core.internal.world.VsiShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.CodecUtil;
import yay.evy.everest.vstuff.internal.utility.records.RopePosData;
import yay.evy.everest.vstuff.internal.utility.records.RopeSegment;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
