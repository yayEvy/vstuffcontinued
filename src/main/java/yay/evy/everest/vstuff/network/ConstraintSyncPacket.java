package yay.evy.everest.vstuff.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.ClientConstraintTracker;

import java.util.function.Supplier;

public class ConstraintSyncPacket {

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

    public ConstraintSyncPacket(Integer constraintId, Long shipA, Long shipB,
                                Vector3d localPosA, Vector3d localPosB, double maxLength) {
        this.action = Action.ADD;
        this.constraintId = constraintId;
        this.shipA = shipA;
        this.shipB = shipB;
        this.localPosA = new Vector3d(localPosA);
        this.localPosB = new Vector3d(localPosB);
        this.maxLength = maxLength;
    }

    public ConstraintSyncPacket(Integer constraintId) {
        this.action = Action.REMOVE;
        this.constraintId = constraintId;
        this.shipA = null;
        this.shipB = null;
        this.localPosA = null;
        this.localPosB = null;
        this.maxLength = 0;
    }

    public ConstraintSyncPacket() {
        this.action = Action.CLEAR_ALL;
        this.constraintId = null;
        this.shipA = null;
        this.shipB = null;
        this.localPosA = null;
        this.localPosB = null;
        this.maxLength = 0;
    }

    public ConstraintSyncPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);

        switch (action) {
            case ADD:
                this.constraintId = buf.readInt();
                this.shipA = buf.readLong();
                this.shipB = buf.readLong();
                this.localPosA = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
                this.localPosB = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
                this.maxLength = buf.readDouble();
                break;
            case REMOVE:
                this.constraintId = buf.readInt();
                this.shipA = null;
                this.shipB = null;
                this.localPosA = null;
                this.localPosB = null;
                this.maxLength = 0;
                break;
            case CLEAR_ALL:
            default:
                this.constraintId = null;
                this.shipA = null;
                this.shipB = null;
                this.localPosA = null;
                this.localPosB = null;
                this.maxLength = 0;
                break;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);

        switch (action) {
            case ADD:
                buf.writeInt(constraintId);
                buf.writeLong(shipA);
                buf.writeLong(shipB);
                buf.writeDouble(localPosA.x);
                buf.writeDouble(localPosA.y);
                buf.writeDouble(localPosA.z);
                buf.writeDouble(localPosB.x);
                buf.writeDouble(localPosB.y);
                buf.writeDouble(localPosB.z);
                buf.writeDouble(maxLength);
                break;
            case REMOVE:
                buf.writeInt(constraintId);
                break;
            case CLEAR_ALL:
                // No additional data needed
                break;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            switch (action) {
                case ADD:
                    ClientConstraintTracker.addClientConstraint(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
                    break;
                case REMOVE:
                    ClientConstraintTracker.removeClientConstraint(constraintId);
                    break;
                case CLEAR_ALL:
                    ClientConstraintTracker.clearAllClientConstraints();
                    break;
            }
        });
        context.setPacketHandled(true);
    }
}
