package yay.evy.everest.vstuff.infrastructure.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import yay.evy.everest.vstuff.VStuff;

import java.util.List;

import static net.minecraft.data.worldgen.placement.PlacementUtils.register;

public class VStuffPlacedFeatures {

    public static final ResourceKey<PlacedFeature> LEVITUFF = ResourceKey.create(Registries.PLACED_FEATURE, VStuff.asResource("levituff"));

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        Holder<ConfiguredFeature<?, ?>> levituff = configured.getOrThrow(VStuffConfiguredFeatures.LEVITUFF);

        register(context, LEVITUFF, levituff,
            List.of(
                RarityFilter.onAverageOnceEvery(18),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-50), VerticalAnchor.absolute(30)),
                BiomeFilter.biome()
            )
        );
    }
}