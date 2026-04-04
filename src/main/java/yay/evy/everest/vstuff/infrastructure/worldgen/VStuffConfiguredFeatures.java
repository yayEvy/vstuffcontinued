package yay.evy.everest.vstuff.infrastructure.worldgen;

import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.infrastructure.worldgen.AllFeatures;
import com.simibubi.create.infrastructure.worldgen.LayerPattern;
import com.simibubi.create.infrastructure.worldgen.LayeredOreConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffBlocks;

import java.util.List;

import static net.minecraft.data.worldgen.features.FeatureUtils.register;

public class VStuffConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> LEVITUFF = ResourceKey.create(Registries.CONFIGURED_FEATURE, VStuff.asResource("levituff"));

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        register(context, LEVITUFF, AllFeatures.LAYERED_ORE.get(), new LayeredOreConfiguration(List.of(LEVITUFF_LAYER_PATTERN), 8, 0));
    }

    private static final LayerPattern LEVITUFF_LAYER_PATTERN = LayerPattern.builder()
            .layer(l -> l.weight(1)
                    .passiveBlock())
            .layer(l -> l.weight(1)
                    .block(Blocks.SMOOTH_BASALT))
            .layer(l -> l.weight(2)
                    .block(Blocks.CALCITE))
            .layer(l -> l.weight(1)
                    .block(AllPaletteStoneTypes.ASURINE.getBaseBlock()))
            .layer(l -> l.weight(2)
                    .block(VStuffBlocks.LEVITUFF)
                    .size(1, 3))
            .build();
}