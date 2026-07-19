package dev.flarelog.vstuff.network.packets.rope;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClearAllRopesPacket extends SimplePacketBase {

    public ClearAllRopesPacket() {}

    public ClearAllRopesPacket(FriendlyByteBuf buffer) {}

    @Override
    public void write(FriendlyByteBuf buffer) {}

    @Override
    public boolean handle(NetworkEvent.Context context) {
        //context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientRopeManager::clearAllClientConstraints));
        return true;
    }
}
