package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import java.util.*;

@SuppressWarnings("unused")
public record RopeType(Map<String, Object> endJoint, Map<String, Object> connectingPhysBodyJoint, boolean doTheVodiesCollideWithEachOther) {
    public RopeType(Object endJoint, Object connectingPhysBodyJoint, boolean doTheVodiesCollideWithEachOther) {
        this(ValkyrienSkiesMod.getVsCore().getStringMapper()
                        .convertValue(endJoint,
                                new TypeReference<Map<String, Object>>() {}),
                ValkyrienSkiesMod.getVsCore().getStringMapper()
                        .convertValue(connectingPhysBodyJoint,
                                new TypeReference<Map<String, Object>>() {}),
                doTheVodiesCollideWithEachOther);
    }

    public static final Codec<RopeType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.ANY_MAP_CODEC.fieldOf("endJoint").forGetter(RopeType::endJoint),
            CodecUtil.ANY_MAP_CODEC.fieldOf("connectingPhysBodyJoint").forGetter(RopeType::connectingPhysBodyJoint),
            Codec.BOOL.fieldOf("doTheVodiesCollideWithEachOther").forGetter(RopeType::doTheVodiesCollideWithEachOther)
    ).apply(instance, RopeType::new));

    public VSJoint getEndJointWith(long shipId0, VSJointPose pose0, long shipId1, VSJointPose pose1){
        return getWith(endJoint, shipId0, pose0, shipId1, pose1);
    }

    public VSJoint getConnectingPhysBodyJointWith(long shipId0, VSJointPose pose0, long shipId1, VSJointPose pose1){
        return getWith(connectingPhysBodyJoint, shipId0, pose0, shipId1, pose1);
    }

    private VSJoint getWith(Map<String, Object> value, long shipId0, VSJointPose pose0, long shipId1, VSJointPose pose1){
        Map<String, Object> map = new HashMap<>(value);
        map.put("shipId0", shipId0);
        map.put("pose0", pose0);
        map.put("shipId1", shipId1);
        map.put("pose1", pose1);
        return ValkyrienSkiesMod.getVsCore().getStringMapper().convertValue(map, VSJoint.class);
    }
}
