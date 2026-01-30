package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.content.ropestyler.handler.RopeStyleHandlerServer;
import yay.evy.everest.vstuff.internal.RopeStyles;

import java.util.function.Supplier;

public class RopeStyleSelectPacket {

    private final String styleId;

    public RopeStyleSelectPacket(String styleId) {
        this.styleId = styleId;
    }

    public static void encode(RopeStyleSelectPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.styleId);
    }

    public static RopeStyleSelectPacket decode(FriendlyByteBuf buf) {
        return new RopeStyleSelectPacket(buf.readUtf());
    }

    public static void handle(RopeStyleSelectPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RopeStyles.RopeStyle style = RopeStyles.fromString(pkt.styleId);
                if (style != null) {
                    RopeStyleHandlerServer.addStyle(player.getUUID(), style);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
