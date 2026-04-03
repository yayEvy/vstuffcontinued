package yay.evy.everest.vstuff.index;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import yay.evy.everest.vstuff.VStuff;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class VStuffWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, VStuffConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, VStuffPlacedFeatures::bootstrap);

    public VStuffWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(VStuff.MOD_ID));
    }

    @Override
    public String getName() { return "VStuff World Gen"; }
}