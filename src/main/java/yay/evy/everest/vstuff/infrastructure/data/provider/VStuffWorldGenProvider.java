package yay.evy.everest.vstuff.infrastructure.data.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.infrastructure.worldgen.VStuffBiomeModifiers;
import yay.evy.everest.vstuff.infrastructure.worldgen.VStuffConfiguredFeatures;
import yay.evy.everest.vstuff.infrastructure.worldgen.VStuffPlacedFeatures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class VStuffWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, VStuffConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, VStuffPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, VStuffBiomeModifiers::bootstrap);

    public VStuffWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(VStuff.MOD_ID));
    }

    @Override
    public String getName() { return "vstuff_worldgen"; }
}