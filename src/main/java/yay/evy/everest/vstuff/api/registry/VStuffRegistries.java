package yay.evy.everest.vstuff.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.styling.data.*;

public class VStuffRegistries {

    public static final ResourceKey<Registry<RegistryRopeStyle>> STYLES = key("style");
    public static final ResourceKey<Registry<RegistryRopeCategory>> CATEGORIES = key("category");

    public static final ResourceKey<Registry<RegistryRopeRendererType>> RENDERERS = key("renderer");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(VStuff.asResource(name));
    }
}
