package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.client.ClientOutlineHandler;
import java.util.function.Supplier;

public class OutlinePacket {

    BlockPos pos;
    int color;

    public OutlinePacket(BlockPos pos, int color) {
        this.pos = pos;
        this.color = color;
    }

    public static void encode(OutlinePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.color);
    }

    public static OutlinePacket decode(FriendlyByteBuf buf) {
        return new OutlinePacket(
                buf.readBlockPos(),
                buf.readInt()
        );
    }

    public static void handle(OutlinePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientOutlineHandler.handleOutlinePacket(msg));
        ctx.get().setPacketHandled(true);
    }

    public BlockPos pos() { return pos; }
    public int color() { return color; }
}
