package yay.evy.everest.vstuff.infrastructure.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;

public class VStuffBiomeModifiers {

    public static final ResourceKey<BiomeModifier> LEVITUFF = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, VStuff.asResource("levituff"));

    public static void bootstrap(BootstapContext<BiomeModifier> ctx) {
        HolderGetter<Biome> lookup = ctx.lookup(Registries.BIOME);
        HolderSet<Biome> isOverworld = lookup.getOrThrow(BiomeTags.IS_OVERWORLD);

        HolderGetter<PlacedFeature> featureLookup = ctx.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> levituff = featureLookup.getOrThrow(VStuffPlacedFeatures.LEVITUFF);

        ctx.register(LEVITUFF, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(isOverworld, HolderSet.direct(levituff), GenerationStep.Decoration.UNDERGROUND_ORES));
    }

}
