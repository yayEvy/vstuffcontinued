package yay.evy.everest.vstuff.util.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.content.constraint.ropes.AbstractRope;
import yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils;

import java.util.function.Supplier;

public class RopePacket {
    public enum Action {
        ADD, REMOVE, CLEAR_ALL, RERENDER
    }

    private final Action action;
    private final Integer constraintId;
    private final AbstractRope rope;

    public RopePacket(Action action, AbstractRope rope) {
        this.action = action;
        this.constraintId = rope.ID;
        this.rope = rope;
    }

    public RopePacket() {
        this(Action.CLEAR_ALL, null);
    }

    public static RopePacket decode(FriendlyByteBuf buf) {
        Action bufAction = buf.readEnum(Action.class);

        if (bufAction == Action.CLEAR_ALL) return new RopePacket();
        return new RopePacket(
                bufAction,
                RopeUtils.fromBuf(buf)
        );
    }

    public static void encode(RopePacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.action);

        if (pkt.action != Action.CLEAR_ALL) pkt.rope.addToBuf(buf);
    }

    public static void handle(RopePacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            try {
                switch (pkt.action) {
                    case ADD, RERENDER:
                        ClientRopeManager.addClientRope(pkt.rope);
                        break;
                    case REMOVE:
                        ClientRopeManager.removeClientRope(pkt.constraintId);
                        break;
                    case CLEAR_ALL:
                        ClientRopeManager.clearAllClientRopes();
                        break;
                }
            } catch (Exception e) {
                VStuff.LOGGER.error("Error handling RopePacket: {}", e.getMessage());
                e.printStackTrace();
            }
        });
        context.setPacketHandled(true);
    }
}