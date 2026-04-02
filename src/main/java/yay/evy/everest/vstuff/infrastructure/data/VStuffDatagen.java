package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.VStuff;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(modid = VStuff.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VStuffDatagen {

    static final List<String> COLORS = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
            "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");

    static final List<String> dyes = COLORS.stream().map(color -> color.toLowerCase(Locale.ROOT).replace(" ", "_") + "_dye").toList();
    static final List<String> wools = COLORS.stream().map(color -> color.toLowerCase(Locale.ROOT).replace(" ", "_") + "_wool").toList();

    static final List<String> LOGS = List.of("Oak", "Birch", "Dark Oak", "Jungle", "Acacia", "Mangrove", "Cherry",
            "Stripped Oak", "Stripped Birch", "Stripped Dark Oak", "Stripped Jungle", "Stripped Acacia",
            "Stripped Mangrove", "Stripped Cherry");

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

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();


        generator.addProvider(event.includeServer(), new RopeStyleProvider(generator));
        generator.addProvider(event.includeServer(), new RopeStyleCategoryProvider(generator));
        //generator.addProvider(event.includeServer(), new RopeRestylingProvider(generator)); todo i will finish this tmr

        gatherAllLang(generator);
    }

    private static void gatherAllLang(DataGenerator generator) {
        VStuff.registrate().addDataGenerator(ProviderType.LANG, registrateLangProvider -> {
            BiConsumer<String, String> langConsumer = registrateLangProvider::add;

            provideDefaultLang("default", langConsumer);
            RopeStyleLangProvider.provideLang(generator, langConsumer);
        });
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        String path = "assets/" + VStuff.MOD_ID + "/lang/" + fileName + ".json";
        JsonElement jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException("Could not find default lang file: " + path);
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
