package yay.evy.everest.vstuff.internal.network.newpackets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

public class StyleSelectPacket extends SimplePacketBase {

    private ResourceLocation id;

    public StyleSelectPacket(ResourceLocation id) {
        this.id = id;
    }

    public StyleSelectPacket(FriendlyByteBuf buffer) {
        this.id = buffer.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.id);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> RopeStyleManager.setStyle(context.getSender(), this.id));
        return true;
    }
}
