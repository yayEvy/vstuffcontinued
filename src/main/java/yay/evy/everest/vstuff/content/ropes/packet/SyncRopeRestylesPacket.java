package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.internal.styling.RopeRestyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeRestyle;

import java.util.List;

public class SyncRopeRestylesPacket extends SimplePacketBase {
    private final List<RopeRestyle> restyles;

    public SyncRopeRestylesPacket() {
        this.restyles = RopeRestyleManager.getAll();
    }

    public SyncRopeRestylesPacket(FriendlyByteBuf buf) {
        this.restyles = buf.readList(SyncRopeRestylesPacket::readRestyle);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(restyles, SyncRopeRestylesPacket::writeRestyle);
    }

    private static RopeRestyle readRestyle(FriendlyByteBuf buf) {
        List<ResourceLocation> input = buf.readList(FriendlyByteBuf::readResourceLocation);

        boolean hasFromCategory = buf.readBoolean();

        ResourceLocation fromCategory = hasFromCategory ? buf.readResourceLocation() : null;

        boolean hasFromTypes = buf.readBoolean();

        List<ResourceLocation> fromTypes = hasFromTypes ? buf.readList(FriendlyByteBuf::readResourceLocation) : null;

        ResourceLocation result = buf.readResourceLocation();

        return new RopeRestyle(input, fromCategory, fromTypes, result);
    }

    private static void writeRestyle(FriendlyByteBuf buf, RopeRestyle restyle) {
        buf.writeCollection(restyle.input(), FriendlyByteBuf::writeResourceLocation);

        buf.writeBoolean(restyle.fromCategory() != null);

        if (restyle.fromCategory() != null) {
            buf.writeResourceLocation(restyle.fromCategory());
        }

        buf.writeBoolean(restyle.fromTypes() != null);

        if (restyle.fromTypes() != null) {
            buf.writeCollection(restyle.fromTypes(), FriendlyByteBuf::writeResourceLocation);
        }

        buf.writeResourceLocation(restyle.result());
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> RopeRestyleManager.RESTYLES = this.restyles);
        return true;
    }
}
