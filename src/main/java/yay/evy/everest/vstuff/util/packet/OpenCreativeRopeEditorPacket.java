package yay.evy.everest.vstuff.util.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import yay.evy.everest.vstuff.content.ropes.Rope;
import yay.evy.everest.vstuff.content.ropes.RopeTracker;
import yay.evy.everest.vstuff.util.CreativeRopeEditorMenuProvider;

import java.util.function.Supplier;

public class OpenCreativeRopeEditorPacket {

    private final int constraintId;

    public OpenCreativeRopeEditorPacket(int constraintId) {
        this.constraintId = constraintId;
    }

    public static void encode(OpenCreativeRopeEditorPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.constraintId);
    }

    public static OpenCreativeRopeEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenCreativeRopeEditorPacket(buf.readInt());
    }

    public static void handle(OpenCreativeRopeEditorPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isCreative()) return;

            Rope rope = RopeTracker.getActiveRopes().get(pkt.constraintId);
            if (rope == null) return;

            NetworkHooks.openScreen(
                    player,
                    new CreativeRopeEditorMenuProvider(pkt.constraintId),
                    buf -> buf.writeInt(pkt.constraintId)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
