package yay.evy.everest.vstuff.internal.rendering;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffBuiltInRegistries;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffRegistries;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class RopeRendererType {

    private final Function<JsonObject, IRopeRenderer> factory;
    private final BiConsumer<GuiGraphics, JsonObject> previewRenderer;

    public RopeRendererType(Function<JsonObject, IRopeRenderer> factory) {
        this(factory, null);
    }

    public RopeRendererType(Function<JsonObject, IRopeRenderer> factory, BiConsumer<GuiGraphics, JsonObject> previewRenderer) {
        this.factory = factory;
        this.previewRenderer = previewRenderer;
    }

    public static final Codec<RopeRendererType> LOC_CODEC = ResourceLocation.CODEC.flatXmap(
            loc -> Optional.ofNullable(VStuffBuiltInRegistries.ROPE_RENDERER_TYPE.get(loc))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown rope renderer: " + loc)),
            type -> Optional.ofNullable(VStuffBuiltInRegistries.ROPE_RENDERER_TYPE.getKey(type))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unregistered rope renderer"))
    );

    public static final Codec<Holder<RopeRendererType>> CODEC = RegistryFileCodec.create(VStuffRegistries.ROPE_RENDERER_TYPE, LOC_CODEC);

    public IRopeRenderer create(JsonObject params) {
        return factory.apply(params);
    }

    public void renderPreview(GuiGraphics guiGraphics, JsonObject params) {
        if (previewRenderer != null) {
            previewRenderer.accept(guiGraphics, params);
            return;
        }
        ResourceLocation texture = ResourceLocation.bySeparator(
                params.get("texture").getAsString(), ':');
        guiGraphics.blit(texture, 0, 0, 0, 0, 0, 16, 16, 16, 16);
    }

}