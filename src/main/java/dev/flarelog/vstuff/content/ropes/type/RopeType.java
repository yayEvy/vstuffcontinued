package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import org.apache.commons.jexl3.MapContext;
import org.valkyrienskies.core.api.bodies.VsBodyDefaults;
import org.valkyrienskies.core.api.bodies.shape.BodyShapeData;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public record RopeType(List<ObjectNode> endJoints, List<ObjectNode> connectingJoints, BodyData bodyData, boolean selfCollision) {

    public static RopeType create(Object joint, BodyData bodyData, boolean selfCollision) {
        return create(joint, joint, bodyData, selfCollision);
    }

    public static RopeType create(Object endJoint, Object connectingJoint, BodyData bodyData, boolean selfCollision) {
        return new RopeType(
                List.of(VStuff.getOrCreateMapper().valueToTree(endJoint)),
                List.of(VStuff.getOrCreateMapper().valueToTree(connectingJoint)),
                bodyData,
                selfCollision
        );
    }

    public static final Codec<RopeType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.OBJECT_NODE.comapFlatMap(RopeType::validateJoint, Function.identity())
                    .listOf().fieldOf("endJoints").forGetter(RopeType::endJoints),
            CodecUtil.OBJECT_NODE.comapFlatMap(RopeType::validateJoint, Function.identity())
                    .listOf().fieldOf("connectingJoints").forGetter(RopeType::connectingJoints),
            BodyData.CODEC.fieldOf("bodyData").forGetter(RopeType::bodyData),
            Codec.BOOL.fieldOf("selfCollision").forGetter(RopeType::selfCollision)
    ).apply(instance, RopeType::new));

    private static DataResult<ObjectNode> validateJoint(ObjectNode n) {
        try {
            ExpressionParser.parseTree(n.deepCopy(), new MapContext(JointValuesContext.SAMPLE.toMap()));
            return DataResult.success(n);
        } catch (RuntimeException e) {
            return DataResult.error(() -> "Bad joint expression: " + e.getMessage());
        }
    }

    public List<VSJoint> getEndJointsWith(Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, JointValuesContext ctx){
        return endJoints.stream().map((o) -> getJointWith(o, shipId0, pose0, shipId1, pose1, ctx)).toList();
    }

    public List<VSJoint> getConnectingJointsWith(Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, JointValuesContext ctx){
        return connectingJoints.stream().map((o) -> getJointWith(o, shipId0, pose0, shipId1, pose1, ctx)).toList();
    }

    private static VSJoint getJointWith(ObjectNode value, Long shipId0, VSJointPose pose0, Long shipId1, VSJointPose pose1, JointValuesContext ctx){
        ObjectNode tree = ExpressionParser.parseTree(value.deepCopy(), new MapContext(ctx.toMap()));

        tree.put("shipId0", shipId0);
        tree.set("pose0", VStuff.getOrCreateMapper().valueToTree(pose0));
        tree.put("shipId1", shipId1);
        tree.set("pose1", VStuff.getOrCreateMapper().valueToTree(pose1));

        return VStuff.getOrCreateMapper().convertValue(tree, VSJoint.class);
    }

    public record BodyData(ObjectNode shape, double mass, double staticFriction, double dynamicFriction, double restitution){
        public static final Codec<BodyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecUtil.OBJECT_NODE.fieldOf("shape").forGetter(BodyData::shape),
                Codec.DOUBLE.fieldOf("mass").forGetter(BodyData::mass),
                Codec.DOUBLE.optionalFieldOf("staticFriction", VsBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT).forGetter(BodyData::staticFriction),
                Codec.DOUBLE.optionalFieldOf("dynamicFriction", VsBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT).forGetter(BodyData::dynamicFriction),
                Codec.DOUBLE.optionalFieldOf("restitution", VsBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT).forGetter(BodyData::restitution)
        ).apply(instance, BodyData::new));

        public BodyShapeData getShape() {
            return VStuff.getOrCreateMapper().convertValue(shape, BodyShapeData.class);
        }

        public static BodyData create(BodyShapeData shapeData, double mass, double staticFriction, double dynamicFriction, double restitution) {
            return new BodyData(
                    VStuff.getOrCreateMapper().valueToTree(shapeData),
                    mass,
                    staticFriction,
                    dynamicFriction,
                    restitution
            );
        }
    }

}
