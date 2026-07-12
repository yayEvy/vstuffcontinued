package dev.flarelog.vstuff.client;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.client.renderers.ChainRopeRenderer;
import dev.flarelog.vstuff.client.renderers.NormalRopeRenderer;
import dev.flarelog.vstuff.client.renderers.SolidColourRopeRenderer;
import dev.flarelog.vstuff.infrastructure.registry.VStuffBuiltInRegistries;
import dev.flarelog.vstuff.internal.rendering.RopeRendererType;

import java.util.Map;

public class VStuffRopeRendererTypes {

    public static final RopeRendererType NORMAL = register("normal", new RopeRendererType(params -> {
        ResourceLocation texture = ResourceLocation.bySeparator(params.get("texture").getAsString(), ':');
        float uMax = params.has("scale") ? params.get("scale").getAsFloat() : 1.0f;
        return new NormalRopeRenderer(texture, uMax);
    }));

    public static final RopeRendererType CHAIN = register("chain", new RopeRendererType(params -> {
        ResourceLocation texture = ResourceLocation.bySeparator(params.get("texture").getAsString(), ':');
        return new ChainRopeRenderer(texture);
    }));

    public static final RopeRendererType SOLID_COLOR = register("solid_color", new RopeRendererType(params -> {
        int argb = parseColour(params.get("hex").getAsString());
        return new SolidColourRopeRenderer(argb);
    },
    (guiGraphics, params) -> {
        int argb = parseColour(params.get("hex").getAsString());
        guiGraphics.fill(0, 0, 16, 16, argb | 0xFF000000);
    }));

    private static final Map<String, RopeRendererType> RENDERER_TYPE_MAP;

    static {
        Object2ReferenceOpenHashMap<String, RopeRendererType> map = new Object2ReferenceOpenHashMap<>();
        map.put("NORMAL", NORMAL);
        map.put("CHAIN", CHAIN);
        map.put("SOLID_COLOR", SOLID_COLOR);
        map.trim();
        RENDERER_TYPE_MAP = map;
    }

    private static int parseColour(String hex) {
        String stripped = hex.startsWith("#") ? hex.substring(1) : hex;
        if (stripped.length() == 6) {
            // No alpha, prepend FF
            stripped = "FF" + stripped;
        }
        return (int) Long.parseLong(stripped, 16);
    }

    private static <T extends RopeRendererType> T register(String name, T type) {
        return Registry.register(VStuffBuiltInRegistries.ROPE_RENDERER_TYPE, VStuff.asResource(name), type);
    }

    @ApiStatus.Internal
    public static void init() {}

}
