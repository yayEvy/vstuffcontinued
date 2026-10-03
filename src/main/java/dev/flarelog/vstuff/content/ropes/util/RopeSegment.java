package dev.flarelog.vstuff.content.ropes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import dev.flarelog.vstuff.internal.utility.CodecUtil;

public record RopeSegment(LocalPosAndBodyId pos0, LocalPosAndBodyId pos1) {

    private static final Long NOID = -1L;

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

    public static final Codec<RopeSegment> CODEC = RecordCodecBuilder.create(i -> i.group( // codec of doom and despair part 2
            LocalPosAndBodyId.CODEC.fieldOf("pos0").forGetter(RopeSegment::pos0),
            LocalPosAndBodyId.CODEC.fieldOf("pos1").forGetter(RopeSegment::pos1)
    ).apply(i, RopeSegment::new));

//    private static final Codec<Long> ID = Codec.LONG.xmap(
//            idTo -> idTo,
//            idFrom -> idFrom == null ? -1L : idFrom
//    );
//
//    public static final Codec<RopeSegment> CODEC = RecordCodecBuilder.create(i -> i.group( // codec of doom and despair part 2
//            ID.fieldOf("id0").forGetter(RopeSegment::id0),
//            ID.fieldOf("id1").forGetter(RopeSegment::id1),
//            CodecUtil.VECTOR3D.fieldOf("pos0").forGetter(RopeSegment::pos0),
//            CodecUtil.VECTOR3D.fieldOf("pos1").forGetter(RopeSegment::pos1)
//    ).apply(i, RopeSegment::new));

    public static RopeSegment readJsonFromBuffer(FriendlyByteBuf buf) {
        return buf.readJsonWithCodec(CODEC);
//        RopeSegment segmentNonNull = buf.readJsonWithCodec(RopeSegment.CODEC);
//        return new RopeSegment(
//                segmentNonNull.id0 == -1 ? null : segmentNonNull.id0,
//                segmentNonNull.id1 == -1 ? null : segmentNonNull.id1,
//                segmentNonNull.pos0,
//                segmentNonNull.pos1
//        );
    }

    public static void writeJsonToBuffer(FriendlyByteBuf buf, RopeSegment segment) {
        buf.writeJsonWithCodec(CODEC, segment);
    }

}
