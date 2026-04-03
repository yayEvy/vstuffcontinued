package yay.evy.everest.vstuff.index;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yay.evy.everest.vstuff.VStuff;

import java.util.List;

public class VStuffPlacedFeatures {

    public static final ResourceKey<PlacedFeature> LEVITUFF_ORE =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation(VStuff.MOD_ID, "levituff_ore"));

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured =
                context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(LEVITUFF_ORE, new PlacedFeature(
                configured.getOrThrow(VStuffConfiguredFeatures.LEVITUFF_ORE),
                List.of(
                        CountPlacement.of(8),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(-64),
                                VerticalAnchor.absolute(32)
                        ),
                        BiomeFilter.biome()
                )
        ));
    }
}