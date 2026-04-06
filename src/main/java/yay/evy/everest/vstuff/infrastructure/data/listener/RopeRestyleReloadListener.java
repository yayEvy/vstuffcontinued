package yay.evy.everest.vstuff.infrastructure.data.listener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.internal.RopeRestyleManager;

import java.util.HashMap;
import java.util.Map;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.parseLoc;

public class RopeRestyleReloadListener extends SimpleJsonResourceReloadListener {

    public RopeRestyleReloadListener() {
        super(new Gson(), "roperestyle");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeRestyleManager.clear();

        RopeRestyleManager.RopeRestyle noneRestyle = new RopeRestyleManager.RopeRestyle(RopeRestyleManager.NONE_ID, new HashMap<>());

        RopeRestyleManager.register(noneRestyle);

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            HashMap<ResourceLocation, ResourceLocation> itemLocToTypeIdMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> restyleEntry : json.entrySet()) {
                JsonArray items = restyleEntry.getValue().getAsJsonArray();

                ResourceLocation styleId = ResourceLocation.bySeparator(restyleEntry.getKey(), ':');

                for (JsonElement item : items) {
                    itemLocToTypeIdMap.put(parseLoc(item), styleId);
                }
            }

            RopeRestyleManager.register(new RopeRestyleManager.RopeRestyle(id, itemLocToTypeIdMap));
        }

        VStuff.LOGGER.info("Loaded {} restyles from data.", RopeRestyleManager.getAll().size());

    }
}
