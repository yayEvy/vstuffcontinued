package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public class RopeRestylingProvider implements DataProvider {

    private final DataGenerator generator;

    public RopeRestylingProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(restylingSingle(output, "dyes", zipToMap(dyes.stream().map(VStuff::asResource).toList(), DYE_ITEMS)));
        futures.add(restyling(output, "wools", zipToMap(wools.stream().map(VStuff::asResource).toList(), zipToList(DYE_ITEMS, WOOL_ITEMS))));
        futures.add(restylingSingle(output, "logs", zipToMap(logs.stream().map(VStuff::asResource).toList(), LOG_ITEMS)));
        futures.add(restyling(output, "casings",
            Map.ofEntries(
                restyleEntry("andesite_casing", AllBlocks.ANDESITE_CASING.asItem(), AllItems.ANDESITE_ALLOY.asItem()),
                restyleEntry("copper_casing", AllBlocks.COPPER_CASING.asItem(), Items.COPPER_INGOT),
                restyleEntry("brass_casing", AllBlocks.BRASS_CASING.asItem(), AllItems.BRASS_INGOT.asItem()),
                restyleEntry("train_casing", AllBlocks.RAILWAY_CASING.asItem(), AllItems.STURDY_SHEET.asItem()),
                restyleEntry("industrial_iron", AllBlocks.INDUSTRIAL_IRON_BLOCK.asItem(), Items.IRON_INGOT)
            )
        ));

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

    private CompletableFuture<?> restylingSingle(CachedOutput output, String filename,  Map<ResourceLocation, Item> styleToItemMap) {
        return restyling(output, filename, zipToMap(styleToItemMap.keySet(), styleToItemMap.values().stream().map(List::of).toList()));
    }

    private CompletableFuture<?> restyling(CachedOutput output, String fileName, Map<ResourceLocation, List<Item>> styleToItemMap) {
        JsonObject json = new JsonObject();

        for (Map.Entry<ResourceLocation, List<Item>> styleToItemEntry : styleToItemMap.entrySet()) {
            JsonArray itemArray = new JsonArray();

            for (Item item : styleToItemEntry.getValue()) {
                itemArray.add(getItemLocation(item).toString());
            }

            json.add(styleToItemEntry.getKey().toString(), itemArray);
        }

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

    static final List<Item> DYE_ITEMS = List.of(
            Items.RED_DYE,
            Items.ORANGE_DYE,
            Items.YELLOW_DYE,
            Items.LIME_DYE,
            Items.GREEN_DYE,
            Items.CYAN_DYE,
            Items.BLUE_DYE,
            Items.LIGHT_BLUE_DYE,
            Items.PURPLE_DYE,
            Items.PINK_DYE,
            Items.MAGENTA_DYE,
            Items.BROWN_DYE,
            Items.BLACK_DYE,
            Items.GRAY_DYE,
            Items.LIGHT_GRAY_DYE,
            Items.WHITE_DYE
    );

    static final List<Item> WOOL_ITEMS = List.of(
            Items.RED_WOOL,
            Items.ORANGE_WOOL,
            Items.YELLOW_WOOL,
            Items.LIME_WOOL,
            Items.GREEN_WOOL,
            Items.CYAN_WOOL,
            Items.BLUE_WOOL,
            Items.LIGHT_BLUE_WOOL,
            Items.PURPLE_WOOL,
            Items.PINK_WOOL,
            Items.MAGENTA_WOOL,
            Items.BROWN_WOOL,
            Items.BLACK_WOOL,
            Items.GRAY_WOOL,
            Items.LIGHT_GRAY_WOOL,
            Items.WHITE_WOOL
    );

    static final List<Item> LOG_ITEMS = List.of(
            Items.OAK_LOG,
            Items.BIRCH_LOG,
            Items.SPRUCE_LOG,
            Items.DARK_OAK_LOG,
            Items.JUNGLE_LOG,
            Items.ACACIA_LOG,
            Items.MANGROVE_LOG,
            Items.CHERRY_LOG,
            Items.STRIPPED_OAK_LOG,
            Items.STRIPPED_BIRCH_LOG,
            Items.STRIPPED_SPRUCE_LOG,
            Items.STRIPPED_DARK_OAK_LOG,
            Items.STRIPPED_JUNGLE_LOG,
            Items.STRIPPED_ACACIA_LOG,
            Items.STRIPPED_MANGROVE_LOG,
            Items.STRIPPED_CHERRY_LOG
    );
}
