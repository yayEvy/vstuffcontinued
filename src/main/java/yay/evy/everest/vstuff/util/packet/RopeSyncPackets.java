package yay.evy.everest.vstuff.util.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import org.joml.Vector3f;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.function.Supplier;

public class RopeSyncPackets {

    public static class Add {

        Integer ropeId;
        Vector3f localPos0;
        Vector3f localPos1;
        Long ship0;
        Long ship1;
        float length;
        String styleId;
        RopeStyles.RopeStyle style;
        public Add(Integer ropeId, Vector3f localPos0, Vector3f localPos1, Long ship0, Long ship1, float length, String styleId) {
            this.ropeId = ropeId;
            this.localPos0 = localPos0;
            this.localPos1 = localPos1;
            this.ship0 = ship0;
            this.ship1 = ship1;
            this.styleId = styleId;
            this.style = RopeStyles.fromString(styleId);
        }

        public static void encode(Add pkt,FriendlyByteBuf buf) {
            buf.writeInt(pkt.ropeId);
            buf.writeVector3f(pkt.localPos0);
            buf.writeVector3f(pkt.localPos1);
            buf.writeLong(pkt.ship0);
            buf.writeLong(pkt.ship1);
            buf.writeFloat(pkt.length);
            buf.writeUtf(pkt.styleId);
        }

        public static Add decode(FriendlyByteBuf buf) {
            return new Add(
                    buf.readInt(),
                    buf.readVector3f(),
                    buf.readVector3f(),
                    buf.readLong(),
                    buf.readLong(),
                    buf.readFloat(),
                    buf.readUtf()
            );
        }

        public static void handle(Add msg, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context ctx = contextSupplier.get();
            ctx.enqueueWork(() -> ClientRopeManager.addClientConstraint(msg.ropeId, msg.ship0, msg.ship1, msg.localPos0, msg.localPos1, msg.length, msg.style));
            ctx.setPacketHandled(true);
        }
    }

    public static class Remove {

        Integer ropeId;
        public Remove(Integer ropeId) {
            this.ropeId = ropeId;
        }

        public static void encode(Remove pkt, FriendlyByteBuf buf) {
            buf.writeInt(pkt.ropeId);
        }

        public static Remove decode(FriendlyByteBuf buf) {
            return new Remove(buf.readInt());
        }

        public static void handle(Remove msg, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context ctx = contextSupplier.get();
            ctx.enqueueWork(() -> ClientRopeManager.removeClientConstraint(msg.ropeId));
            ctx.setPacketHandled(true);
        }

    }

    public static class ClearAll {

        public ClearAll() {

        }

        public static void encode(ClearAll pkt, FriendlyByteBuf buf) {}
        public static ClearAll decode(FriendlyByteBuf buf) {
            return new ClearAll();
        }

        public static void handle(ClearAll msg, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context ctx = contextSupplier.get();
            ctx.enqueueWork(ClientRopeManager::clearAllClientConstraints);
            ctx.setPacketHandled(true);
        }

    }
}