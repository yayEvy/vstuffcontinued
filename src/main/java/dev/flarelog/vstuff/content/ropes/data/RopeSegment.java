package dev.flarelog.vstuff.content.ropes.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ClientVsBody;
import org.valkyrienskies.core.internal.world.VsiClientShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public record RopeSegment(LocalPosAndBodyId pos0, LocalPosAndBodyId pos1) {
    
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
}
