package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatagenUtils {

    public static final List<String> COLORS = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
            "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");

    public static final Map<String, String> DYE_COLORS = Map.ofEntries(
            Map.entry("red",        "#FF6961"),
            Map.entry("orange",     "#FF9F33"),
            Map.entry("yellow",     "#FFFF00"),
            Map.entry("lime",       "#7FFF00"),
            Map.entry("green",      "#3D7B3F"),
            Map.entry("cyan",       "#169C9C"),
            Map.entry("blue",       "#3C44AA"),
            Map.entry("light_blue", "#3ABEC7"),
            Map.entry("purple",     "#8932B8"),
            Map.entry("pink",       "#F38BAA"),
            Map.entry("magenta",    "#C74EBD"),
            Map.entry("brown",      "#835432"),
            Map.entry("black",      "#1D1D21"),
            Map.entry("gray",       "#474F52"),
            Map.entry("light_gray", "#9D9D97"),
            Map.entry("white",      "#F9FFFE")
    );

    public static final List<String> LOGS = List.of("Oak", "Birch", "Spruce", "Dark Oak", "Jungle", "Acacia", "Mangrove", "Cherry",
            "Stripped Oak", "Stripped Birch", "Stripped Spruce", "Stripped Dark Oak", "Stripped Jungle", "Stripped Acacia",
            "Stripped Mangrove", "Stripped Cherry");

    public static final List<String> WOOLS = COLORS.stream().map(color -> color + " Wool").toList();

    public static final List<String> dyes = toIds(COLORS, "_dye");
    public static final List<String> wools = toIds(COLORS, "_wool");
    public static final List<String> logs = toIds(LOGS, "_log");

    public static String[] resourceArray(String... paths) {
        return Arrays.stream(paths).map(path -> VStuff.asResource(path).toString()).toArray(String[]::new);
    }

    public static List<String> toIds(List<String> names, String append) {
        return names.stream().map(name -> name.toLowerCase(Locale.ROOT).replace(" ", "_") + append).toList();
    }

    public static ResourceLocation parseLoc(JsonElement jsonElement) {
        return ResourceLocation.bySeparator(jsonElement.getAsString(), ':');
    }
}
