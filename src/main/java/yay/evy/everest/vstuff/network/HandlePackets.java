package yay.evy.everest.vstuff.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import yay.evy.everest.vstuff.VStuff;

public class HandlePackets {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel INSTANCE = null;
    private static int id = 0;

    public static void register() {
        if (INSTANCE != null) return;
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(VStuff.MOD_ID, "handle"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE.registerMessage(id++,
                StartHandleHoldPacket.class,
                StartHandleHoldPacket::encode,
                StartHandleHoldPacket::decode,
                StartHandleHoldPacket::handle);

        INSTANCE.registerMessage(id++,
                StopHandleHoldPacket.class,
                StopHandleHoldPacket::encode,
                StopHandleHoldPacket::decode,
                StopHandleHoldPacket::handle);
    }

    public static <MSG> void sendToServer(MSG message) {
        if (INSTANCE == null) return;
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        if (INSTANCE == null) return;
        INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                message
        );
    }
}
