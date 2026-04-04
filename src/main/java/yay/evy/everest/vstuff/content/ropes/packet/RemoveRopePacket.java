package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.client.ClientRopeManager;

public class RemoveRopePacket extends SimplePacketBase {

    private Integer ropeId;

    public RemoveRopePacket(Integer ropeId) {
        this.ropeId = ropeId;
    }

    public RemoveRopePacket(FriendlyByteBuf buffer) {
        this.ropeId = buffer.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(ropeId);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRopeManager.removeClientConstraint(ropeId)));
        return true;
    }
}
