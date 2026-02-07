package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.content.ropes.styler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.internal.RopeStyle;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.function.Supplier;

public class RopeStyleSelectPacket {

    private final ResourceLocation id;

    public RopeStyleSelectPacket(ResourceLocation id) {
        this.id = id;
    }

    public static void encode(RopeStyleSelectPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.id);
    }

    public static RopeStyleSelectPacket decode(FriendlyByteBuf buf) {
        return new RopeStyleSelectPacket(buf.readResourceLocation());
    }

    public static void handle(RopeStyleSelectPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RopeStyleHandlerServer.addStyle(player.getUUID(), pkt.id);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
