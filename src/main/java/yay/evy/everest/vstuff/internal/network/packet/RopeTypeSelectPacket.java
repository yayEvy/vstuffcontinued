package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;

import java.util.function.Supplier;

public class RopeTypeSelectPacket {

    private final ResourceLocation id;

    public RopeTypeSelectPacket(ResourceLocation id) {
        this.id = id;
    }

    public static void encode(RopeTypeSelectPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.id);
    }

    public static RopeTypeSelectPacket decode(FriendlyByteBuf buf) {
        return new RopeTypeSelectPacket(buf.readResourceLocation());
    }

    public static void handle(RopeTypeSelectPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RopeType.set(player, pkt.id);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
