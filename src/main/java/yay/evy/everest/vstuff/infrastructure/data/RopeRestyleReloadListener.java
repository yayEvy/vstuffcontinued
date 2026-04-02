package yay.evy.everest.vstuff.infrastructure.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yay.evy.everest.vstuff.internal.RopeRestyleManager;

import java.util.HashMap;
import java.util.Map;

public class RopeRestyleReloadListener extends SimpleJsonResourceReloadListener {

    public RopeRestyleReloadListener() {
        super(new Gson(), "ropestyle/restyle");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
        RopeRestyleManager.clear();

        RopeRestyleManager.RopeRestyle noneRestyle = new RopeRestyleManager.RopeRestyle(RopeRestyleManager.NONE_ID, new HashMap<>());

        RopeRestyleManager.register(noneRestyle);

    }
}
