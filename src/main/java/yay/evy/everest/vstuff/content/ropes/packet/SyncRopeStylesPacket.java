package yay.evy.everest.vstuff.content.ropes.packet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

import java.util.Map;

public class SyncRopeStylesPacket extends SimplePacketBase {
    private final Map<ResourceLocation, RopeStyle> styles;
    private static final Gson GSON = new Gson();

    public SyncRopeStylesPacket(){
        styles = RopeStyleManager.STYLES;
    }

    public SyncRopeStylesPacket(FriendlyByteBuf buf) {
        this.styles = buf.readMap(FriendlyByteBuf::readResourceLocation, SyncRopeStylesPacket::readStyle);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(styles, FriendlyByteBuf::writeResourceLocation, SyncRopeStylesPacket::writeStyle);
    }

    private static RopeStyle readStyle(FriendlyByteBuf buf){
        ResourceLocation id = buf.readResourceLocation();
        Component name =  buf.readComponent();
        ResourceLocation category = buf.readResourceLocation();
        ResourceLocation rendererType = buf.readResourceLocation();
        JsonObject params = JsonParser.parseString(buf.readUtf()).getAsJsonObject();
        SoundEvent placeSound = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation());
        SoundEvent breakSound = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation());
        return new RopeStyle(id, name, category, rendererType, params, placeSound, breakSound);
    }

    private static void writeStyle(FriendlyByteBuf buf, RopeStyle style){
        buf.writeResourceLocation(style.id());
        buf.writeComponent(style.name());
        buf.writeResourceLocation(style.category());
        buf.writeResourceLocation(style.rendererTypeId());
        buf.writeUtf(GSON.toJson(style.rendererParams()), 65535);
        buf.writeResourceLocation(style.placeSound().getLocation());
        buf.writeResourceLocation(style.breakSound().getLocation());
    }


    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> RopeStyleManager.STYLES = this.styles);
        return true;
    }
}
