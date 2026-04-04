package yay.evy.everest.vstuff.index;

import com.simibubi.create.infrastructure.worldgen.LayerPattern;
import com.simibubi.create.infrastructure.worldgen.LayeredOreConfiguration;
import com.simibubi.create.infrastructure.worldgen.LayeredOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import yay.evy.everest.vstuff.VStuff;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;

public class VStuffConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> LEVITUFF_ORE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(VStuff.MOD_ID, "levituff_ore"));


    public static final TagKey<Block> TUFF_REPLACEABLES =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "tuff_replaceables"));
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest replaceableTuff = new BlockMatchTest(Blocks.TUFF);

        LayerPattern levituffPattern = LayerPattern.builder()
                .layer(layer -> layer
                        .block(VStuffBlocks.LEVITUFF.get())
                        .weight(3)
                        .size(2, 5)
                )
                .layer(layer -> layer
                        .passiveBlock()
                        .weight(6)
                        .size(1, 3)
                )
                .build();

        context.register(LEVITUFF_ORE, new ConfiguredFeature<>(
                new LayeredOreFeature(),
                new LayeredOreConfiguration(
                        List.of(levituffPattern),
                        12,
                        0.0f
                )
        ));
    }
}