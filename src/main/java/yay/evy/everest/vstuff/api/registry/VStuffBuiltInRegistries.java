package yay.evy.everest.vstuff.api.registry;

import com.mojang.serialization.Lifecycle;
import com.simibubi.create.foundation.mixin.accessor.BuiltInRegistriesAccessor;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeCategory;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle;

public class VStuffBuiltInRegistries {

    public static final Registry<RegistryRopeStyle> STYLES = reg(VStuffRegistries.STYLES);
    public static final Registry<RegistryRopeCategory> CATEGORIES = reg(VStuffRegistries.CATEGORIES);
    public static final Registry<RegistryRopeRendererType> RENDERERS = reg(VStuffRegistries.RENDERERS);

    private static <T> Registry<T> reg(ResourceKey<Registry<T>> key) {
        return register(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> register(ResourceKey<Registry<T>> key, WritableRegistry<T> registry) {
        BuiltInRegistriesAccessor.create$getWRITABLE_REGISTRY().register((ResourceKey<WritableRegistry<?>>) (Object) key, registry, Lifecycle.stable());
        return registry;
    }

}
