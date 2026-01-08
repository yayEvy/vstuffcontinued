package yay.evy.everest.vstuff.foundation.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import yay.evy.everest.vstuff.content.rope.roperework.NewRopeUtils;
import yay.evy.everest.vstuff.foundation.network.packets.OutlinePacket;
import yay.evy.everest.vstuff.foundation.network.packets.RopeSoundPacket;
import yay.evy.everest.vstuff.foundation.network.packets.RopeStyleSelectPacket;
import yay.evy.everest.vstuff.foundation.network.packets.RopeSyncPackets.*;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("vstuff", "rework"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        INSTANCE.messageBuilder(Add.class, packetId++)
                .decoder(Add::decode)
                .encoder(Add::encode)
                .consumerMainThread(Add::handle)
                .add();

        INSTANCE.messageBuilder(Remove.class, packetId++)
                .decoder(Remove::decode)
                .encoder(Remove::encode)
                .consumerMainThread(Remove::handle)
                .add();

        INSTANCE.messageBuilder(ClearAll.class, packetId++)
                .decoder(ClearAll::decode)
                .encoder(ClearAll::encode)
                .consumerMainThread(ClearAll::handle)
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



    public static void sendRopeRerender(Integer ropeId, NewRopeUtils.RopePosData posData0, NewRopeUtils.RopePosData posData1, float length, String style){
        Add packet = new Add(ropeId, posData0.localPos(), posData1.localPos(), posData0.shipId(), posData1.shipId(), length, style);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
    public static void sendRopeAdd(Integer ropeId, NewRopeUtils.RopePosData posData0, NewRopeUtils.RopePosData posData1, float length, String style){
        Add packet = new Add(ropeId, posData0.localPos(), posData1.localPos(), posData0.shipId(), posData1.shipId(), length, style);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendRopeAddToPlayer(ServerPlayer player, Integer ropeId, NewRopeUtils.RopePosData posData0, NewRopeUtils.RopePosData posData1, float length, String style) {
        Add packet = new Add(ropeId, posData0.localPos(), posData1.localPos(), posData0.shipId(), posData1.shipId(), length, style);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendRopeRemove(Integer ropeId) {
        Remove packet = new Remove(ropeId);
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendRopeRemoveToPlayer(ServerPlayer player, Integer ropeId) {
        Remove packet = new Remove(ropeId);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendClearAllRopes() {
        ClearAll packet = new ClearAll();
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendClearAllRopesToPlayer(ServerPlayer player) {
        ClearAll packet = new ClearAll();
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendOutlineToPlayer(ServerPlayer player, BlockPos pos, int color) {
        OutlinePacket packet = new OutlinePacket(pos, color);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

}