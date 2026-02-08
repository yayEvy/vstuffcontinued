package yay.evy.everest.vstuff.internal.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.internal.RopeStyle;
import yay.evy.everest.vstuff.internal.RopeStyleCategory;
import yay.evy.everest.vstuff.internal.RopeStyleCategoryManager;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RopeStyleCategoryReloadListener extends SimpleJsonResourceReloadListener {

    public RopeStyleCategoryReloadListener() {
        super(new Gson(), "ropestyle_categories");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeStyleCategoryManager.clear();

        RopeStyleCategory uncategorized = new RopeStyleCategory(
                        RopeStyleCategoryManager.UNCATEGORIZED_ID,
                        Component.translatable("ropestyle.category.vstuff.uncategorized"),
                        Integer.MAX_VALUE
                );

        RopeStyleCategoryManager.register(uncategorized);

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable("ropestyle.category." + id.getNamespace() + "." + id.getPath());
            int order = json.has("order") ? json.get("order").getAsInt() : 0;

            RopeStyleCategoryManager.register(new RopeStyleCategory(id, name, order));
        }

        assignStyles(jsons);
    }



    private void assignStyles(Map<ResourceLocation, JsonElement> jsons) {
        Set<RopeStyle> assigned = new HashSet<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation categoryId = entry.getKey();
            RopeStyleCategory category = RopeStyleCategoryManager.getSorted().stream()
                            .filter(c -> c.id.equals(categoryId))
                            .findFirst()
                            .orElse(null);

            if (category == null) {
                continue;
            }

            JsonArray styles = entry.getValue()
                    .getAsJsonObject()
                    .getAsJsonArray("styles");

            for (JsonElement e : styles) {
                ResourceLocation styleId = new ResourceLocation(e.getAsString());
                RopeStyle style = RopeStyleManager.get(styleId);

                if (style != null) {
                    category.styles.add(style);
                    assigned.add(style);
                }
            }
        }

        RopeStyleCategory uncategorized = RopeStyleCategoryManager.getUncategorized();

        for (RopeStyle style : RopeStyleManager.getAll()) {
            if (!assigned.contains(style)) {
                uncategorized.styles.add(style);
            }
        }
    }
}
