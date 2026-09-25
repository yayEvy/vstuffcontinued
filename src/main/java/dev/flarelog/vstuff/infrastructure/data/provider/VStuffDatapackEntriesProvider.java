package dev.flarelog.vstuff.infrastructure.data.provider;

import dev.flarelog.vstuff.content.ropes.VStuffRopeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.VStuffRopeCategories;
import dev.flarelog.vstuff.content.ropes.VStuffRopeStyles;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.infrastructure.worldgen.VStuffBiomeModifiers;
import dev.flarelog.vstuff.infrastructure.worldgen.VStuffConfiguredFeatures;
import dev.flarelog.vstuff.infrastructure.worldgen.VStuffPlacedFeatures;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class VStuffDatapackEntriesProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, VStuffConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, VStuffPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, VStuffBiomeModifiers::bootstrap)
            .add(VStuffRegistries.ROPE_TYPE, VStuffRopeTypes::bootstrap)
            .add(VStuffRegistries.ROPE_STYLE, VStuffRopeStyles::bootstrap)
            .add(VStuffRegistries.ROPE_CATEGORY, VStuffRopeCategories::bootstrap);

    public VStuffDatapackEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(VStuff.MOD_ID));
    }

    @Override
    public String getName() { return "vstuff_datapack_entries"; }
}