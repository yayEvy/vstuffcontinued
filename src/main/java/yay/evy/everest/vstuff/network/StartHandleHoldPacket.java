package yay.evy.everest.vstuff.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.content.handle.HandleBlockEntity;

import java.util.function.Supplier;

public class StartHandleHoldPacket {
    private final BlockPos pos;
    public StartHandleHoldPacket(BlockPos pos) { this.pos = pos; }
    public static void encode(StartHandleHoldPacket msg, FriendlyByteBuf buf) { buf.writeBlockPos(msg.pos); }
    public static StartHandleHoldPacket decode(FriendlyByteBuf buf) { return new StartHandleHoldPacket(buf.readBlockPos()); }

    public static void handle(StartHandleHoldPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;
            BlockEntity be = sender.level().getBlockEntity(msg.pos);
            if (be instanceof HandleBlockEntity handleBe) {
                handleBe.startHolding(sender);
            }
        });
        ctx.setPacketHandled(true);
    }
}
