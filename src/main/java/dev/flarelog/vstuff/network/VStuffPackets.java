package dev.flarelog.vstuff.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import dev.flarelog.vstuff.network.packets.misc.OutlinePacket;
import dev.flarelog.vstuff.network.packets.rope.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.network.packets.phys_grabber.GrabPacket;
import dev.flarelog.vstuff.network.packets.phys_grabber.ReleasePacket;
import dev.flarelog.vstuff.network.packets.phys_grabber.UpdatePacket;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public enum VStuffPackets {

    // to client
    OUTLINE(OutlinePacket.class, OutlinePacket::new, PLAY_TO_CLIENT),
    PHYS_ROPE_ADD(AddRopePacket.class, AddRopePacket::new, PLAY_TO_CLIENT),
    ROPE_REMOVE(RemoveRopePacket.class, RemoveRopePacket::new, PLAY_TO_CLIENT),
    ROPE_CLEAR_ALL(ClearAllRopesPacket.class, ClearAllRopesPacket::new, PLAY_TO_CLIENT),

    // to server
    STYLE_SELECT(StyleSelectPacket.class, StyleSelectPacket::new, PLAY_TO_SERVER),
    PHYS_GRABBER_GRAB(GrabPacket.class, GrabPacket::new, PLAY_TO_SERVER),
    PHYS_GRABBER_UPDATE(UpdatePacket.class, UpdatePacket::new, PLAY_TO_SERVER),
    PHYS_GRABBER_RELEASE(ReleasePacket.class, ReleasePacket::new, PLAY_TO_SERVER)
    ;

    public static SimpleChannel channel() {
        return channel;
    }

    public static final ResourceLocation CHANNEL_NAME = VStuff.asResource("main");
    public static final String VERSION_STR = "2";
    private static SimpleChannel channel;

    private PacketType<?> packetType;

    <T extends SimplePacketBase> VStuffPackets(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
        packetType = new PacketType<>(type, factory, direction);
    }

    public static void register() {
        channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
                .serverAcceptedVersions(VERSION_STR::equals)
                .clientAcceptedVersions(VERSION_STR::equals)
                .networkProtocolVersion(() -> VERSION_STR)
                .simpleChannel();

        for (VStuffPackets packet : values()) {
            packet.packetType.register();
        }
    }

    private static class PacketType<T extends SimplePacketBase> {
        private static int index = 0;

        private BiConsumer<T, FriendlyByteBuf> encoder;
        private Function<FriendlyByteBuf, T> decoder;
        private BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        private Class<T> type;
        private NetworkDirection direction;

        private PacketType(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
            encoder = T::write;
            decoder = factory;
            handler = (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                if (packet.handle(context)) {
                    context.setPacketHandled(true);
                }
            };
            this.type = type;
            this.direction = direction;
        }

        private void register() {
            channel().messageBuilder(type, index++, direction)
                    .encoder(encoder)
                    .decoder(decoder)
                    .consumerNetworkThread(handler)
                    .add();
        }
    }
}