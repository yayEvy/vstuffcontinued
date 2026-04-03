package yay.evy.everest.vstuff.client.rope;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

public final class RopeRendererType {
    private final ResourceLocation id;
    private final Function<JsonObject, IRopeRenderer> factory;

    public RopeRendererType(ResourceLocation id, Function<JsonObject, IRopeRenderer> factory) {
        this.id = id;
        this.factory = factory;
    }

    public IRopeRenderer create(JsonObject params) {
        return factory.apply(params);
    }

    public ResourceLocation getId() { return id; }
}