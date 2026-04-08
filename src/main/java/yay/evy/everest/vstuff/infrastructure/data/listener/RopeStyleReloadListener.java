package yay.evy.everest.vstuff.infrastructure.data.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.RopeRendererTypes;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.parseLoc;

public class RopeStyleReloadListener extends SimpleJsonResourceReloadListener {
    public RopeStyleReloadListener() {
        super(new Gson(), "ropestyle");
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeStyleManager.clearAll(); // clear categories too cuz that runs after this one
        RopeRendererTypes.clearCache();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable("ropestyle." + id.getNamespace() + "." + id.getPath());
            ResourceLocation category = parseLoc(json.get("category"));
            ResourceLocation renderer = parseLoc(json.get("renderer"));
            JsonObject rendererParams = json.getAsJsonObject("renderer_params");

            RopeStyleManager.registerStyle(new RopeStyle(
                    id,
                    name,
                    category,
                    renderer,
                    rendererParams
            ));
        }
        VStuff.LOGGER.info("Loaded {} rope types from data.", RopeStyleManager.styleCount());
    }
}
