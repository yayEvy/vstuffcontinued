package yay.evy.everest.vstuff.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.impl.game.ships.ShipObject;
import org.valkyrienskies.core.impl.game.ships.ShipObjectWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.ServerShip;
import yay.evy.everest.vstuff.content.physgrabber.PhysGrabberServerAttachment;

import java.util.function.Supplier;

public class PhysGrabberPackets {

    public static void handleGrabOnServer(ServerLevel level, long shipId, Vector3d initialTarget) {
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) {
            System.out.println("[PhysGrabber] handleGrab: ship " + shipId + " not found on server");
            return;
        }

        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.setTarget(initialTarget);
        System.out.println("[PhysGrabber] handleGrab: attached grabber to ship " + shipId + " and activated");
    }


    public static void handleReleaseOnServer(ServerLevel level, long shipId) {
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) {
            System.out.println("[PhysGrabber] handleRelease: ship " + shipId + " not found on server");
            return;
        }
        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.release();
        System.out.println("[PhysGrabber] handleRelease: released attachment on ship " + shipId);
    }

    public static void handleUpdateOnServer(ServerLevel level, long shipId, Vector3d target) {
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) {
            System.out.println("[PhysGrabber] handleUpdate: ship " + shipId + " not found on server");
            return;
        }
        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.setTarget(target);
        System.out.println("[PhysGrabber] handleUpdate: set target " + target + " on ship " + shipId);
    }

    // ----------------------- packets -----------------------------

    public static class GrabPacket {
        private final long shipId;
        private final double x, y, z;

        public GrabPacket(long shipId, double x, double y, double z) {
            this.shipId = shipId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public static void encode(GrabPacket msg, FriendlyByteBuf buf) {
            buf.writeLong(msg.shipId);
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
        }

        public static GrabPacket decode(FriendlyByteBuf buf) {
            return new GrabPacket(buf.readLong(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        public static void handle(GrabPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender == null) return;
                ServerLevel level = sender.serverLevel();
                handleGrabOnServer(level, msg.shipId, new Vector3d(msg.x, msg.y, msg.z));
            });
            ctx.setPacketHandled(true);
        }
    }


    public static class ReleasePacket {
        private final long shipId;
        public ReleasePacket(long shipId) {
            this.shipId = shipId;
        }

        public static void encode(ReleasePacket msg, FriendlyByteBuf buf) {
            buf.writeLong(msg.shipId);
        }

        public static ReleasePacket decode(FriendlyByteBuf buf) {
            return new ReleasePacket(buf.readLong());
        }

        public static void handle(ReleasePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender == null) {
                    System.out.println("[PhysGrabber] ReleasePacket handler: sender is null!");
                    return;
                }
                ServerLevel level = sender.serverLevel();
                handleReleaseOnServer(level, msg.shipId);
            });
            ctx.setPacketHandled(true);
        }
    }

    public static class UpdatePacket {
        private final long shipId;
        private final double x, y, z;

        public UpdatePacket(long shipId, double x, double y, double z) {
            this.shipId = shipId; this.x = x; this.y = y; this.z = z;
        }

        public static void encode(UpdatePacket msg, FriendlyByteBuf buf) {
            buf.writeLong(msg.shipId);
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
        }

        public static UpdatePacket decode(FriendlyByteBuf buf) {
            return new UpdatePacket(buf.readLong(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        public static void handle(UpdatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender == null) {
                    System.out.println("[PhysGrabber] UpdatePacket handler: sender is null!");
                    return;
                }
                ServerLevel level = sender.serverLevel();
                handleUpdateOnServer(level, msg.shipId, new Vector3d(msg.x, msg.y, msg.z));
            });
            ctx.setPacketHandled(true);
        }
    }
}
