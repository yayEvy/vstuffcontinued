package yay.evy.everest.vstuff.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.constraint.ropes.AbstractRope;
import yay.evy.everest.vstuff.util.packet.*;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("vstuff", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static int pktId = 0;

    public static void registerPackets() {

        CHANNEL.messageBuilder(RopePacket.class, pktId++)
                .encoder(RopePacket::encode)
                .decoder(RopePacket::decode)
                .consumerMainThread(RopePacket::handle)
                .add();

        CHANNEL.messageBuilder(RopeSoundPacket.class, pktId++)
                .encoder(RopeSoundPacket::encode)
                .decoder(RopeSoundPacket::decode)
                .consumerMainThread(RopeSoundPacket::handle)
                .add();

        CHANNEL.messageBuilder(RopeStyleSelectPacket.class, pktId++)
                .encoder(RopeStyleSelectPacket::encode)
                .decoder(RopeStyleSelectPacket::decode)
                .consumerMainThread(RopeStyleSelectPacket::handle)
                .add();

        CHANNEL.messageBuilder(OutlinePacket.class, pktId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OutlinePacket::encode)
                .decoder(OutlinePacket::decode)
                .consumerMainThread(OutlinePacket::handle)
                .add();
    }


    public static void sendConstraintAdd(AbstractRope rope) {
        VStuff.LOGGER.info("[NetworkManager] sending constraintAdd");
        RopePacket packet = new RopePacket(RopePacket.Action.ADD, rope);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        VStuff.LOGGER.info("[NetworkManager] constraintAdd sent");
    }

    public static void sendConstraintRerender(AbstractRope rope) {
        VStuff.LOGGER.info("[NetworkManager] sending constraintRerender");
        RopePacket packet = new RopePacket(RopePacket.Action.RERENDER, rope);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        VStuff.LOGGER.info("[NetworkManager] constraintRerender sent");
    }

    public static void sendConstraintRemove(AbstractRope rope) {
        VStuff.LOGGER.info("[NetworkManager] sending constraintRemove");
        RopePacket packet = new RopePacket(RopePacket.Action.REMOVE, rope);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        VStuff.LOGGER.info("[NetworkManager] constraintRemove sent");
    }

    public static void sendClearAllConstraints() {
        VStuff.LOGGER.info("[NetworkManager] sending clearAllConstraints");
        RopePacket packet = new RopePacket();
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        VStuff.LOGGER.info("[NetworkManager] clearAllConstraints sent");
    }


    public static void sendConstraintAddToPlayer(ServerPlayer player, AbstractRope rope) {
        VStuff.LOGGER.info("[NetworkManager] Sending addConstraint to player {}", player.getName().getString());
        RopePacket packet = new RopePacket(RopePacket.Action.ADD, rope);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        VStuff.LOGGER.info("[NetworkManager] constraintAdd sent to player {}", player.getName().getString());
    }

    public static void sendConstraintRemoveToPlayer(ServerPlayer player, AbstractRope rope) {
        VStuff.LOGGER.info("[NetworkManager] Sending removeConstraint to player {}", player.getName().getString());
        RopePacket packet = new RopePacket(RopePacket.Action.REMOVE, rope);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        VStuff.LOGGER.info("[NetworkManager] constraintRemove sent to player {}", player.getName().getString());
    }

    public static void sendClearAllConstraintsToPlayer(ServerPlayer player) {
        VStuff.LOGGER.info("[NetworkManager] Sending clearAllConstraints to player {}", player.getName().getString());
        RopePacket packet = new RopePacket();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        VStuff.LOGGER.info("[NetworkManager] clearAllConstraints sent to player {}", player.getName().getString());
    }


    public static void sendOutlineToPlayer(ServerPlayer player, BlockPos pos, int color) {
        VStuff.LOGGER.info("[NetworkManager] sending outline to player {}", player.getName().getString());
        OutlinePacket packet = new OutlinePacket(pos, color);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        VStuff.LOGGER.info("[NetworkManager] outline sent to player {}", player.getName().getString());
    }

}