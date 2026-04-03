package yay.evy.everest.vstuff.content.ropes.type;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.*;

public final class RopeTypeRegistry {
    private static final Map<ResourceLocation, RopeType> TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RopeCategory> CATEGORIES = new LinkedHashMap<>();

    public static final ResourceLocation FALLBACK_ID = VStuff.asResource("normal");

    private RopeTypeRegistry() {}

    public static void registerType(RopeType type) {
        TYPES.put(type.id(), type);
    }

    public static void registerCategory(RopeCategory category) {
        CATEGORIES.put(category.id(), category);
    }

    public static RopeType get(ResourceLocation id) {
        RopeType t = TYPES.get(id);
        if (t == null) {
            VStuff.LOGGER.warn("Unknown rope type '{}', falling back to '{}'", id, FALLBACK_ID);
            return TYPES.get(FALLBACK_ID);
        }
        return t;
    }

    public static Collection<RopeType> getAllTypes() { return TYPES.values(); }

    public static Collection<RopeCategory> getAllCategories() { return CATEGORIES.values(); }

    public static int typeCount() { return TYPES.size(); }

    public static void clearAll() {
        TYPES.clear();
        CATEGORIES.clear();
    }

    // group types by le category field.
    public static List<RopeCategory> buildSortedCategories() {
        Map<ResourceLocation, List<RopeType>> grouped = new LinkedHashMap<>();
        for (RopeCategory cat : CATEGORIES.values()) {
            grouped.put(cat.id(), new ArrayList<>());
        }
        // Uncategorized bucket always exists
        ResourceLocation uncategorizedId = VStuff.asResource("uncategorized");
        grouped.putIfAbsent(uncategorizedId, new ArrayList<>());

        for (RopeType type : TYPES.values()) {
            grouped.computeIfAbsent(type.category(), k -> new ArrayList<>()).add(type);
        }

        List<RopeCategory> result = new ArrayList<>();
        for (RopeCategory cat : CATEGORIES.values()) {
            List<RopeType> types = grouped.getOrDefault(cat.id(), List.of());
            if (!types.isEmpty())
                result.add(new RopeCategory(cat.id(), cat.name(), cat.order(), types));
        }
        List<RopeType> uncategorized = grouped.getOrDefault(uncategorizedId, List.of());
        if (!uncategorized.isEmpty())
            result.add(new RopeCategory(uncategorizedId,
                    Component.translatable("ropecategory.vstuff.uncategorized"),
                    Integer.MAX_VALUE, uncategorized));
        return result;
    }
}