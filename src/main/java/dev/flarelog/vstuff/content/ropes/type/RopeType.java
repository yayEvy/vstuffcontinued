package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import org.apache.commons.jexl3.MapContext;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import java.util.*;

@SuppressWarnings("unused")
public record RopeType(ObjectNode endJoint, ObjectNode connectingJoint, boolean selfCollision) {

    public static RopeType create(Object joint, boolean selfCollision) {
        return create(joint, joint, selfCollision);
    }

    public static RopeType create(Object endJoint, Object connectingJoint, boolean selfCollision) {
        return new RopeType(
                VStuff.getOrCreateMapper().valueToTree(endJoint),
                VStuff.getOrCreateMapper().valueToTree(connectingJoint),
                selfCollision
        );
    }

    public static final Codec<RopeType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.OBJECT_NODE.fieldOf("endJoint").forGetter(RopeType::endJoint),
            CodecUtil.OBJECT_NODE.fieldOf("connectingJoint").forGetter(RopeType::connectingJoint),
            Codec.BOOL.fieldOf("selfCollision").forGetter(RopeType::selfCollision)
    ).apply(instance, RopeType::new));

    private static DataResult<ObjectNode> validate(ObjectNode n) {
        try {
            ExpressionParser.parseTree(n.deepCopy(), new MapContext(JointValuesContext.SAMPLE.toMap()));
            return DataResult.success(n);
        } catch (RuntimeException e) {
            return DataResult.error(() -> "Bad joint expression: " + e.getMessage());
        }
    }

    public VSJoint getEndJointWith(Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, Float maxDistance){
        return getJointWith(endJoint, shipId0, pose0, shipId1, pose1, maxDistance);
    }

    public VSJoint getConnectingPhysBodyJointWith(Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, Float maxDistance){
        return getJointWith(connectingJoint, shipId0, pose0, shipId1, pose1, maxDistance);
    }

    private static VSJoint getJointWith(ObjectNode value, Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, Float maxDistance){
        ObjectNode tree = VStuff.getOrCreateMapper().valueToTree(value);
        tree.put("shipId0", shipId0);
        tree.set("pose0", VStuff.getOrCreateMapper().valueToTree(pose0));
        tree.put("shipId1", shipId1);
        tree.set("pose1", VStuff.getOrCreateMapper().valueToTree(pose1));
        tree.put("maxDistance", maxDistance);

        return VStuff.getOrCreateMapper().convertValue(tree, VSJoint.class);
    }

}
