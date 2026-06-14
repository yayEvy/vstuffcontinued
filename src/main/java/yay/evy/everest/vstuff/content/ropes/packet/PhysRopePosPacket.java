package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.client.ClientRopeManager;

public class PhysRopePosPacket extends SimplePacketBase {
    private final int ropeId;
    private final long sequence;
    private final boolean hasPos0, hasPos1;
    private final Vector3d pos0, vel0, pos1, vel1;

    public PhysRopePosPacket(int ropeId, long sequence,
                             Vector3d pos0, Vector3d vel0,
                             Vector3d pos1, Vector3d vel1) {
        this.ropeId   = ropeId;
        this.sequence = sequence;
        this.hasPos0  = pos0 != null;
        this.hasPos1  = pos1 != null;
        this.pos0 = pos0 != null ? pos0 : new Vector3d();
        this.vel0 = vel0 != null ? vel0 : new Vector3d();
        this.pos1 = pos1 != null ? pos1 : new Vector3d();
        this.vel1 = vel1 != null ? vel1 : new Vector3d();
    }

    public PhysRopePosPacket(FriendlyByteBuf buf) {
        this.ropeId   = buf.readInt();
        this.sequence = buf.readLong();
        this.hasPos0  = buf.readBoolean();
        this.pos0 = hasPos0 ? new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()) : new Vector3d();
        this.vel0 = hasPos0 ? new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()) : new Vector3d();
        this.hasPos1  = buf.readBoolean();
        this.pos1 = hasPos1 ? new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()) : new Vector3d();
        this.vel1 = hasPos1 ? new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()) : new Vector3d();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(ropeId);
        buf.writeLong(sequence);
        buf.writeBoolean(hasPos0);
        if (hasPos0) {
            buf.writeDouble(pos0.x); buf.writeDouble(pos0.y); buf.writeDouble(pos0.z);
            buf.writeDouble(vel0.x); buf.writeDouble(vel0.y); buf.writeDouble(vel0.z);
        }
        buf.writeBoolean(hasPos1);
        if (hasPos1) {
            buf.writeDouble(pos1.x); buf.writeDouble(pos1.y); buf.writeDouble(pos1.z);
            buf.writeDouble(vel1.x); buf.writeDouble(vel1.y); buf.writeDouble(vel1.z);
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() ->
                ClientRopeManager.updateClientRopePositions(ropeId, sequence,
                        hasPos0 ? pos0 : null, hasPos0 ? vel0 : null,
                        hasPos1 ? pos1 : null, hasPos1 ? vel1 : null)
        );
        return true;
    }
}