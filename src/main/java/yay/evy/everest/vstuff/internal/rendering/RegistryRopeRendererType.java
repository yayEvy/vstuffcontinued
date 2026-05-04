package yay.evy.everest.vstuff.internal.rendering;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.api.registry.VStuffBuiltInRegistries;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class RegistryRopeRendererType {

    private final Function<JsonObject, IRopeRenderer> factory;
    private final BiConsumer<GuiGraphics, JsonObject> previewRenderer;

    public RegistryRopeRendererType(Function<JsonObject, IRopeRenderer> factory) {
        this(factory, null);
    }

    public RegistryRopeRendererType(Function<JsonObject, IRopeRenderer> factory, BiConsumer<GuiGraphics, JsonObject> previewRenderer) {
        this.factory = factory;
        this.previewRenderer = previewRenderer;
    }

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

    public static final Codec<RegistryRopeRendererType> CODEC = ResourceLocation.CODEC.flatXmap(
            loc -> Optional.ofNullable(VStuffBuiltInRegistries.ROPE_RENDERERS.get(loc))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown rope renderer: " + loc)),
            type -> Optional.ofNullable(VStuffBuiltInRegistries.ROPE_RENDERERS.getKey(type))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unregistered rope renderer"))
    );

    public static class BuiltInRenderers {
        public static final String NORMAL = "normal";
        public static final String CHAIN = "chain";
        public static final String COLOR = "solid_colour";
    }

    @Nullable
    public static RegistryRopeRendererType parse(String str) {
        return VStuffBuiltInRegistries.ROPE_RENDERERS.get(ResourceLocation.tryParse(str));
    }
}