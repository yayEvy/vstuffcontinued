package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuffConfig;
import yay.evy.everest.vstuff.content.physgrabber.PhysGrabberServerAttachment;

import java.util.function.Supplier;

public class PhysGrabberPackets {

    public static void handleGrabOnServer(ServerLevel level, long shipId, Vector3d initialTarget) {
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) {
            return;
        }

        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.setTarget(initialTarget);
    }


    public static void handleReleaseOnServer(ServerLevel level, long shipId) {
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) {
            return;
        }
        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.release();
    }

    public static void handleUpdateOnServer(ServerLevel level, long shipId, Vector3d target) {
        LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(shipId);
        if (ship == null) {
            return;
        }
        PhysGrabberServerAttachment grabber = PhysGrabberServerAttachment.getOrCreate(ship);
        grabber.setTarget(target);
    }


    public static class GrabPacket {
        private final long shipId;
        private final double x, y, z;
        private final boolean creative; // NEW

        public GrabPacket(long shipId, double x, double y, double z, boolean creative) {
            this.shipId = shipId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.creative = creative;
        }

        public static void encode(GrabPacket msg, FriendlyByteBuf buf) {
            buf.writeLong(msg.shipId);
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
            buf.writeBoolean(msg.creative); // NEW
        }

        public static GrabPacket decode(FriendlyByteBuf buf) {
            return new GrabPacket(
                    buf.readLong(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readBoolean() // NEW
            );
        }

        public static void handle(GrabPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender == null) return;
                ServerLevel level = sender.serverLevel();
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level).getLoadedShips().getById(msg.shipId);
                if (ship != null) {
                    var grabber = PhysGrabberServerAttachment.getOrCreate(ship);
                    grabber.setTarget(new Vector3d(msg.x, msg.y, msg.z));
                    grabber.setCreative(msg.creative); // NEW
                }
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
        private final boolean creative;

        public UpdatePacket(long shipId, double x, double y, double z, boolean creative) {
            this.shipId = shipId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.creative = creative;
        }

        public static void encode(UpdatePacket msg, FriendlyByteBuf buf) {
            buf.writeLong(msg.shipId);
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
            buf.writeBoolean(msg.creative);
        }

        public static UpdatePacket decode(FriendlyByteBuf buf) {
            return new UpdatePacket(
                    buf.readLong(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readBoolean()
            );
        }

        public static void handle(UpdatePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender == null) return;

                ServerLevel level = sender.serverLevel();
                LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(level)
                        .getLoadedShips()
                        .getById(msg.shipId);

                if (ship == null) return;

                double mass = ship.getInertiaData().getMass();
                double maxMass = VStuffConfig.PHYS_GRABBER_MAX_MASS.get();


                if (mass > maxMass && !msg.creative) {
                    sender.displayClientMessage(
                            Component.literal(Component.translatable("vstuff.message.grabber_limit").getString() + " (" + mass + " / " + maxMass + ")"),
                            true
                    );
                    return;
                }

                var grabber = PhysGrabberServerAttachment.getOrCreate(ship);
                grabber.setTarget(new org.joml.Vector3d(msg.x, msg.y, msg.z));
                grabber.setCreative(msg.creative);
            });
            ctx.setPacketHandled(true);
        }
    }

}