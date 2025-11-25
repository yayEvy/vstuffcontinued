package yay.evy.everest.vstuff.network;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.physgrabber.PhysGrabberServerAttachment;

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
                PhysGrabberPackets.GrabPacket::handle
        );

        CHANNEL.registerMessage(id++, PhysGrabberPackets.ReleasePacket.class,
                PhysGrabberPackets.ReleasePacket::encode,
                PhysGrabberPackets.ReleasePacket::decode,
                PhysGrabberPackets.ReleasePacket::handle
        );

        CHANNEL.registerMessage(id++, PhysGrabberPackets.UpdatePacket.class,
                PhysGrabberPackets.UpdatePacket::encode,
                PhysGrabberPackets.UpdatePacket::decode,
                PhysGrabberPackets.UpdatePacket::handle
        );
    }


    public static void sendGrab(long shipId, Vec3 target, boolean creative) {
        CHANNEL.sendToServer(new PhysGrabberPackets.GrabPacket(
                shipId, target.x, target.y, target.z, creative
        ));
    }

    public static void sendRelease(long shipId) {
        CHANNEL.sendToServer(new PhysGrabberPackets.ReleasePacket(shipId));
    }

    public static void sendUpdate(long shipId, Vec3 target, boolean creative) {
        CHANNEL.sendToServer(new PhysGrabberPackets.UpdatePacket(
                shipId, target.x, target.y, target.z, creative
        ));
    }
    public static void handleGrabRequest(ServerPlayer player, long shipId, Vec3 target, boolean creative) {
        LoadedServerShip ship = VSGameUtilsKt.getAllShips(player.level())
                .stream()
                .filter(s -> s.getId() == shipId)
                .filter(s -> s instanceof LoadedServerShip)
                .map(s -> (LoadedServerShip) s)
                .findFirst()
                .orElse(null);

        if (ship == null) return;

        double mass = ship.getInertiaData().getMass();
        double maxMass = 20000.0;

        if (mass > maxMass && !creative) {
            sendTooHeavyMessage(player);
            return;
        }

        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.setTarget(new Vector3d(target.x, target.y, target.z));
        grabber.setCreative(creative);
    }


    public static void sendTooHeavyMessage(ServerPlayer player) {
        player.displayClientMessage(Component.literal("§cShip is too heavy to lift!"), true);

    }

}