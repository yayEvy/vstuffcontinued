package yay.evy.everest.vstuff.api.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.styling.data.*;

public class VStuffRegistries {

    public static final ResourceKey<Registry<RegistryRopeStyle>> STYLES = VStuff.registrate().makeRegistry("style", RegistryBuilder::new);
    public static final ResourceKey<Registry<RegistryRopeCategory>> CATEGORIES = VStuff.registrate().makeRegistry("category", RegistryBuilder::new);

    public static final ResourceKey<Registry<RegistryRopeRendererType>> RENDERERS = VStuff.registrate().makeRegistry("renderer", RegistryBuilder::new);

    public static void init() {}
}
