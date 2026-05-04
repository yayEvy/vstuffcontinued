package yay.evy.everest.vstuff.client;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.api.registry.VStuffBuiltInRegistries;
import yay.evy.everest.vstuff.client.renderers.ChainRopeRenderer;
import yay.evy.everest.vstuff.client.renderers.NormalRopeRenderer;
import yay.evy.everest.vstuff.client.renderers.SolidColourRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;

import java.util.Map;

import static yay.evy.everest.vstuff.internal.rendering.RendererParamHelper.parseColour;


public class VStuffRopeRendererTypes {

    public static final RegistryRopeRendererType NORMAL = register("normal", new RegistryRopeRendererType(params -> {
        ResourceLocation texture = ResourceLocation.bySeparator(params.get("texture").getAsString(), ':');
        float uMax = params.has("scale") ? params.get("scale").getAsFloat() : 1.0f;
        return new NormalRopeRenderer(texture, uMax);
    }));

    public static final RegistryRopeRendererType CHAIN = register("chain", new RegistryRopeRendererType(params -> {
        ResourceLocation texture = ResourceLocation.bySeparator(params.get("texture").getAsString(), ':');
        return new ChainRopeRenderer(texture);
    }));

    public static final RegistryRopeRendererType SOLID_COLOR = register("solid_color", new RegistryRopeRendererType(params -> {
        int argb = parseColour(params.get("color").getAsString());
        return new SolidColourRopeRenderer(argb);
    },
    (guiGraphics, params) -> {
        int argb = parseColour(params.get("color").getAsString());
        // draw a filled 16x16 swatch — alpha always fully opaque in the GUI
        guiGraphics.fill(0, 0, 16, 16, argb | 0xFF000000);
    }));

    private static final Map<String, RegistryRopeRendererType> RENDERER_MAP;

    static {
        Object2ReferenceOpenHashMap<String, RegistryRopeRendererType> map = new Object2ReferenceOpenHashMap<>();
        map.put("NORMAL", NORMAL);
        map.put("CHAIN", CHAIN);
        map.put("SOLID_COLOR", SOLID_COLOR);
        map.trim();
        RENDERER_MAP = map;
    }

    private static <T extends RegistryRopeRendererType> T register(String name, T type) {
        return Registry.register(VStuffBuiltInRegistries.ROPE_RENDERERS, VStuff.asResource(name), type);
    }

    public static void init() {}
}
