package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import java.util.*;

@SuppressWarnings("unused")
public record RopeType(Map<String, Object> endJoint, Map<String, Object> connectingJoint, boolean selfCollision) {

    public static RopeType create(Object joint, boolean selfCollision) {
        return create(joint, joint, selfCollision);
    }

    public static RopeType create(Object endJoint, Object connectingJoint, boolean selfCollision) {
        Map<String, Object> endJointMap = ValkyrienSkiesMod.getVsCore().getStringMapper()
                .convertValue(endJoint,
                        new TypeReference<Map<String, Object>>() {});

        Map<String, Object> connectingJointMap = ValkyrienSkiesMod.getVsCore().getStringMapper()
                .convertValue(connectingJoint,
                        new TypeReference<Map<String, Object>>() {});
        return new RopeType(endJointMap, connectingJointMap, selfCollision);
    }

    public static final Codec<RopeType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.ANY_MAP_CODEC.fieldOf("endJoint").forGetter(RopeType::endJoint),
            CodecUtil.ANY_MAP_CODEC.fieldOf("connectingJoint").forGetter(RopeType::connectingJoint),
            Codec.BOOL.fieldOf("selfCollision").forGetter(RopeType::selfCollision)
    ).apply(instance, RopeType::new));

    public VSJoint getEndJointWith(Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, Float maxDistance){
        return getJointWith(endJoint, shipId0, pose0, shipId1, pose1, maxDistance);
    }

    public VSJoint getConnectingPhysBodyJointWith(Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, Float maxDistance){
        return getJointWith(connectingJoint, shipId0, pose0, shipId1, pose1, maxDistance);
    }

    private static VSJoint getJointWith(Map<String, Object> value, Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, Float maxDistance){
        Map<String, Object> map = new HashMap<>(value);
        map.put("shipId0", shipId0);
        map.put("pose0", pose0);
        map.put("shipId1", shipId1);
        map.put("pose1", pose1);
        map.put("maxDistance", maxDistance); // for distance joint
        return VStuff.getOrCreateMapper().convertValue(map, VSJoint.class);
    }

}
