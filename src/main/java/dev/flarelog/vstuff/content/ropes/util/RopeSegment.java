package dev.flarelog.vstuff.content.ropes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import dev.flarelog.vstuff.internal.utility.CodecUtil;

public record RopeSegment(@Nullable Long id0, @Nullable Long id1, Vector3d pos0, Vector3d pos1) {

    private static final Long NOID = -1L;

    public Vector3d pos0(ClientLevel level) {
        return getPos(level, id0, pos0);
    }

    public Vector3d pos1(ClientLevel level) {
        return getPos(level, id1, pos1);
    }

    private Vector3d getPos(ClientLevel level, Long id, Vector3d pos) {
        if (id == null) return pos; // ground body

        VsiClientShipWorld shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

        ClientVsBody shipOrBody = shipWorld.getAllBodies().getById(id);
        if (shipOrBody == null) return pos;

        return new Vector3d(shipOrBody.getRenderTransform().getToWorld().transformPosition(new Vector3d(pos)));
    }

    private static final Codec<Long> ID = Codec.LONG.xmap(
            idTo -> idTo,
            idFrom -> idFrom == null ? -1L : idFrom
    );

    public static final Codec<RopeSegment> CODEC = RecordCodecBuilder.create(i -> i.group( // codec of doom and despair part 2
            ID.fieldOf("id0").forGetter(RopeSegment::id0),
            ID.fieldOf("id1").forGetter(RopeSegment::id1),
            CodecUtil.VECTOR3D.fieldOf("pos0").forGetter(RopeSegment::pos0),
            CodecUtil.VECTOR3D.fieldOf("pos1").forGetter(RopeSegment::pos1)
    ).apply(i, RopeSegment::new));

    public static RopeSegment readJsonFromBuffer(FriendlyByteBuf buf) {
        RopeSegment segmentNonNull = buf.readJsonWithCodec(RopeSegment.CODEC);
        return new RopeSegment(
                segmentNonNull.id0 == -1 ? null : segmentNonNull.id0,
                segmentNonNull.id1 == -1 ? null : segmentNonNull.id1,
                segmentNonNull.pos0,
                segmentNonNull.pos1
        );
    }

    public static void writeJsonToBuffer(FriendlyByteBuf buf, RopeSegment segment) {
        buf.writeJsonWithCodec(CODEC, segment);
    }

}
