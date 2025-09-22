package yay.evy.everest.vstuff.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.client.ClientConstraintTracker;
import yay.evy.everest.vstuff.utils.RopeStyles;

import java.util.function.Supplier;

public class RopeStyleSelectionPacket {

    private final RopeStyles.RopeStyle ropeStyle;
    private final String style;
    private final RopeStyles.PrimitiveRopeStyle basicStyle;
    private final String styleLKey;

    public RopeStyleSelectionPacket(RopeStyles.RopeStyle ropeStyle) {
        this.ropeStyle = ropeStyle;
        this.style = ropeStyle.getStyle();
        this.basicStyle = ropeStyle.getBasicStyle();
        this.styleLKey = ropeStyle.getLangKey();
    }

    public RopeStyleSelectionPacket(FriendlyByteBuf buf) {
        this.style = buf.readUtf();
        this.basicStyle = buf.readEnum(RopeStyles.PrimitiveRopeStyle.class);
        this.styleLKey = buf.readUtf();
        this.ropeStyle = new RopeStyles.RopeStyle(style, basicStyle, styleLKey);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(style);
        buf.writeEnum(basicStyle);
        buf.writeUtf(styleLKey);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.setPacketHandled(true); // idk maybe i'll do something with this later but for now
    }

}
