package yay.evy.everest.vstuff.infrastructure.data.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.internal.styling.data.RopeCategory;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

public class RopeCategoryReloadListener extends SimpleJsonResourceReloadListener {
    public RopeCategoryReloadListener() {
        super(new Gson(), "ropecategory");
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            Component name = Component.translatable("ropecategory." + id.getNamespace() + "." + id.getPath());
            int order = json.get("order").getAsInt();

            RopeStyleManager.registerCategory(
                    new RopeCategory(
                            id,
                            name,
                            order,
                            List.of()
                    )
            );
        }
    }
}
