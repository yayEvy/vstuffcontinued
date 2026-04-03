package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DatagenUtils {

    static final List<String> COLORS = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
            "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");

    static final List<String> LOGS = List.of("Oak", "Birch", "Spruce", "Dark Oak", "Jungle", "Acacia", "Mangrove", "Cherry",
            "Stripped Oak", "Stripped Birch", "Stripped Spruce", "Stripped Dark Oak", "Stripped Jungle", "Stripped Acacia",
            "Stripped Mangrove", "Stripped Cherry");

    static final List<String> WOOLS = COLORS.stream().map(color -> color + " Wool").toList();

    static final List<String> dyes = toIds(COLORS, "_dye");
    static final List<String> wools = toIds(COLORS, "_wool");
    static final List<String> logs = toIds(LOGS, "_log");

    static String[] resourceArray(String... paths) {
        return Arrays.stream(paths).map(path -> VStuff.asResource(path).toString()).toArray(String[]::new);
    }

    static List<String> toIds(List<String> names, String append) {
        return names.stream().map(name -> name.toLowerCase(Locale.ROOT).replace(" ", "_") + append).toList();
    }

    static ResourceLocation parseLoc(JsonElement jsonElement) {
        return ResourceLocation.bySeparator(jsonElement.getAsString(), ':');
    }
}
