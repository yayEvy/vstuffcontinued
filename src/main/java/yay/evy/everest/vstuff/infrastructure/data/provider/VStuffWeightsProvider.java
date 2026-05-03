package yay.evy.everest.vstuff.infrastructure.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffBlocks;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class VStuffWeightsProvider implements DataProvider {

    private final DataGenerator generator;

    public VStuffWeightsProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput output) {
        JsonArray jsonArray = new JsonArray();

        jsonArray.add(block(VStuffBlocks.MECHANICAL_THRUSTER.get(), 2000.0, 0.6));
        jsonArray.add(block(VStuffBlocks.PHYS_PULLEY.get(), 2000.0, 0.6));
        jsonArray.add(block(VStuffBlocks.PULLEY_ANCHOR.get(), 100.0, 0.2));
        jsonArray.add(block(VStuffBlocks.REACTION_WHEEL.get(), 2000.0, 0.6));
        jsonArray.add(block(VStuffBlocks.LEVITUFF.get(), 1600, 0.7));

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("vs_mass")
                .resolve("vstuff.json");

        return DataProvider.saveStable(output, jsonArray, path);
    }

    private JsonObject block(Block block, double mass, double friction) {
        JsonObject json = new JsonObject();
        if (!isRegistered(block)) return json;

        json.addProperty("block", getBlockLocation(block));
        json.addProperty("mass", mass);
        json.addProperty("friction", friction);
        return json;
    }

    private boolean isRegistered(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block) != null;
    }

    private @NotNull String getBlockLocation(@NotNull Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).toString();
    }

    @Override
    public String getName() {
        return "vstuff_blockweights";
    }
}
