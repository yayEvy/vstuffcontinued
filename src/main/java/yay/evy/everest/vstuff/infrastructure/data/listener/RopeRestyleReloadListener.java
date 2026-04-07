package yay.evy.everest.vstuff.infrastructure.data.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.RopeRestyleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.parseLoc;

public class RopeRestyleReloadListener extends SimpleJsonResourceReloadListener {

    public RopeRestyleReloadListener() {
        super(new Gson(), "roperestyle");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeRestyleManager.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject json = entry.getValue().getAsJsonObject();

                JsonElement input = json.get("input");

                List<ResourceLocation> inputItems = new ArrayList<>();
                if (input.isJsonArray()) {
                    for (JsonElement el : input.getAsJsonArray()) {
                        inputItems.add(parseLoc(el));
                    }
                } else {
                    inputItems.add(parseLoc(input));
                }

                ResourceLocation fromCategory = json.has("from_category") ? parseLoc(json.get("from_category")) : null;



                List<ResourceLocation> fromTypes = null;
                if (json.has("from_types")) {
                    JsonElement fromTypesElementProMaxUltra = json.get("from_types");
                    fromTypes = new ArrayList<>();
                    if (fromTypesElementProMaxUltra.isJsonArray()) {
                        for (JsonElement el : fromTypesElementProMaxUltra.getAsJsonArray()) {
                            fromTypes.add(parseLoc(el));
                        }
                    } else {
                        fromTypes.add(parseLoc(fromTypesElementProMaxUltra));
                    }
                }

                ResourceLocation result = parseLoc(json.get("result"));

                RopeRestyleManager.register(new RopeRestyleManager.RopeRestyle(inputItems, fromCategory, fromTypes, result));
            } catch (Exception e) {
                VStuff.LOGGER.error("Failed to load restyle '{}': {}", id, e.getMessage());
            }
        }

        VStuff.LOGGER.info("Loaded {} restyles from data.", RopeRestyleManager.getAll().size());

    }
}
