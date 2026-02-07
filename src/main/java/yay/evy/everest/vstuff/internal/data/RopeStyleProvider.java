package yay.evy.everest.vstuff.internal.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RopeStyleProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeStyleProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {

        List<CompletableFuture<?>> futures = new ArrayList<>();


        futures.add(basicRopeStyle(output, "Normal", "normal"));
        futures.add(basicRopeStyle(output, "Chain", "chain"));

        woolStyles(output, futures);
        dyedStyles(output, futures);

        futures.add(basicRopeStyle(output, "Pride", "normal"));
        futures.add(basicRopeStyle(output, "Gay", "normal"));
        futures.add(basicRopeStyle(output, "Lesbian", "normal"));
        futures.add(basicRopeStyle(output, "Bisexual", "normal"));
        futures.add(basicRopeStyle(output, "Transgender", "normal"));
        futures.add(basicRopeStyle(output, "Nonbinary", "normal"));
        futures.add(basicRopeStyle(output, "Asexual", "normal"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }



    private void woolStyles(CachedOutput output, List<CompletableFuture<?>> futures) {
        List<String> colors = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
                "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");
        futures.addAll(colors.stream()
                .map(color -> {
                            String fileName = color.toLowerCase(Locale.ROOT).replace(" ", "_") + "_wool";
                            return ropeStyle(output, color, "normal", "minecraft:textures/block/" + fileName, fileName);
                        }
                ).collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private void dyedStyles(CachedOutput output, List<CompletableFuture<?>> futures) {
        List<String> colors = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
                "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");
        futures.addAll(colors.stream()
                .map(color -> {
                    String fileName = color.toLowerCase(Locale.ROOT).replace(" ", "_") + "_dye";
                    return ropeStyle(output, color, "normal", "vstuff:textures/rope/rope_" + fileName, fileName);
                }
                ).collect(Collectors.toCollection(ArrayList::new))
        );

    }

    private CompletableFuture<?> basicRopeStyle(CachedOutput output, String name, String renderType) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeStyle(output, name, renderType, "vstuff:textures/rope/rope_" + fileName, fileName);
    }



    private CompletableFuture<?> ropeStyle(CachedOutput output, String name, String type, String texture, String fileName) {

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("render_type", type);
        json.addProperty("texture", texture);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropestyles")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "vstuff_ropestyles";
    }
}
