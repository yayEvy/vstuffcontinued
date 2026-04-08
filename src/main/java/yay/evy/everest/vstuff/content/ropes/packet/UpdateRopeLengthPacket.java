package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.client.ClientRopeManager;

public class UpdateRopeLengthPacket extends SimplePacketBase {

    private Integer ropeId;
    private double newLength;

    public UpdateRopeLengthPacket(Integer ropeId, double newLength) {
        this.ropeId = ropeId;
        this.newLength = newLength;
    }

    public UpdateRopeLengthPacket(FriendlyByteBuf buffer) {
        this.ropeId = buffer.readInt();
        this.newLength = buffer.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(ropeId);
        buffer.writeDouble(newLength);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRopeManager.updateClientRopeLength(ropeId, newLength)));
        return true;
    }
}
