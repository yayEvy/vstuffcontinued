package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.internal.RopeTypeManager;

public class UpdateRopeStylePacket extends SimplePacketBase {

    private Integer ropeId;
    private ResourceLocation newType;

    public UpdateRopeStylePacket(Integer ropeId, ResourceLocation newType) {
        this.ropeId = ropeId;
        this.newType = newType;
    }

    public UpdateRopeStylePacket(FriendlyByteBuf buffer) {
        this.ropeId = buffer.readInt();
        this.newType = buffer.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(ropeId);
        buffer.writeResourceLocation(newType);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRopeManager.updateClientRopeStyle(ropeId, RopeTypeManager.get(newType))));
        return true;
    }
}
