package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.infrastructure.data.provider.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VStuffDatagen {

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new RopeStyleProvider(generator));
        generator.addProvider(event.includeServer(), new RopeCategoryProvider(generator));
        generator.addProvider(event.includeServer(), new RopeRestylingProvider(generator));
        generator.addProvider(event.includeServer(), new VStuffWeightsProvider(generator));
        generator.addProvider(event.includeServer(), new VStuffWorldGenProvider(output, lookupProvider));
        gatherAllLang(generator);
    }

    private static void gatherAllLang(DataGenerator generator) {
        VStuff.registrate().addDataGenerator(ProviderType.LANG, registrateLangProvider -> {
            BiConsumer<String, String> langConsumer = registrateLangProvider::add;

            provideDefaultLang("default", langConsumer);
            RopeLangProvider.provideLang(generator, langConsumer);
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
