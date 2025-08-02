package yay.evy.everest.vstuff.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.joml.Vector3d;

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
        INSTANCE.messageBuilder(ConstraintSyncPacket.class, packetId++)
                .decoder(ConstraintSyncPacket::new)
                .encoder(ConstraintSyncPacket::encode)
                .consumerMainThread(ConstraintSyncPacket::handle)
                .add();
    }

    public static void sendConstraintAdd(Integer constraintId, Long shipA, Long shipB,
                                         Vector3d localPosA, Vector3d localPosB, double maxLength) {
        ConstraintSyncPacket packet = new ConstraintSyncPacket(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendConstraintRemove(Integer constraintId) {
        ConstraintSyncPacket packet = new ConstraintSyncPacket(constraintId);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendConstraintRemoveToPlayer(ServerPlayer player, Integer constraintId) {
        ConstraintSyncPacket packet = new ConstraintSyncPacket(constraintId);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }


    public static void sendConstraintAddToPlayer(ServerPlayer player, Integer constraintId, Long shipA, Long shipB,
                                                 Vector3d localPosA, Vector3d localPosB, double maxLength) {
        ConstraintSyncPacket packet = new ConstraintSyncPacket(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    public static void sendClearAllConstraints() {
        ConstraintSyncPacket packet = new ConstraintSyncPacket();  // Will default to CLEAR_ALL
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendClearAllConstraintsToPlayer(ServerPlayer player) {
        ConstraintSyncPacket packet = new ConstraintSyncPacket();
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}
