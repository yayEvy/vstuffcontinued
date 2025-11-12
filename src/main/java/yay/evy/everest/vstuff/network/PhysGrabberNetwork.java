package yay.evy.everest.vstuff.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import yay.evy.everest.vstuff.VStuff;

import java.util.Optional;

public class PhysGrabberNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VStuff.MOD_ID, "phys_grabber"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(id++, PhysGrabberPackets.GrabPacket.class,
                PhysGrabberPackets.GrabPacket::encode,
                PhysGrabberPackets.GrabPacket::decode,
                PhysGrabberPackets.GrabPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(id++, PhysGrabberPackets.ReleasePacket.class,
                PhysGrabberPackets.ReleasePacket::encode,
                PhysGrabberPackets.ReleasePacket::decode,
                PhysGrabberPackets.ReleasePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(id++, PhysGrabberPackets.UpdatePacket.class,
                PhysGrabberPackets.UpdatePacket::encode,
                PhysGrabberPackets.UpdatePacket::decode,
                PhysGrabberPackets.UpdatePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        System.out.println("[PhysGrabberNetwork] Registered all packets");
    }

    public static void sendGrab(long shipId, Vec3 target) {
        CHANNEL.sendToServer(new PhysGrabberPackets.GrabPacket(
                shipId, target.x, target.y, target.z
        ));
    }

    public static void sendRelease(long shipId) {
        CHANNEL.sendToServer(new PhysGrabberPackets.ReleasePacket(shipId));
    }

    public static void sendUpdate(long shipId, Vec3 target) {
        CHANNEL.sendToServer(new PhysGrabberPackets.UpdatePacket(shipId, target.x, target.y, target.z));
    }
}
