package yay.evy.everest.vstuff.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

    public static void registerPackets() {
        int pktId = 0;

        CHANNEL.registerMessage(pktId++, RopePacket.class,
                RopePacket::encode,
                RopePacket::decode,
                RopePacket::handle);

        CHANNEL.registerMessage(pktId++, RopeSoundPacket.class,
                RopeSoundPacket::encode,
                RopeSoundPacket::decode,
                RopeSoundPacket::handle);

        CHANNEL.registerMessage(pktId++, RopeStyleSelectPacket.class,
                RopeStyleSelectPacket::encode,
                RopeStyleSelectPacket::decode,
                RopeStyleSelectPacket::handle);

        CHANNEL.registerMessage(pktId++, OutlinePacket.class,
                OutlinePacket::encode,
                OutlinePacket::decode,
                OutlinePacket::handle);
    }


    public static void sendConstraintAdd(AbstractRope rope) {
        RopePacket packet = new RopePacket(RopePacket.Action.ADD, rope);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendConstraintRerender(AbstractRope rope) {
        RopePacket packet = new RopePacket(RopePacket.Action.RERENDER, rope);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendConstraintRemove(AbstractRope rope) {
        RopePacket packet = new RopePacket(RopePacket.Action.REMOVE, rope);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendClearAllConstraints() {
        RopePacket packet = new RopePacket();
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }


    public static void sendConstraintAddToPlayer(ServerPlayer player, AbstractRope rope) {
        RopePacket packet = new RopePacket(RopePacket.Action.ADD, rope);
        VStuff.LOGGER.info("[NetworkHandler] Sending addConstraint to player {}", player.getName());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendConstraintRemoveToPlayer(ServerPlayer player, AbstractRope rope) {
        VStuff.LOGGER.info("[NetworkHandler] Sending removeConstraint to player {}", player.getName().getString());
        RopePacket packet = new RopePacket(RopePacket.Action.REMOVE, rope);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendClearAllConstraintsToPlayer(ServerPlayer player) {
        VStuff.LOGGER.info("[NetworkHandler] Sending clearAllConstraints to player {}", player.getName());
        RopePacket packet = new RopePacket();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendOutline(BlockPos pos, int color) {
        OutlinePacket packet = new OutlinePacket(pos, color);
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendOutlineToPlayer(ServerPlayer player, BlockPos pos, int color) {
        OutlinePacket packet = new OutlinePacket(pos, color);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}