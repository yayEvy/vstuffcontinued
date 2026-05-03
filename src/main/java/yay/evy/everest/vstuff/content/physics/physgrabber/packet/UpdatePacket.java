package yay.evy.everest.vstuff.content.physics.physgrabber.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;

public class UpdatePacket extends SimplePacketBase {

    private long shipId;
    private double x, y, z;
    private boolean creative;

    public UpdatePacket(long shipId, Vec3 vec3, boolean creative) {
        this(shipId, vec3.x, vec3.y, vec3.z, creative);
    }

    public UpdatePacket(long shipId, double x, double y, double z, boolean creative) {
        this.shipId = shipId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.creative = creative;
    }

    public UpdatePacket(FriendlyByteBuf buffer) {
        this.shipId = buffer.readLong();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.creative = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(shipId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeBoolean(creative);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> GrabberHandler.handleUpdate(context.getSender(), shipId, new Vector3d(x, y, z), creative));
        return true;
    }
}
