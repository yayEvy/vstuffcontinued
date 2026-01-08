package yay.evy.everest.vstuff.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.foundation.network.packets.RopeSyncPacket;
import yay.evy.everest.vstuff.foundation.network.packets.OutlinePacket;
import yay.evy.everest.vstuff.foundation.network.packets.RopeSoundPacket;
import yay.evy.everest.vstuff.util.RopeStyles;
import yay.evy.everest.vstuff.foundation.network.packets.RopeStyleSelectPacket;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("vstuff", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        INSTANCE.messageBuilder(RopeSyncPacket.class, packetId++)
                .decoder(RopeSyncPacket::new)
                .encoder(RopeSyncPacket::encode)
                .consumerMainThread(RopeSyncPacket::handle)
                .add();

        INSTANCE.messageBuilder(RopeSoundPacket.class, packetId++)
                .decoder(RopeSoundPacket::decode)
                .encoder(RopeSoundPacket::encode)
                .consumerMainThread(RopeSoundPacket::handle)
                .add();

        INSTANCE.messageBuilder(RopeStyleSelectPacket.class, packetId++)
                .decoder(RopeStyleSelectPacket::decode)
                .encoder(RopeStyleSelectPacket::encode)
                .consumerMainThread(RopeStyleSelectPacket::handle)
                .add();

        INSTANCE.messageBuilder(OutlinePacket.class, packetId++)
                .decoder(OutlinePacket::decode)
                .encoder(OutlinePacket::encode)
                .consumerMainThread(OutlinePacket::handle)
                .add();
    }



    public static void sendConstraintRerender(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength, RopeStyles.RopeStyle style){
        RopeSyncPacket packet = new RopeSyncPacket(constraintId, shipA, shipB, localPosA, localPosB, maxLength, style);
        INSTANCE.send(PacketDistributor.ALL.noArg(),packet );
    }
    public static void sendConstraintAdd(Integer constraintId, Long shipA, Long shipB,
                                         Vector3d localPosA, Vector3d localPosB, double maxLength, RopeStyles.RopeStyle style) {
        RopeSyncPacket packet = new RopeSyncPacket(constraintId, shipA, shipB, localPosA, localPosB, maxLength, style);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendConstraintRemove(Integer constraintId) {
        RopeSyncPacket packet = new RopeSyncPacket(constraintId);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendConstraintRemoveToPlayer(ServerPlayer player, Integer constraintId) {
        RopeSyncPacket packet = new RopeSyncPacket(constraintId);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }


    public static void sendConstraintAddToPlayer(ServerPlayer player, Integer constraintId, Long shipA, Long shipB,
                                                 Vector3d localPosA, Vector3d localPosB, double maxLength, RopeStyles.RopeStyle style) {
        RopeSyncPacket packet = new RopeSyncPacket(constraintId, shipA, shipB, localPosA, localPosB, maxLength, style);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    public static void sendClearAllConstraints() {
        RopeSyncPacket packet = new RopeSyncPacket();
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendClearAllConstraintsToPlayer(ServerPlayer player) {
        RopeSyncPacket packet = new RopeSyncPacket();
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendOutline(BlockPos pos, int color) {
        OutlinePacket packet = new OutlinePacket(pos, color);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendOutlineToPlayer(ServerPlayer player, BlockPos pos, int color) {
        OutlinePacket packet = new OutlinePacket(pos, color);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}