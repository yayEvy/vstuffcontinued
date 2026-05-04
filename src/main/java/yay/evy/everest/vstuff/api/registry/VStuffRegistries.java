package yay.evy.everest.vstuff.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.styling.data.*;

public class VStuffRegistries {

    public static final ResourceKey<Registry<RegistryRopeStyle>> ROPE_STYLES = key("ropestyle");
    public static final ResourceKey<Registry<RegistryRopeCategory>> ROPE_CATEGORIES = key("ropecategory");
    public static final ResourceKey<Registry<RegistryRopeRendererType>> ROPE_RENDERERS = key("roperenderer");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(VStuff.asResource(name));
    }
}
