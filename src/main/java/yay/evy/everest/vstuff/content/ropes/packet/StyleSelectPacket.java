package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

public class StyleSelectPacket extends SimplePacketBase {

    private final ResourceKey<RopeStyle> id;

    public StyleSelectPacket(ResourceKey<RopeStyle> id) {
        this.id = id;
    }

    public StyleSelectPacket(FriendlyByteBuf buffer) {
        this.id = buffer.readResourceKey(VStuffRegistries.ROPE_STYLE);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(id);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            RopeStyleManager.set(context.getSender(), this.id);
        });
        return true;
    }
}
