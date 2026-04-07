package yay.evy.everest.vstuff.infrastructure.data.provider;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static yay.evy.everest.vstuff.VStuff.asResource;
import static yay.evy.everest.vstuff.VStuff.mcResource;
import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public class RopeTypeProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeTypeProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(ropeType(output, "Normal", "basic_styles", "none", "normal"));
        futures.add(ropeType(output,"Chain","basic_styles","none","chain"));

        futures.addAll(woolTypes(output));

        futures.addAll(dyeTypes(output));

        futures.addAll(ropeTypes(output, List.of("Pride", "Gay", "Lesbian", "Bisexual", "Transgender", "Nonbinary", "Asexual"), "pride_styles", "none", "normal"));

        futures.addAll(logTypes(output));

        futures.add(ropeType(output, "Candycane", "merry_vstuffmas", "none", "normal"));
        futures.add(ropeType(output, "Christmas Tree", "merry_vstuffmas", "none", "normal"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private List<? extends CompletableFuture<?>> woolTypes(CachedOutput output) {
        return WOOLS.stream().map(wool -> {
            String fileName = wool.toLowerCase(Locale.ROOT).replace(" ", "_");
            return ropeType(output, fileName, wool, "wool_styles", "wools", "normal", textureParams(mcResource("textures/block/" + fileName + ".png")));
        }).toList();
    }

    private List<? extends CompletableFuture<?>> dyeTypes(CachedOutput output) {
        return COLORS.stream().map(color -> {
            String fileName = color.toLowerCase(Locale.ROOT).replace(" ", "_");
            String hex = DYE_COLORS.getOrDefault(fileName, "#FFFFFF");
            return ropeType(output, fileName + "_dye", color, "dyed_styles", "dyes", "solid_colour", colorParams(hex));
        }).toList();
    }

    private List<? extends CompletableFuture<?>> logTypes(CachedOutput output) {
        return LOGS.stream().map(log -> {
            String fileName = log.toLowerCase(Locale.ROOT).replace(" ", "_") + "_log";
            return ropeType(output, fileName, log, "log_styles", "logs", "normal", textureParams(mcResource("textures/block/" + fileName + ".png")));
        }).toList();
    }

    private List<? extends CompletableFuture<?>> ropeTypes(CachedOutput output, List<String> names, String category, String restyleGroup, String renderer) {
        List<CompletableFuture<?>> types = new ArrayList<>();
        names.forEach(name -> types.add(ropeType(output, name, category, restyleGroup, renderer)));
        return types;
    }

    private CompletableFuture<?> ropeType(CachedOutput output, String name, String category, String restyleGroup, String renderer) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeType(output, fileName, name, category, restyleGroup, renderer, textureParams(asResource("textures/rope/rope_" + fileName + ".png")));
    }

    private CompletableFuture<?> ropeType(CachedOutput output, String fileName, String name, String category, String restyleGroup, String renderer, JsonObject rendererParams) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("category", asResource(category).toString());
        json.addProperty("renderer", asResource(renderer).toString());
        json.add("renderer_params", rendererParams);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropetype")
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
        return "vstuff_ropetypes";
    }
}
