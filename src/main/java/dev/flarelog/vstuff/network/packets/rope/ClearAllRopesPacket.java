package dev.flarelog.vstuff.network.packets.rope;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import dev.flarelog.vstuff.client.ClientPhysRopeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ClearAllRopesPacket extends SimplePacketBase {

    public ClearAllRopesPacket() {}

    public ClearAllRopesPacket(FriendlyByteBuf buffer) {}

    @Override
    public void write(FriendlyByteBuf buffer) {}

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPhysRopeManager::clearAllClientConstraints));
        return true;
    }
}
