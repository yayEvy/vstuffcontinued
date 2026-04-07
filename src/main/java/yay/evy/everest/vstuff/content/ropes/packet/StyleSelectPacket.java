package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

public class StyleSelectPacket extends SimplePacketBase {

    private final ResourceLocation id;

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
        context.enqueueWork(() -> RopeStyle.set(context.getSender(), this.id));
        return true;
    }
}
