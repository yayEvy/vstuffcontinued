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
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.Locale;
import java.util.Map;

public class RopeStyleReloadListener extends SimpleJsonResourceReloadListener {

    public RopeStyleReloadListener() {
        super(new Gson(), "ropestyle/style");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeStyleManager.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable("ropestyle." + id.getNamespace() + "." + id.getPath());

            RopeStyleManager.RopeRenderType renderType = RopeStyleManager.RopeRenderType.valueOf(json.get("render_type").getAsString());

            ResourceLocation texture = ResourceLocation.bySeparator(json.get("texture").getAsString(), ':');

            ResourceLocation restyleGroup = ResourceLocation.bySeparator(json.get("restyle_group").getAsString(), ':');

            RopeStyleManager.register(new RopeStyleManager.RopeStyle(id, restyleGroup, name, renderType, texture));
        }

        VStuff.LOGGER.info("Loaded {} rope styles from data.", RopeStyleManager.getAll().size());
    }
}
