package yay.evy.everest.vstuff.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.content.handle.HandleBlockEntity;

import java.util.function.Supplier;

public class StopHandleHoldPacket {

    private final BlockPos pos;

    public StopHandleHoldPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(StopHandleHoldPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static StopHandleHoldPacket decode(FriendlyByteBuf buf) {
        return new StopHandleHoldPacket(buf.readBlockPos());
    }

    public static void handle(StopHandleHoldPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                net.minecraft.server.level.ServerPlayer sender = context.getSender();

                BlockEntity be = sender.level().getBlockEntity(msg.pos);

                if (be instanceof HandleBlockEntity handleBe) {
                    handleBe.stopHolding(sender);
                }
            }
        });
        context.setPacketHandled(true);
    }
}