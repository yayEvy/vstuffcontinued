package yay.evy.everest.vstuff.client.rope;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class RopeRendererType {

    private final ResourceLocation id;
    private final Function<JsonObject, IRopeRenderer> factory;
    private final BiConsumer<GuiGraphics, JsonObject> previewRenderer;

    public RopeRendererType(ResourceLocation id, Function<JsonObject, IRopeRenderer> factory) {
        this(id, factory, null);
    }

    public RopeRendererType(ResourceLocation id, Function<JsonObject, IRopeRenderer> factory, BiConsumer<GuiGraphics, JsonObject> previewRenderer) {
        this.id = id;
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

    public ResourceLocation getId() {
        return id;
    }

}