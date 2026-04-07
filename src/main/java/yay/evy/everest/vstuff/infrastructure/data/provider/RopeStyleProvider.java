package yay.evy.everest.vstuff.infrastructure.data.provider;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static yay.evy.everest.vstuff.VStuff.asResource;
import static yay.evy.everest.vstuff.VStuff.mcResource;
import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public class RopeStyleProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeStyleProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(ropeStyle(output, "Normal", "basic_styles", "normal"));
        futures.add(ropeStyle(output,"Chain","basic_styles","chain"));

        futures.addAll(woolStyles(output));

        futures.addAll(dyeStyles(output));

        futures.addAll(ropeStyles(output, List.of("Pride", "Gay", "Lesbian", "Bisexual", "Transgender", "Nonbinary", "Asexual"), "pride_styles", "normal"));

        futures.addAll(logStyles(output));

        futures.add(ropeStyle(output, "Candycane", "merry_vstuffmas", "normal"));
        futures.add(ropeStyle(output, "Christmas Tree", "merry_vstuffmas", "normal"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private List<? extends CompletableFuture<?>> woolStyles(CachedOutput output) {
        return WOOLS.stream().map(wool -> {
            String fileName = wool.toLowerCase(Locale.ROOT).replace(" ", "_");
            return ropeStyle(output, fileName, wool, "wool_styles", "normal", textureParams(mcResource("textures/block/" + fileName + ".png")));
        }).toList();
    }

    private List<? extends CompletableFuture<?>> dyeStyles(CachedOutput output) {
        return COLORS.stream().map(color -> {
            String fileName = color.toLowerCase(Locale.ROOT).replace(" ", "_");
            String hex = DYE_COLORS.getOrDefault(fileName, "#FFFFFF");
            return ropeStyle(output, fileName + "_dye", color, "dyed_styles", "solid_colour", colorParams(hex));
        }).toList();
    }

    private List<? extends CompletableFuture<?>> logStyles(CachedOutput output) {
        return LOGS.stream().map(log -> {
            String fileName = log.toLowerCase(Locale.ROOT).replace(" ", "_") + "_log";
            return ropeStyle(output, fileName, log, "log_styles", "normal", textureParams(mcResource("textures/block/" + fileName + ".png")));
        }).toList();
    }

    private List<? extends CompletableFuture<?>> ropeStyles(CachedOutput output, List<String> names, String category, String renderer) {
        List<CompletableFuture<?>> types = new ArrayList<>();
        names.forEach(name -> types.add(ropeStyle(output, name, category, renderer)));
        return types;
    }

    private CompletableFuture<?> ropeStyle(CachedOutput output, String name, String category, String renderer) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeStyle(output, fileName, name, category, renderer, textureParams(asResource("textures/rope/rope_" + fileName + ".png")));
    }

    private CompletableFuture<?> ropeStyle(CachedOutput output, String fileName, String name, String category, String renderer, JsonObject rendererParams) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("category", asResource(category).toString());
        json.addProperty("renderer", asResource(renderer).toString());
        json.add("renderer_params", rendererParams);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropestyle")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    private JsonObject textureParams(ResourceLocation texture) {
        JsonObject p = new JsonObject();
        p.addProperty("texture", texture.toString());
        return p;
    }

    private JsonObject colorParams(String hex) {
        JsonObject p = new JsonObject();
        p.addProperty("color", hex);
        return p;
    }



    @Override
    public String getName() {
        return "vstuff_ropestyles";
    }
}
