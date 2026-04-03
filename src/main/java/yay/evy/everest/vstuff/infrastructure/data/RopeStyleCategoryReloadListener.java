package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.RopeStyleCategoryManager;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.*;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.parseLoc;

public class RopeStyleCategoryReloadListener extends SimpleJsonResourceReloadListener {

    public RopeStyleCategoryReloadListener() {
        super(new Gson(), "ropestyle/category");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeStyleCategoryManager.clear();

        List<RopeStyleManager.RopeStyle> assigned = new ArrayList<>();
        List<RopeStyleManager.RopeStyle> uncategorizedStyles = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable("ropestyle.category." + id.getNamespace() + "." + id.getPath());
            int order = json.get("order").getAsInt();

            List<RopeStyleManager.RopeStyle> styles = json.getAsJsonArray("styles").asList().stream().map(jsonElement -> RopeStyleManager.get(parseLoc(jsonElement))).toList();

            assigned.addAll(styles);

            RopeStyleCategoryManager.register(new RopeStyleCategoryManager.RopeStyleCategory(id, name, order, styles));
        }

        for (RopeStyleManager.RopeStyle style : RopeStyleManager.getAll()) {
            if (!assigned.contains(style)) {
                uncategorizedStyles.add(style);
            }
        }

        RopeStyleCategoryManager.RopeStyleCategory uncategorized = new RopeStyleCategoryManager.RopeStyleCategory(
            RopeStyleCategoryManager.UNCATEGORIZED_ID,
            Component.translatable("ropestyle.category.vstuff.uncategorized"),
            Integer.MAX_VALUE,
            uncategorizedStyles
        );

        RopeStyleCategoryManager.register(uncategorized);

        VStuff.LOGGER.info("Loaded {} style categories from data.", RopeStyleCategoryManager.getSortedList().size());
    }
}
