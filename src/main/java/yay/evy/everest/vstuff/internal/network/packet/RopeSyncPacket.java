package yay.evy.everest.vstuff.internal.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.ClientRopeManager;
import yay.evy.everest.vstuff.internal.RopeStyle;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.function.Supplier;

public class RopeSyncPacket {
    public enum Action {
        ADD, REMOVE, CLEAR_ALL
    }

    private final Action action;
    private final Integer constraintId;
    private final Long shipA;
    private final Long shipB;
    private final Vector3d localPosA;
    private final Vector3d localPosB;
    private final double maxLength;
    private final ResourceLocation ropeStyle;

    public RopeSyncPacket(Integer constraintId, Long shipA, Long shipB,
                          Vector3d localPosA, Vector3d localPosB, double maxLength, ResourceLocation ropeStyle) {
        this.action = Action.ADD;
        this.constraintId = constraintId;
        this.shipA = shipA;
        this.shipB = shipB;
        this.localPosA = localPosA != null ? new Vector3d(localPosA) : new Vector3d();
        this.localPosB = localPosB != null ? new Vector3d(localPosB) : new Vector3d();
        this.maxLength = maxLength;

        if (ropeStyle == null) {
            ropeStyle = RopeStyleManager.defaultId;
        }
        this.ropeStyle = ropeStyle;
    }

    public RopeSyncPacket() {
        this.action = Action.CLEAR_ALL;
        this.constraintId = null;
        this.shipA = null;
        this.shipB = null;
        this.localPosA = null;
        this.localPosB = null;
        this.maxLength = 0;
        this.ropeStyle = RopeStyleManager.defaultId;
    }

    public RopeSyncPacket(Integer constraintId) {
        this.action = Action.REMOVE;
        this.constraintId = constraintId;
        this.shipA = null;
        this.shipB = null;
        this.localPosA = null;
        this.localPosB = null;
        this.maxLength = 0;
        this.ropeStyle = RopeStyleManager.defaultId;
    }

    public RopeSyncPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
        switch (action) {
            case ADD:
                this.constraintId = buf.readInt();
                boolean hasShipA = buf.readBoolean();
                this.shipA = hasShipA ? buf.readLong() : null;
                boolean hasShipB = buf.readBoolean();
                this.shipB = hasShipB ? buf.readLong() : null;
                this.localPosA = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
                this.localPosB = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
                this.maxLength = buf.readDouble();


                this.ropeStyle = buf.readResourceLocation();
                break;
            case REMOVE:
                this.constraintId = buf.readInt();
                this.shipA = null;
                this.shipB = null;
                this.localPosA = null;
                this.localPosB = null;
                this.maxLength = 0;
                this.ropeStyle = RopeStyleManager.defaultId;
                break;
            case CLEAR_ALL:
            default:
                this.constraintId = null;
                this.shipA = null;
                this.shipB = null;
                this.localPosA = null;
                this.localPosB = null;
                this.maxLength = 0;
                this.ropeStyle = RopeStyleManager.defaultId;
                break;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        switch (action) {
            case ADD:
                if (constraintId == null) {
                    throw new IllegalStateException("Cannot encode ADD packet with null constraintId");
                }
                buf.writeInt(constraintId);

                buf.writeBoolean(shipA != null);
                if (shipA != null) {
                    buf.writeLong(shipA);
                }
                buf.writeBoolean(shipB != null);
                if (shipB != null) {
                    buf.writeLong(shipB);
                }

                if (localPosA == null || localPosB == null) {
                    throw new IllegalStateException("Cannot encode ADD packet with null positions");
                }
                buf.writeDouble(localPosA.x);
                buf.writeDouble(localPosA.y);
                buf.writeDouble(localPosA.z);
                buf.writeDouble(localPosB.x);
                buf.writeDouble(localPosB.y);
                buf.writeDouble(localPosB.z);
                buf.writeDouble(maxLength);

                buf.writeResourceLocation(ropeStyle);
                break;
            case REMOVE:
                if (constraintId == null) {
                    throw new IllegalStateException("Cannot encode REMOVE packet with null constraintId");
                }
                buf.writeInt(constraintId);
                break;
            case CLEAR_ALL:
                break;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            try {
                switch (action) {
                    case ADD:
                        ClientRopeManager.addClientConstraint(constraintId, shipA, shipB, localPosA, localPosB, maxLength, ropeStyle);
                        break;
                    case REMOVE:
                        ClientRopeManager.removeClientConstraint(constraintId);
                        break;
                    case CLEAR_ALL:
                        ClientRopeManager.clearAllClientConstraints();
                        break;
                }
            } catch (Exception e) {
                VStuff.LOGGER.error("Error handling constraint sync packet: {}", e.getMessage());
                e.printStackTrace();
            }
        });
        context.setPacketHandled(true);
    }
}