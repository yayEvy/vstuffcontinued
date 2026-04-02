package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static yay.evy.everest.vstuff.infrastructure.data.VStuffDatagen.*;

public class RopeRestylingProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeRestylingProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(restylingSingle(output, "dyes", dyes, zipToMap(dyes.stream().map(VStuff::asResource).toList(), DYE_ITEMS)));
        futures.add(restyling(output, "wools", wools, zipToMap(wools.stream().map(VStuff::asResource).toList(), zipToList(DYE_ITEMS, WOOL_ITEMS))));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static Map.Entry<ResourceLocation, List<Item>> restyleEntry(String loc, Item... items) {
        return Map.entry(VStuff.asResource(loc), Arrays.stream(items).toList());
    }

    public static <K, V> Map<K, V> zipToMap(Collection<K> keys, Collection<V> values) {
        Map<K, V> map = new HashMap<>();
        Iterator<K> keyIter = keys.iterator();
        Iterator<V> valIter = values.iterator();

        while (keyIter.hasNext() && valIter.hasNext()) {
            map.put(keyIter.next(), valIter.next());
        }
        return map;
    }

    public static <T> List<List<T>> zipToList(Collection<T> list1, Collection<T> list2) {
        List<List<T>> list = new ArrayList<>();
        Iterator<T> list1Iter = list1.iterator();
        Iterator<T> list2Iter = list2.iterator();

        while(list1Iter.hasNext() && list2Iter.hasNext()) {
            list.add(List.of(list1Iter.next(), list2Iter.next()));
        }

        return list;
    }

    private CompletableFuture<?> restylingSingle(CachedOutput output, String filename, List<String> forStyles, Map<ResourceLocation, Item> styleToItemMap) {
        return restyling(output, filename, forStyles, zipToMap(styleToItemMap.keySet(), styleToItemMap.values().stream().map(List::of).toList()));
    }

    private CompletableFuture<?> restyling(CachedOutput output, String fileName, List<String> forStyles, Map<ResourceLocation, List<Item>> styleToItemMap) {
        JsonObject json = new JsonObject();

        JsonArray forStylesArray = new JsonArray();

        for (String style : forStyles) forStylesArray.add(VStuff.asResource(style).toString());

        json.add("forStyles", forStylesArray);

        JsonObject restyles = new JsonObject();

        for (Map.Entry<ResourceLocation, List<Item>> styleToItemEntry : styleToItemMap.entrySet()) {
            JsonArray itemArray = new JsonArray();

            for (Item item : styleToItemEntry.getValue()) {
                itemArray.add(getItemLocation(item).toString());
            }

            restyles.add(styleToItemEntry.getKey().toString(), itemArray);
        }

        json.add("restyles", restyles);

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve("ropestyle")
                .resolve("restyle")
                .resolve(fileName + ".json");

        return DataProvider.saveStable(output, json, path);
    }

    private ResourceLocation getItemLocation(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    @Override
    public String getName() {
        return "vstuff_restyle";
    }
}
