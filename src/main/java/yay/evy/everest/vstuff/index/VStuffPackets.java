package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.packet.GrabPacket;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.packet.ReleasePacket;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.physgrabber.packet.UpdatePacket;
import yay.evy.everest.vstuff.content.ropes.packet.*;
import yay.evy.everest.vstuff.content.ropes.packet.OutlinePacket;
import yay.evy.everest.vstuff.content.ropes.packet.StyleSelectPacket;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public enum VStuffPackets {

    // server -> client packets
    OUTLINE(OutlinePacket.class, OutlinePacket::new, PLAY_TO_CLIENT),
    ROPE_ADD(AddRopePacket.class, AddRopePacket::new, PLAY_TO_CLIENT),
    ROPE_REMOVE(RemoveRopePacket.class, RemoveRopePacket::new, PLAY_TO_CLIENT),
    ROPE_LENGTH_UPDATE(UpdateRopeLengthPacket.class, UpdateRopeLengthPacket::new, PLAY_TO_CLIENT),
    ROPE_STYLE_UPDATE(UpdateRopeStylePacket.class, UpdateRopeStylePacket::new, PLAY_TO_CLIENT),
    ROPE_CLEAR_ALL(ClearAllRopesPacket.class, ClearAllRopesPacket::new, PLAY_TO_CLIENT),
    ROPE_STYLES_SYNC(SyncRopeStylesPacket.class, SyncRopeStylesPacket::new, PLAY_TO_CLIENT),
    ROPE_CATEGORIES_SYNC(SyncRopeCategoriesPacket.class, SyncRopeCategoriesPacket::new, PLAY_TO_CLIENT),
    ROPE_RESTYLES_SYNC(SyncRopeRestylesPacket.class, SyncRopeRestylesPacket::new, PLAY_TO_CLIENT),

    // client -> server packets
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
