package yay.evy.everest.vstuff.infrastructure.data;

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

        futures.add(textured(output,"normal","ropetype.vstuff.normal","basic_styles","none",asResource("textures/rope/rope_normal.png")));
        futures.add(chain(output,"chain","ropetype.vstuff.chain","basic_styles","none",asResource("textures/rope/rope_chain.png")));

        woolTypes(output, futures);

        dyedTypes(output, futures);

        futures.add(textured(output,"pride","ropetype.vstuff.pride","pride_styles", "none", asResource("textures/rope/rope_pride.png")));
        futures.add(textured(output,"gay","ropetype.vstuff.gay","pride_styles", "none", asResource("textures/rope/rope_gay.png")));
        futures.add(textured(output,"lesbian","ropetype.vstuff.lesbian","pride_styles", "none", asResource("textures/rope/rope_lesbian.png")));
        futures.add(textured(output,"bisexual","ropetype.vstuff.bisexual","pride_styles", "none", asResource("textures/rope/rope_bisexual.png")));
        futures.add(textured(output,"transgender","ropetype.vstuff.transgender","pride_styles", "none", asResource("textures/rope/rope_transgender.png")));
        futures.add(textured(output,"nonbinary","ropetype.vstuff.nonbinary","pride_styles", "none", asResource("textures/rope/rope_nonbinary.png")));
        futures.add(textured(output,"asexual","ropetype.vstuff.asexual","pride_styles", "none", asResource("textures/rope/rope_asexual.png")));

        logTypes(output,futures);

        futures.add(textured(output,"candycane","ropetype.vstuff.candycane","merry_vstuffmas","none",asResource("textures/rope/rope_candycane.png")));
        futures.add(textured(output,"christmas_tree","ropetype.vstuff.christmas_tree","merry_vstuffmas","none",asResource("textures/rope/rope_christmas_tree.png")));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> ropeType(CachedOutput output, String fileName, String translationKey, String category, String restyleGroup, String renderer, JsonObject rendererParams) {
        JsonObject json = new JsonObject();
        json.addProperty("name", translationKey);
        json.addProperty("category", asResource(category).toString());
        json.addProperty("restyle_group", asResource(restyleGroup).toString());
        json.addProperty("renderer", asResource(renderer).toString());
        json.add("renderer_params", rendererParams);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropetype")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    private void woolTypes(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.addAll(COLORS.stream().map(color -> {
            String id = color.toLowerCase(Locale.ROOT).replace(" ", "_") + "_wool";
            String translationKey = "ropetype.vstuff." + id;
            ResourceLocation texture = mcResource("textures/block/" + id + ".png");
            return textured(output, id, translationKey, "wool_styles", "wools", texture);
        }).collect(Collectors.toCollection(ArrayList::new)));
    }

    private void dyedTypes(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.addAll(COLORS.stream().map(color -> {
            String colorId = color.toLowerCase(Locale.ROOT).replace(" ", "_");
            String id = colorId + "_dye";
            String translationKey = "ropetype.vstuff." + id;
            String hex = DYE_COLORS.getOrDefault(colorId, "#FFFFFF");
            return solidColour(output, id, translationKey, "dyed_styles", "dyes", hex);
        }).collect(Collectors.toCollection(ArrayList::new)));
    }

    private void logTypes(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.addAll(LOGS.stream().map(log -> {
            String id = log.toLowerCase(Locale.ROOT).replace(" ", "_") + "_log";
            String translationKey = "ropetype.vstuff." + id;
            ResourceLocation texture = mcResource("textures/block/" + id + ".png");
            return textured(output, id, translationKey, "log_styles", "logs", texture);
        }).collect(Collectors.toCollection(ArrayList::new)));
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

    private CompletableFuture<?> textured(CachedOutput output, String fileName, String translationKey, String category, String restyleGroup, ResourceLocation texture) {
        return ropeType(output, fileName, translationKey, category, restyleGroup, "normal", textureParams(texture));
    }

    private CompletableFuture<?> chain(CachedOutput output, String fileName, String translationKey, String category, String restyleGroup, ResourceLocation texture) {
        return ropeType(output, fileName, translationKey, category, restyleGroup, "chain", textureParams(texture));
    }

    private CompletableFuture<?> solidColour(CachedOutput output, String fileName, String translationKey, String category, String restyleGroup, String hex) {
        return ropeType(output, fileName, translationKey, category, restyleGroup, "solid_colour", colorParams(hex));
    }

    @Override
    public String getName() {
        return "vstuff_ropetypes";
    }
}
