package dev.flarelog.vstuff.network.packets.rope;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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
        //context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientRopeManager.removeClientConstraint(ropeId)));
        return true;
    }
}
