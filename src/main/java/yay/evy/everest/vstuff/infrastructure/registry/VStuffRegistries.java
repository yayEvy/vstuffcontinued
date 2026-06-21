package yay.evy.everest.vstuff.infrastructure.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.rendering.RopeRendererType;
import yay.evy.everest.vstuff.internal.styling.data.RopeCategory;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

public class VStuffRegistries {

    public static final ResourceKey<Registry<RopeStyle>> ROPE_STYLE = reg("rope/style");
    public static final ResourceKey<Registry<RopeCategory>> ROPE_CATEGORY = reg("rope/category");
    public static final ResourceKey<Registry<RopeRendererType>> ROPE_RENDERER_TYPE = reg("rope/renderer_type");

    private static <T> ResourceKey<Registry<T>> reg(String name) {
        return ResourceKey.createRegistryKey(VStuff.asResource(name));
    }

}
