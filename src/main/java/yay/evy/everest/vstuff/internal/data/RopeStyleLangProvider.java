package yay.evy.everest.vstuff.internal.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import yay.evy.everest.vstuff.VStuff;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RopeStyleLangProvider implements DataProvider {

    final DataGenerator generator;

    public RopeStyleLangProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject langJson = new JsonObject();

        lang(langJson, "ropestyles", "ropestyle");
        lang(langJson, "ropestyle_categories", "ropestyle.category");

        Path path = generator.getPackOutput().getOutputFolder()
                .resolve("assets")
                .resolve(VStuff.MOD_ID)
                .resolve("lang")
                .resolve("en_us.json");

        return DataProvider.saveStable(output, langJson, path);
    }


    private void lang(JsonObject langJson, String dir, String keyFirst) {
        Path root = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve(dir);

        Map<String, String> langData = new HashMap<>();

        try {
            Files.walk(root)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> readFile(path, langData, keyFirst));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        langData.forEach(langJson::addProperty);
    }

    @Override
    public String getName() {
        return "vstuff_ropestyle_lang";
    }


    private void readFile(Path path, Map<String, String> langData, String keyFirst) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (!json.has("name")) return;

            String id = path.getFileName().toString().replace(".json", "");

            String key = keyFirst + "." + VStuff.MOD_ID + "." + id;

            String name = json.get("name").getAsString();

            langData.put(key, name);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
    }

}