package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static yay.evy.everest.vstuff.VStuff.asResource;
import static yay.evy.everest.vstuff.VStuff.mcResource;
import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public class RopeStyleProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeStyleProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(basicRopeStyle(output, "Normal", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Chain", RopeStyleManager.RopeRenderType.CHAIN));

        woolStyles(output, futures);
        dyedStyles(output, futures);

        futures.add(basicRopeStyle(output, "Pride", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Gay", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Lesbian", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Bisexual", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Transgender", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Nonbinary", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Asexual", RopeStyleManager.RopeRenderType.NORMAL));

        logStyles(output, futures);

//        futures.add(basicRopeStyle(output, "Andesite Casing", RopeStyleManager.RopeRenderType.NORMAL, "casings"));
//        futures.add(basicRopeStyle(output, "Copper Casing", RopeStyleManager.RopeRenderType.NORMAL, "casings"));
//        futures.add(basicRopeStyle(output, "Brass Casing", RopeStyleManager.RopeRenderType.NORMAL, "casings"));
//        futures.add(basicRopeStyle(output, "Train Casing", RopeStyleManager.RopeRenderType.NORMAL, "casings"));
//        futures.add(basicRopeStyle(output, "Industrial Iron", RopeStyleManager.RopeRenderType.NORMAL, "casings"));

        futures.add(basicRopeStyle(output, "Candycane", RopeStyleManager.RopeRenderType.NORMAL));
        futures.add(basicRopeStyle(output, "Christmas Tree", RopeStyleManager.RopeRenderType.NORMAL));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> basicRopeStyle(CachedOutput output, String name, RopeStyleManager.RopeRenderType ropeRenderType) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeStyle(output, name, ropeRenderType, asResource("textures/rope/rope_" + fileName + ".png"), "none", fileName);
    }

    private CompletableFuture<?> basicRopeStyle(CachedOutput output, String name, RopeStyleManager.RopeRenderType ropeRenderType, String restyleGroup) {
        String fileName = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        return ropeStyle(output, name, ropeRenderType, asResource("textures/rope/rope_" + fileName + ".png"), restyleGroup, fileName);
    }

    private void dyedStyles(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.addAll(COLORS.stream()
                .map(color -> {
                    String fileName = color.toLowerCase(Locale.ROOT).replace(" ", "_") + "_dye";
                    return ropeStyle(output, color, RopeStyleManager.RopeRenderType.NORMAL, asResource("textures/rope/rope_" + fileName + ".png"), "dyes", fileName);
                })
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private void woolStyles(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.addAll(WOOLS.stream()
                .map(color -> {
                    String fileName = color.toLowerCase(Locale.ROOT).replace(" ", "_");
                    return ropeStyle(output, color, RopeStyleManager.RopeRenderType.NORMAL, mcResource("textures/block/" + fileName + ".png"), "wools", fileName);
                })
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private void logStyles(CachedOutput output, List<CompletableFuture<?>> futures) {
        futures.addAll(LOGS.stream()
                .map(log -> {
                    String fileName = log.toLowerCase(Locale.ROOT).replace(" ", "_") + "_log";
                    return ropeStyle(output, log, RopeStyleManager.RopeRenderType.NORMAL, mcResource("textures/block/" + fileName + ".png"), "logs", fileName);
                })
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }


    private String makeFileName(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "_");
    }

    private CompletableFuture<?> ropeStyle(CachedOutput output, String name, RopeStyleManager.RopeRenderType renderType, ResourceLocation texture, String restyleGroup, String fileName) {

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("render_type", renderType.name());
        json.addProperty("texture", texture.toString());
        json.addProperty("restyle_group", asResource(restyleGroup).toString());

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropestyle")
                .resolve("style")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    @Override
    public String getName() {
        return "vstuff_styles";
    }
}
