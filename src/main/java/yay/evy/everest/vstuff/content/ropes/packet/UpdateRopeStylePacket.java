package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.client.ClientRopeStyle;

public class UpdateRopeStylePacket extends SimplePacketBase {

    private Integer ropeId;
    private ClientRopeStyle newStyle;

    public UpdateRopeStylePacket(Integer ropeId, ClientRopeStyle newStyle) {
        this.ropeId = ropeId;
        this.newStyle = newStyle;
    }

    public UpdateRopeStylePacket(FriendlyByteBuf buffer) {
        this.ropeId = buffer.readInt();
        this.newStyle = buffer.readJsonWithCodec(ClientRopeStyle.CODEC);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(ropeId);
        buffer.writeJsonWithCodec(ClientRopeStyle.CODEC, newStyle);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRopeManager.updateClientRopeStyle(ropeId, newStyle)));
        return true;
    }
}
