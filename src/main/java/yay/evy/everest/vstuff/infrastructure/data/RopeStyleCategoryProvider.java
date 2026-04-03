package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public class RopeStyleCategoryProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeStyleCategoryProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(category(output, 0, "Basic Styles", "vstuff:normal", "vstuff:chain"));

        futures.add(category(output, 1, "Wool Styles", wools));

        futures.add(category(output, 2, "Dyed Styles", dyes));

        futures.add(category(output, 3, "Pride Styles", resourceArray("pride", "gay", "lesbian", "bisexual", "transgender", "nonbinary", "asexual")));

        futures.add(category(output, 4, "Log Styles", logs));

        //futures.add(category(output, 5, "Casing Styles", resourceArray("andesite_casing", "brass_casing", "copper_casing", "train_casing", "industrial_iron")));

        futures.add(category(output, 6, "Merry VStuffmas", "vstuff:christmas_tree", "vstuff:candycane"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> category(CachedOutput output, int order, String name, List<String> styles) {
        return category(output, order, name, resourceArray(styles.toArray(String[]::new)));
    }

    private CompletableFuture<?> category(CachedOutput output, int order, String name, String... styles) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");

        return category(output, VStuff.asResource(fileName), order, name, styles);
    }

    private CompletableFuture<?> category(CachedOutput output, ResourceLocation id, int order, String name, String... styles) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("order", order);

        JsonArray styleArray = new JsonArray();

        for (String style : styles) styleArray.add(style);

        json.add("styles", styleArray);


        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropestyle")
                .resolve("category")
                .resolve(id.getPath() + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "vstuff_category";
    }
}
