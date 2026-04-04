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
import yay.evy.everest.vstuff.client.rope.RopeRendererTypes;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;
import yay.evy.everest.vstuff.content.ropes.type.RopeTypeManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.parseLoc;

public class RopeTypeReloadListener extends SimpleJsonResourceReloadListener {
    public RopeTypeReloadListener() {
        super(new Gson(), "ropetype");
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeTypeManager.clearAll(); // clear categories too cuz that runs after this one
        RopeRendererTypes.clearCache();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable(json.get("name").getAsString());
            ResourceLocation category = parseLoc(json.get("category"));
            ResourceLocation renderer = parseLoc(json.get("renderer"));
            ResourceLocation restyleGroup = parseLoc(json.get("restyle_group"));
            JsonObject rendererParams = json.getAsJsonObject("renderer_params");

            RopeTypeManager.registerType(new RopeType(
                    id,
                    name,
                    category,
                    restyleGroup,
                    renderer,
                    rendererParams
            ));
        }
        VStuff.LOGGER.info("Loaded {} rope types from data.", RopeTypeManager.typeCount());
    }
}
