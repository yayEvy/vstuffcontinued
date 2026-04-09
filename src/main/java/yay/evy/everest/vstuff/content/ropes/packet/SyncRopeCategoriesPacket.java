package yay.evy.everest.vstuff.content.ropes.packet;

import com.google.gson.Gson;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeCategory;

import java.util.Map;

public class SyncRopeCategoriesPacket extends SimplePacketBase {
    private final Map<ResourceLocation, RopeCategory> categories;
    private static final Gson GSON = new Gson();

    public SyncRopeCategoriesPacket() {
        this.categories = RopeStyleManager.CATEGORIES;
    }

    public SyncRopeCategoriesPacket(FriendlyByteBuf buf) {
        this.categories = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                SyncRopeCategoriesPacket::readCategory
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(categories, FriendlyByteBuf::writeResourceLocation, SyncRopeCategoriesPacket::writeCategory);
    }

    private static RopeCategory readCategory(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component name = buf.readComponent();
        int order = buf.readInt();
        return new RopeCategory(id, name, order, null);
    }

    private static void writeCategory(FriendlyByteBuf buf, RopeCategory category) {
        buf.writeResourceLocation(category.id());
        buf.writeComponent(category.name());
        buf.writeInt(category.order());
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> RopeStyleManager.CATEGORIES = this.categories);
        return true;
    }
}
