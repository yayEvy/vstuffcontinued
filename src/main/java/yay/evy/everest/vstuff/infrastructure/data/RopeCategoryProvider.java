package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RopeCategoryProvider implements DataProvider {
    private final DataGenerator generator;

    public RopeCategoryProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(category(output, "basic_styles","ropecategory.vstuff.basic_styles",0));
        futures.add(category(output, "wool_styles","ropecategory.vstuff.wool_styles",1));
        futures.add(category(output, "dyed_styles","ropecategory.vstuff.dyed_styles",2));
        futures.add(category(output, "pride_styles","ropecategory.vstuff.pride_styles",3));
        futures.add(category(output, "log_styles","ropecategory.vstuff.log_styles",4));
        futures.add(category(output, "merry_vstuffmas","ropecategory.vstuff.merry_vstuffmas",5));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> category(CachedOutput output, String id, String translationKey, int order) {
        JsonObject json = new JsonObject();
        json.addProperty("name", translationKey);
        json.addProperty("order", order);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropecategory")
                .resolve(id + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "vstuff_ropecategories";
    }
}
