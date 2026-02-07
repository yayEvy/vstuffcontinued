package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.*;

public class RopeStyleCategoryManager {

    private static final Map<ResourceLocation, RopeStyleCategory> CATEGORIES = new HashMap<>();

    public static final ResourceLocation UNCATEGORIZED_ID = new ResourceLocation(VStuff.MOD_ID, "uncategorized");

    public static void clear() {
        CATEGORIES.clear();
    }

    public static void register(RopeStyleCategory category) {
        CATEGORIES.put(category.id, category);
    }

    public static Collection<RopeStyleCategory> getSorted() {
        return CATEGORIES.values().stream()
                .sorted(Comparator.comparingInt(c -> c.order))
                .toList();
    }

    public static List<RopeStyleCategory> getSortedList() {
        return CATEGORIES.values().stream()
                .sorted(Comparator.comparingInt(c -> c.order))
                .toList();
    }

    public static RopeStyleCategory getUncategorized() {
        return CATEGORIES.get(UNCATEGORIZED_ID);
    }
}
