package yay.evy.everest.vstuff.content.physics.physgrabber.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ReleasePacket extends SimplePacketBase {

    private long shipId;

    public ReleasePacket(long shipId) {
        this.shipId = shipId;
    }

    public ReleasePacket(FriendlyByteBuf buffer) {
        this.shipId = buffer.readLong();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeLong(shipId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> GrabberHandler.handleRelease(context.getSender(), shipId));
        return true;
    }
}
