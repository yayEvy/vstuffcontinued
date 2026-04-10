package yay.evy.everest.vstuff.api.data;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.impl.shadow.Co;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public abstract class RopeCategoryProvider implements DataProvider {
    private final DataGenerator generator;

    public RopeCategoryProvider(DataGenerator generator) {
        this.generator = generator;
    }

    public abstract void categories(@NotNull DataProviderContext ctx);

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        DataProviderContext ctx = new DataProviderContext(output);

        categories(ctx);

        return ctx.combineFutures();
    }

    protected void category(DataProviderContext ctx, String name, int order) {
        ctx.addFuture(category(ctx.getOutput(), sanitizeFileName(name), order));
    }

    protected static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
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
}
