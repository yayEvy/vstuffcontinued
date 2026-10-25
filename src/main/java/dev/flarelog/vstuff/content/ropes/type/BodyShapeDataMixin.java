package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.valkyrienskies.core.api.bodies.shape.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SphereBodyShapeData.class, name = "sphere"),
        @JsonSubTypes.Type(value = BoxBodyShapeData.class, name = "box"),
        @JsonSubTypes.Type(value = CapsuleBodyShapeData.class, name = "capsule"),
        @JsonSubTypes.Type(value = VoxelBodyShapeData.class, name = "voxel"),
        @JsonSubTypes.Type(value = CompoundBodyShapeData.class, name = "compound"),
        @JsonSubTypes.Type(value = WheelBodyShapeData.class, name = "wheel")
})
public abstract class BodyShapeDataMixin {}
