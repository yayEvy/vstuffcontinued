package yay.evy.everest.vstuff.infrastructure.data.provider;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class RopeCategoryProvider implements DataProvider {
    private final DataGenerator generator;

    public RopeCategoryProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(category(output, "Basic Styles",0));
        futures.add(category(output, "Wool Styles",1));
        futures.add(category(output, "Dyed Styles",2));
        futures.add(category(output, "Pride Styles",3));
        futures.add(category(output, "Log Styles",4));
        futures.add(category(output, "Merry VStuffmas",5));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> category(CachedOutput output, String name, int order) {
        JsonObject json = new JsonObject();
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        json.addProperty("name", name);
        json.addProperty("order", order);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropecategory")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "vstuff_ropecategories";
    }
}
