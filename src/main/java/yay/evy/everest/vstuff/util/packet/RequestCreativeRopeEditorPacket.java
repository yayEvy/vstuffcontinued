package yay.evy.everest.vstuff.util.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import yay.evy.everest.vstuff.content.ropes.Rope;
import yay.evy.everest.vstuff.content.ropes.RopeTracker;
import yay.evy.everest.vstuff.index.VStuffMenus;
import yay.evy.everest.vstuff.util.CreativeRopeEditorMenu;
import yay.evy.everest.vstuff.util.CreativeRopeEditorMenuProvider;

import java.util.function.Supplier;

public class RequestCreativeRopeEditorPacket {

    public final int ropeId;

    public RequestCreativeRopeEditorPacket() {
        this.ropeId = -1; // default
    }

    public RequestCreativeRopeEditorPacket(int ropeId) {
        this.ropeId = ropeId;
    }

    public static void encode(RequestCreativeRopeEditorPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.ropeId);
    }

    public static RequestCreativeRopeEditorPacket decode(FriendlyByteBuf buf) {
        return new RequestCreativeRopeEditorPacket(buf.readInt());
    }

    public static void handle(RequestCreativeRopeEditorPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.isCreative()) return;

            Rope rope = RopeTracker.getActiveRopes().get(pkt.ropeId);
            if (rope == null) return;

            NetworkHooks.openScreen(player, new CreativeRopeEditorMenuProvider(pkt.ropeId), buf -> {
                buf.writeInt(pkt.ropeId);
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
