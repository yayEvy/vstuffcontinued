package yay.evy.everest.vstuff.internal.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.internal.RopeStyle;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.Map;

public class RopeStyleReloadListener extends SimpleJsonResourceReloadListener {

    public RopeStyleReloadListener() {
        super(new Gson(), "ropestyles");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeStyleManager.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable("ropestyle." + id.getNamespace() + "." + id.getPath());

            RopeStyle.RenderStyle renderStyle = RopeStyle.RenderStyle.valueOf(json.get("render_type").getAsString().toUpperCase());

            ResourceLocation texture = new ResourceLocation(json.get("texture").getAsString());

            RopeStyleManager.register(new RopeStyle(id, name, renderStyle, texture));
        }
    }
}
