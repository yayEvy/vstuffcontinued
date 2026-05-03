package yay.evy.everest.vstuff.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public abstract class RopeRestylingProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeRestylingProvider(DataGenerator generator) {
        this.generator = generator;
    }

    protected abstract void restyling(@NotNull DataProviderContext ctx);

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        DataProviderContext ctx = new DataProviderContext(output);

        restyling(ctx);

        return ctx.combineFutures();
    }

    protected void restyle(DataProviderContext ctx, String fromCategory, List<Item> inputs, ResourceLocation result) {
        ctx.addFuture(restyling(ctx.getOutput(), sanitizeFileName(result.toString()), inputs, fromCategory, null, result.toString()));

    }

    protected void restyle(DataProviderContext ctx, List<String> fromTypes, List<Item> inputs, ResourceLocation result) {
        ctx.addFuture(restyling(ctx.getOutput(), sanitizeFileName(result.toString()), inputs, null, fromTypes, result.toString()));
    }

    protected void restyles(DataProviderContext ctx, Map<List<Item>, ResourceLocation> inputsToResultMap, String fromCategory) {
        inputsToResultMap.forEach((inputs, result) -> restyle(ctx, fromCategory, inputs, result));
    }

    protected void restyles(DataProviderContext ctx, Map<List<Item>, ResourceLocation> inputsToResultMap, List<String> fromTypes) {
        inputsToResultMap.forEach((inputs, result) -> restyle(ctx, fromTypes, inputs, result));
    }

    protected void restyles(DataProviderContext ctx, List<List<Item>> inputs, List<ResourceLocation> results, String fromCategory) {
        restyles(ctx, zipToMap(inputs, results), fromCategory);
    }

    protected void restyles(DataProviderContext ctx, List<List<Item>> inputs, List<ResourceLocation> results, List<String> fromTypes) {
        restyles(ctx, zipToMap(inputs, results), fromTypes);
    }

    protected  <K, V> Map<K, V> zipToMap(Collection<K> keys, Collection<V> values) {
        Map<K, V> map = new HashMap<>();
        Iterator<K> keyIter = keys.iterator();
        Iterator<V> valIter = values.iterator();

        while (keyIter.hasNext() && valIter.hasNext()) {
            map.put(keyIter.next(), valIter.next());
        }
        return map;
    }

    protected <T> List<List<T>> zipToList(Collection<T> list1, Collection<T> list2) {
        List<List<T>> list = new ArrayList<>();
        Iterator<T> list1Iter = list1.iterator();
        Iterator<T> list2Iter = list2.iterator();

        while(list1Iter.hasNext() && list2Iter.hasNext()) {
            list.add(List.of(list1Iter.next(), list2Iter.next()));
        }

        return list;
    }

    public static <T> List<List<T>> listOfLists(Collection<T> list) {
        return list.stream().map(List::of).toList();
    }

    private CompletableFuture<?> restyling(CachedOutput output, String fileName, List<Item> inputs, String fromCategory, List<String> fromTypes, String result) {
        JsonObject json = new JsonObject();

        JsonArray inputsArray = new JsonArray();

        for (Item item : inputs) {
            inputsArray.add(getItemLocation(item).toString());
        }

        json.add("input", inputsArray);

        if (fromCategory != null) json.addProperty("from_category", fromCategory);

        if (fromTypes != null) {
            JsonArray typesArray = new JsonArray();
            for (String type : fromTypes)  typesArray.add(type);
            json.add("from_types", typesArray);
        }

        json.addProperty("result", result);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("roperestyle")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    protected static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
    }

    private ResourceLocation getItemLocation(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

}
