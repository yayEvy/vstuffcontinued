package yay.evy.everest.vstuff.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.utils.RopeStyles;

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
    private final RopeStyles.RopeStyle ropeStyle;
    private final String style;
    private final RopeStyles.PrimitiveRopeStyle basicStyle;
    private final String styleLKey;

    public ConstraintSyncPacket(Integer constraintId, Long shipA, Long shipB,
                                Vector3d localPosA, Vector3d localPosB, double maxLength, RopeStyles.RopeStyle ropeStyle) {
        this.action = Action.ADD;
        this.constraintId = constraintId;
        this.shipA = shipA;
        this.shipB = shipB;
        this.localPosA = localPosA != null ? new Vector3d(localPosA) : new Vector3d();
        this.localPosB = localPosB != null ? new Vector3d(localPosB) : new Vector3d();
        this.maxLength = maxLength;
        this.ropeStyle = ropeStyle;
        this.style = ropeStyle.getStyle();
        this.basicStyle = ropeStyle.getBasicStyle();
        this.styleLKey = ropeStyle.getLangKey();
    }

    public ConstraintSyncPacket() {
        this.action = Action.CLEAR_ALL;
        this.constraintId = null;
        this.shipA = null;
        this.shipB = null;
        this.localPosA = null;
        this.localPosB = null;
        this.maxLength = 0;
        this.ropeStyle = new RopeStyles.RopeStyle("none", RopeStyles.PrimitiveRopeStyle.BASIC, "none");
        this.style = ropeStyle.getStyle();
        this.basicStyle = ropeStyle.getBasicStyle();
        this.styleLKey = ropeStyle.getLangKey();
    }

    public ConstraintSyncPacket(Integer constraintId) {
        this.action = Action.REMOVE;
        this.constraintId = constraintId;
        this.shipA = null;
        this.shipB = null;
        this.localPosA = null;
        this.localPosB = null;
        this.maxLength = 0;
        this.ropeStyle = new RopeStyles.RopeStyle("none", RopeStyles.PrimitiveRopeStyle.BASIC, "none");
        this.style = ropeStyle.getStyle();
        this.basicStyle = ropeStyle.getBasicStyle();
        this.styleLKey = ropeStyle.getLangKey();
    }

    public ConstraintSyncPacket(FriendlyByteBuf buf) {
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
                this.style = buf.readUtf();
                this.basicStyle = buf.readEnum(RopeStyles.PrimitiveRopeStyle.class);
                this.styleLKey = buf.readUtf();
                this.ropeStyle = new RopeStyles.RopeStyle(style, basicStyle, styleLKey);
                break;
            case REMOVE:
                this.constraintId = buf.readInt();
                this.shipA = null;
                this.shipB = null;
                this.localPosA = null;
                this.localPosB = null;
                this.maxLength = 0;
                this.style = "none";
                this.basicStyle = RopeStyles.PrimitiveRopeStyle.BASIC;
                this.styleLKey = "none";
                this.ropeStyle = new RopeStyles.RopeStyle(style, basicStyle, styleLKey);
                break;
            case CLEAR_ALL:
            default:
                this.constraintId = null;
                this.shipA = null;
                this.shipB = null;
                this.localPosA = null;
                this.localPosB = null;
                this.maxLength = 0;
                this.style = "none";
                this.basicStyle = RopeStyles.PrimitiveRopeStyle.BASIC;
                this.styleLKey = "none";
                this.ropeStyle = new RopeStyles.RopeStyle(style, basicStyle, styleLKey);
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
                buf.writeUtf(style);
                buf.writeEnum(basicStyle);
                buf.writeUtf(styleLKey);
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
                        ClientConstraintTracker.addClientConstraint(constraintId, shipA, shipB, localPosA, localPosB, maxLength, ropeStyle);
                        break;
                    case REMOVE:
                        ClientConstraintTracker.removeClientConstraint(constraintId);
                        break;
                    case CLEAR_ALL:
                        ClientConstraintTracker.clearAllClientConstraints();
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error handling constraint sync packet: " + e.getMessage());
                e.printStackTrace();
            }
        });
        context.setPacketHandled(true);
    }
}