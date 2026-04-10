package yay.evy.everest.vstuff.api.data;

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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static yay.evy.everest.vstuff.VStuff.asResource;
import static yay.evy.everest.vstuff.VStuff.mcResource;
import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public abstract class RopeStyleProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeStyleProvider(DataGenerator generator) {
        this.generator = generator;
    }

    public abstract void styles(@NotNull DataProviderContext ctx);

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        DataProviderContext ctx = new DataProviderContext(output);

        styles(ctx);

        return ctx.combineFutures();
    }

    protected void ropeStyles(DataProviderContext ctx, List<String> names, String category, String renderer, Function<String, JsonObject> rendererParams) {
        names.forEach(name -> ropeStyle(ctx, name, category, renderer, rendererParams.apply(name)));
    }

    protected void ropeStyles(DataProviderContext ctx, Map<String, JsonObject> nameToRendererParams, String category, String renderer) {
        nameToRendererParams.forEach((name, rendererParams) -> ropeStyle(ctx, name, category, renderer, rendererParams));
    }

    protected void ropeStyles(DataProviderContext ctx, Map<String, String> nameToRendererParamFunctionInput, String category, String renderer, Function<String, JsonObject> rendererParams) {
        nameToRendererParamFunctionInput.forEach((name, rendererParamFunctionInput) -> ropeStyle(ctx, name, category, renderer, rendererParams.apply(rendererParamFunctionInput)));
    }

    protected void ropeStyle(DataProviderContext ctx, String name, String category, String renderer, JsonObject rendererParams) {
        ctx.addFuture(ropeStyle(ctx.getOutput(), sanitizeFileName(name), name, category, renderer, rendererParams));
    }

    protected void ropeStyle(DataProviderContext ctx, String name, String category, String renderer, Function<String, JsonObject> rendererParams) {
        ctx.addFuture(ropeStyle(ctx.getOutput(), sanitizeFileName(name), name, category, renderer, rendererParams.apply(sanitizeFileName(name))));
    }

    protected void ropeStyle(DataProviderContext ctx, String fileName, String name, String category, String renderer, JsonObject rendererParams) {
        ctx.addFuture(ropeStyle(ctx.getOutput(), fileName, name, category, renderer, rendererParams));
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

    private static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
    }

    protected static JsonObject textureParams(ResourceLocation texture) {
        JsonObject p = new JsonObject();
        p.addProperty("texture", texture.toString());
        return p;
    }

    protected static JsonObject block(String name) {
        return textureParams(mcResource("textures/block/" + name + ".png"));
    }

    protected static JsonObject modResource(String name) {
        return textureParams(asResource("textures/rope/rope_" + name + ".png"));
    }

    protected static JsonObject colorParams(String hex) {
        JsonObject p = new JsonObject();
        p.addProperty("color", hex);
        return p;
    }
}
