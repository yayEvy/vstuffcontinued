package dev.flarelog.vstuff.content.physics.physgrabber.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class GrabPacket extends SimplePacketBase {

    private long shipId;
    private double x, y, z;
    private double lx, ly, lz;
    private boolean creative;

    public GrabPacket(long shipId, Vec3 target, Vector3dc localHit, boolean creative) {
        this.shipId = shipId;
        this.x = target.x;
        this.y = target.y;
        this.z = target.z;
        this.lx = localHit.x();
        this.ly = localHit.y();
        this.lz = localHit.z();
        this.creative = creative;
    }

    public GrabPacket(FriendlyByteBuf buffer) {
        this.shipId = buffer.readLong();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.lx = buffer.readDouble();
        this.ly = buffer.readDouble();
        this.lz = buffer.readDouble();
        this.creative = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(shipId);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeDouble(lx);
        buffer.writeDouble(ly);
        buffer.writeDouble(lz);
        buffer.writeBoolean(creative);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> GrabberHandler.handleGrab(
                context.getSender(),
                shipId,
                new Vector3d(x, y, z),
                new Vector3d(lx, ly, lz),
                creative
        ));
        return true;
    }
}