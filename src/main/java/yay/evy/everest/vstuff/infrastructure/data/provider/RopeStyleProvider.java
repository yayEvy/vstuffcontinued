package yay.evy.everest.vstuff.infrastructure.data.provider;

import com.google.gson.JsonObject;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
        futures.add(ropeStyle(output,"Chain","basic_styles","chain", SoundEvents.CHAIN_PLACE, SoundEvents.CHAIN_BREAK));

        futures.addAll(woolStyles(output));

        futures.addAll(dyeStyles(output));

        futures.addAll(ropeStyles(output, List.of("Pride", "Gay", "Lesbian", "Bisexual", "Transgender", "Nonbinary", "Asexual"), "pride_styles", "normal"));

        futures.addAll(ropeStyles(output, List.of("Andesite Casing", "Brass Casing", "Copper Casing", "Train Casing", "Industrial Iron"), "casing_styles", "normal",SoundEvents.WOOD_PLACE, AllSoundEvents.WRENCH_REMOVE.getMainEvent()));

        futures.addAll(logStyles(output));

        futures.add(ropeStyle(output, "Candycane", "merry_vstuffmas", "normal"));
        futures.add(ropeStyle(output, "Christmas Tree", "merry_vstuffmas", "normal"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private List<? extends CompletableFuture<?>> woolStyles(CachedOutput output) {
        return WOOLS.stream().map(wool -> {
            String fileName = wool.toLowerCase(Locale.ROOT).replace(" ", "_");
            return ropeStyle(output, fileName, wool, "wool_styles", "normal", halfSizeTextureParams(mcResource("textures/block/" + fileName + ".png")));
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
            return ropeStyle(output, fileName, log, "log_styles", "normal",
                    halfSizeTextureParams(mcResource("textures/block/" + fileName + ".png")),
                    SoundEvents.WOOD_PLACE, SoundEvents.WOOD_BREAK);
        }).toList();
    }

    private List<? extends CompletableFuture<?>> ropeStyles(CachedOutput output, List<String> names, String category, String renderer) {
        List<CompletableFuture<?>> types = new ArrayList<>();
        names.forEach(name -> types.add(ropeStyle(output, name, category, renderer)));
        return types;
    }

    private List<? extends CompletableFuture<?>> ropeStyles(CachedOutput output, List<String> names, String category, String renderer, SoundEvent placeSound, SoundEvent breakSound) {
        List<CompletableFuture<?>> types = new ArrayList<>();
        names.forEach(name -> types.add(ropeStyle(output, name, category, renderer, placeSound, breakSound)));
        return types;
    }

    private CompletableFuture<?> ropeStyle(CachedOutput output, String name, String category, String renderer) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeStyle(output, fileName, name, category, renderer, textureParams(asResource("textures/rope/rope_" + fileName + ".png")));
    }

    private CompletableFuture<?> ropeStyle(CachedOutput output, String name, String category, String renderer, SoundEvent placeSound, SoundEvent breakSound) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeStyle(output, fileName, name, category, renderer, textureParams(asResource("textures/rope/rope_" + fileName + ".png")), placeSound, breakSound);
    }
    private CompletableFuture<?> ropeStyle(CachedOutput output, String fileName, String name, String category, String renderer, JsonObject rendererParams) {
        return ropeStyle(output, fileName, name, category, renderer, rendererParams, SoundEvents.WOOL_PLACE, SoundEvents.WOOL_BREAK);
    }

    private CompletableFuture<?> ropeStyle(CachedOutput output, String fileName, String name, String category, String renderer, JsonObject rendererParams, SoundEvent placeSound, SoundEvent breakSound) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("category", asResource(category).toString());
        json.addProperty("renderer", asResource(renderer).toString());
        json.add("renderer_params", rendererParams);
        json.addProperty("place_sound", placeSound.getLocation().toString());
        json.addProperty("break_sound", breakSound.getLocation().toString());

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropestyle")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    public static JsonObject textureParams(ResourceLocation texture) {
        return textureParams(texture, 1.0f);
    }

    public static JsonObject textureParams(ResourceLocation texture, float scale) {
        JsonObject p = new JsonObject();
        p.addProperty("texture", texture.toString());
        p.addProperty("scale", scale);
        return p;
    }

    private JsonObject halfSizeTextureParams(ResourceLocation texture) {
        return textureParams(texture, 0.5f);
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
