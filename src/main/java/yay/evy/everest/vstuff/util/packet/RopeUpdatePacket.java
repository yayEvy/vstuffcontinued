package yay.evy.everest.vstuff.util.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ropes.Rope;
import yay.evy.everest.vstuff.content.ropes.RopeTracker;

import java.util.function.Supplier;

public record RopeUpdatePacket(int ropeId, float newLength, String styleId) {

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(ropeId);
        buffer.writeFloat(newLength);
        buffer.writeUtf(styleId);
    }

    public static RopeUpdatePacket decode(FriendlyByteBuf buffer) {
        return new RopeUpdatePacket(buffer.readInt(), buffer.readFloat(), buffer.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            Rope rope = RopeTracker.getActiveRopes().get(ropeId);

            if (rope != null) {
                rope.updateFromEditor(level, newLength, styleId);

                VStuff.LOGGER.info("Rope {} updated by {}: Length {}, Style {}",
                        ropeId, player.getName().getString(), newLength, styleId);
            }
        });
        context.setPacketHandled(true);
    }

    public record RopePreviewPacket(int ropeId, float previewLength, String styleId) {
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;

                Rope rope = RopeTracker.getActiveRopes().get(ropeId);
                if (rope == null) return;

                if (rope.hasPhysicalImpact) {
                    rope.setJointLengthFromMenu(player.serverLevel(), previewLength);
                } else {
                    rope.renderLength = previewLength;
                }

                rope.setStyle(styleId);
                RopeTracker.syncAllConstraintsToPlayer(player);
            });
            ctx.get().setPacketHandled(true);
        }

    }
    }
