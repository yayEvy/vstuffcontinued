package dev.flarelog.vstuff.infrastructure.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.internal.rendering.RopeRendererType;
import dev.flarelog.vstuff.content.ropes.style.RopeCategory;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;

public class VStuffRegistries {

    public static final ResourceKey<Registry<RopeStyle>> ROPE_STYLE = reg("rope/style");
    public static final ResourceKey<Registry<RopeCategory>> ROPE_CATEGORY = reg("rope/category");
    public static final ResourceKey<Registry<RopeRendererType>> ROPE_RENDERER_TYPE = reg("rope/renderer_type");

    private static <T> ResourceKey<Registry<T>> reg(String name) {
        return ResourceKey.createRegistryKey(VStuff.asResource(name));
    }

}
