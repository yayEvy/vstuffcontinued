package yay.evy.everest.vstuff.infrastructure.data.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.DataGenerator;
import yay.evy.everest.vstuff.VStuff;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class RopeLangProvider {

    public static void provideLang(DataGenerator generator, BiConsumer<String, String> consumer) {
        provideLang(generator, consumer, "ropetype", "ropetype");
        provideLang(generator, consumer, "ropecategory", "ropecategory");
    }

    private static void provideLang(DataGenerator generator, BiConsumer<String, String> consumer, String dir, String keyFirst) {
        Path root = generator.getPackOutput().getOutputFolder()
                .resolve("data")
                .resolve(VStuff.MOD_ID)
                .resolve(dir);

        Map<String, String> langData = new HashMap<>();

        try (Stream<Path> pathStream = Files.walk(root)) {
            pathStream
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> readFile(path, langData, keyFirst));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        langData.forEach(consumer);
    }

    private static void readFile(Path path, Map<String, String> langData, String keyFirst) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            String id = path.getFileName().toString().replace(".json", "");

            String key = keyFirst + "." + VStuff.MOD_ID + "." + id;

            String name = json.get("name").getAsString();

            langData.put(key, name);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
    }

}