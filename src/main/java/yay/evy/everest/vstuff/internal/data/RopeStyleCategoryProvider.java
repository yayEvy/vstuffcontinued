package yay.evy.everest.vstuff.internal.data;

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

public class RopeStyleCategoryProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeStyleCategoryProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(category(output, 0, "Basic Styles", "vstuff:normal", "vstuff:chain"));
        futures.add(category(output, 1, "Wool Styles", colors("wool")));
        futures.add(category(output, 2, "Dyed Styles", colors("dye")));
        futures.add(category(output, 3, "Pride Styles", "vstuff:pride", "vstuff:gay",
                "vstuff:lesbian", "vstuff:bisexual", "vstuff:transgender", "vstuff:nonbinary", "vstuff:asexual"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private String[] colors(String suffix) {
        List<String> colors = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
                "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");
        return colors.stream().map(color -> {
            String path = color.toLowerCase(Locale.ROOT).replace(" ", "_");
            return "vstuff:" + path + "_" + suffix;
        }).toArray(String[]::new);
    }

    private CompletableFuture<?> category(CachedOutput output, int order, String name, String... styles) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");

        return category(output, new ResourceLocation(VStuff.MOD_ID, fileName), order, name, styles);
    }
    private CompletableFuture<?> category(CachedOutput output, ResourceLocation id, int order, String name, String... styles) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("order", order);

        JsonArray styleArray = new JsonArray();
        for (String style : styles) {
            styleArray.add(style);
        }
        json.add("styles", styleArray);


        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(id.getNamespace())
                .resolve("rope_style_categories")
                .resolve(id.getPath() + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "vstuff_ropestyle_categories";
    }
}
