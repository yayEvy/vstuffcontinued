package dev.flarelog.vstuff.content.ropes.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class RopeSegment {
    private final LocalPosAndBodyId pos0;
    private final LocalPosAndBodyId pos1;

    @Setter private List<Either<Integer, VSJoint>> joints = new CopyOnWriteArrayList<>();

    public Vector3d getRenderPos0(ClientLevel level) {
        return getRenderPos(level, pos0.id(), pos0.pos());
    }

    public Vector3d getRenderPos1(ClientLevel level) {
        return getRenderPos(level, pos1.id(), pos1.pos());
    }

    private Vector3d getRenderPos(ClientLevel level, Long id, Vector3d pos) {
        if (id == null) return pos; // ground body

        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        ClientVsBody shipOrBody = shipWorld.getAllBodies().getById(id);
        if (shipOrBody == null) return pos;

        return new Vector3d(shipOrBody.getRenderTransform().getToWorld().transformPosition(new Vector3d(pos)));
    }

    public static final Codec<RopeSegment> CODEC = RecordCodecBuilder.create(i -> i.group(
            LocalPosAndBodyId.CODEC.fieldOf("pos0").forGetter(RopeSegment::pos0),
            LocalPosAndBodyId.CODEC.fieldOf("pos1").forGetter(RopeSegment::pos1),
            Codec.INT.listOf().fieldOf("joints").forGetter(seg -> seg.joints().stream()
                    .map(e -> e.left().orElseThrow(() -> new IllegalStateException("joint not created yet")))
                    .toList())
    ).apply(i, (pos0, pos1, ids) -> {
        RopeSegment segment = new RopeSegment(pos0, pos1);
        segment.joints(ids.stream()
                .map(Either::<Integer, VSJoint>left)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new)));
        return segment;
    }));
}
