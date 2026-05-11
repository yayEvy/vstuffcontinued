package yay.evy.everest.vstuff.api.registry;

import com.mojang.serialization.Lifecycle;
import com.simibubi.create.foundation.mixin.accessor.BuiltInRegistriesAccessor;
import com.simibubi.create.impl.registry.MappedRegistryWithFreezeCallback;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import yay.evy.everest.vstuff.client.rope.VStuffRopeRendererTypes;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;

public class VStuffBuiltInRegistries {

    public static final Registry<RegistryRopeRendererType> ROPE_RENDERERS = reg(VStuffRegistries.ROPE_RENDERERS, VStuffRopeRendererTypes::init);

    private static <T> Registry<T> reg(ResourceKey<Registry<T>> key) {
        return register(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
    }

    private static <T> Registry<T> reg(ResourceKey<Registry<T>> key, Runnable freezeCallback) {
        return register(key, new MappedRegistryWithFreezeCallback<>(key, Lifecycle.stable(), freezeCallback));
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> register(ResourceKey<Registry<T>> key, WritableRegistry<T> registry) {
        BuiltInRegistriesAccessor.create$getWRITABLE_REGISTRY().register((ResourceKey<WritableRegistry<?>>) (Object) key, registry, Lifecycle.stable());
        return registry;
    }

    @ApiStatus.Internal
    public static void init() {}

}
