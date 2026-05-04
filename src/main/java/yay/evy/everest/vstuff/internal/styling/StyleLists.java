package yay.evy.everest.vstuff.internal.styling;

import java.util.List;
import java.util.Map;

public class StyleLists {

    public static final List<String> COLORS = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
            "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");

    public static final List<String> WOOLS = COLORS.stream().map(color -> color + " Wool").toList();

    public static final Map<String, String> DYE_COLORS = Map.ofEntries(
            Map.entry("Red",        "#FF6961"),
            Map.entry("Orange",     "#FF9F33"),
            Map.entry("Yellow",     "#FFFF00"),
            Map.entry("Lime",       "#7FFF00"),
            Map.entry("Green",      "#3D7B3F"),
            Map.entry("Cyan",       "#169C9C"),
            Map.entry("Blue",       "#3C44AA"),
            Map.entry("Light Blue", "#3ABEC7"),
            Map.entry("Purple",     "#8932B8"),
            Map.entry("Pink",       "#F38BAA"),
            Map.entry("Magenta",    "#C74EBD"),
            Map.entry("Brown",      "#835432"),
            Map.entry("Black",      "#1D1D21"),
            Map.entry("Gray",       "#474F52"),
            Map.entry("Light Gray", "#9D9D97"),
            Map.entry("White",      "#F9FFFE")
    );

    public static final List<String> LOGS = List.of("Oak", "Birch", "Spruce", "Dark Oak", "Jungle", "Acacia", "Mangrove", "Cherry",
            "Stripped Oak", "Stripped Birch", "Stripped Spruce", "Stripped Dark Oak", "Stripped Jungle", "Stripped Acacia",
            "Stripped Mangrove", "Stripped Cherry");

    public static final List<String> CASINGS = List.of("Andesite Casing", "Brass Casing", "Copper Casing", "Train Casing", "Industrial Iron");

    public static final List<String> PRIDE = List.of("Pride", "Gay", "Lesbian", "Bisexual", "Transgender", "Nonbinary", "Asexual");
}
