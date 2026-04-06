package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.client.ClientRopeManager;

import java.util.ArrayList;
import java.util.List;

public class PhysRopeSegmentsPacket extends SimplePacketBase {

    private final int ropeId;
    private final List<Vector3d> positions;

    public PhysRopeSegmentsPacket(int ropeId, List<Vector3d> positions) {
        this.ropeId = ropeId;
        this.positions = positions;
    }

    public PhysRopeSegmentsPacket(FriendlyByteBuf buf) {
        this.ropeId = buf.readInt();
        int count = buf.readInt();
        this.positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(ropeId);
        buf.writeInt(positions.size());
        for (Vector3d pos : positions) {
            buf.writeDouble(pos.x);
            buf.writeDouble(pos.y);
            buf.writeDouble(pos.z);
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientRopeManager.updatePhysRopeSegments(ropeId, positions)));
        return true;
    }
}