package yay.evy.everest.vstuff.internal.styling;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.styling.data.RopeCategory;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

import java.util.*;

public final class RopeStyleManager {
    private static final Map<ResourceLocation, RopeStyle> STYLES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RopeCategory> CATEGORIES = new LinkedHashMap<>();

    public static final ResourceLocation FALLBACK_ID = VStuff.asResource("normal");

    private RopeStyleManager() {}

    public static void registerStyle(RopeStyle type) {
        STYLES.put(type.id(), type);
    }

    public static void registerCategory(RopeCategory category) {
        CATEGORIES.put(category.id(), category);
    }

    public static RopeStyle get(ResourceLocation id) {
        RopeStyle t = STYLES.get(id);
        if (t == null) {
            VStuff.LOGGER.warn("Unknown rope type '{}', falling back to '{}'", id, FALLBACK_ID);
            return STYLES.get(FALLBACK_ID);
        }
        return t;
    }

    public static Collection<RopeStyle> getAllStyles() { return STYLES.values(); }

    public static Collection<RopeCategory> getAllCategories() { return CATEGORIES.values(); }

    public static int styleCount() { return STYLES.size(); }

    public static void clearAll() {
        STYLES.clear();
        CATEGORIES.clear();
    }

    // group types by le category field.
    public static List<RopeCategory> buildSortedCategories() {
        Map<ResourceLocation, List<RopeStyle>> grouped = new LinkedHashMap<>();
        for (RopeCategory cat : CATEGORIES.values()) {
            grouped.put(cat.id(), new ArrayList<>());
        }
        // Uncategorized bucket always exists
        ResourceLocation uncategorizedId = VStuff.asResource("uncategorized");
        grouped.putIfAbsent(uncategorizedId, new ArrayList<>());

        for (RopeStyle style : STYLES.values()) {
            grouped.computeIfAbsent(style.category(), k -> new ArrayList<>()).add(style);
        }

        List<RopeCategory> result = new ArrayList<>();
        for (RopeCategory cat : CATEGORIES.values()) {
            List<RopeStyle> types = grouped.getOrDefault(cat.id(), List.of());
            if (!types.isEmpty()) {
                result.add(new RopeCategory(cat.id(), cat.name(), cat.order(), types));
                grouped.remove(cat.id());
            }
        }
        // all extras are dumped here
        List<RopeStyle> uncategorized = grouped.entrySet().stream().sorted().map(Map.Entry::getValue).flatMap(Collection::stream).toList();
        result.add(new RopeCategory(uncategorizedId, Component.translatable("ropecategory.vstuff.uncategorized"), Integer.MAX_VALUE, uncategorized));

        return result.stream().filter(RopeCategory::hasStyles).sorted(Comparator.comparingInt(RopeCategory::order)).toList();
    }
}