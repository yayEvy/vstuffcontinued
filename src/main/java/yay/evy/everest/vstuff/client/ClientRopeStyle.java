package yay.evy.everest.vstuff.client;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import yay.evy.everest.vstuff.internal.rendering.IRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.RopeRendererType;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.CodecUtil;

public record ClientRopeStyle(Holder<RopeRendererType> renderer, JsonObject rendererParams) {

    public static final Codec<ClientRopeStyle> CODEC = RecordCodecBuilder.create(i -> i.group(
            RopeRendererType.CODEC.fieldOf("renderer").forGetter(ClientRopeStyle::renderer),
            CodecUtil.JSON_OBJECT.fieldOf("rendererParams").forGetter(ClientRopeStyle::rendererParams)
    ).apply(i, ClientRopeStyle::new));

    public IRopeRenderer createRenderer() {
        return renderer.get().create(rendererParams);
    }

    public static ClientRopeStyle fromStyle(RopeStyle style) {
        return new ClientRopeStyle(style.renderer(), style.rendererParams());
    }
}
