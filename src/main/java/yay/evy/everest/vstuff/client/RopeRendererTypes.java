package yay.evy.everest.vstuff.client;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.renderers.ChainRopeRenderer;
import yay.evy.everest.vstuff.client.renderers.SolidColourRopeRenderer;
import yay.evy.everest.vstuff.client.renderers.NormalRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.IRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.RopeRendererType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class RopeRendererTypes {
    private static final Map<ResourceLocation, RopeRendererType> REGISTRY = new HashMap<>();

    private static final Map<ResourceLocation, IRopeRenderer> RENDERER_CACHE = new HashMap<>();

    private RopeRendererTypes() {}

    public static RopeRendererType register(RopeRendererType type) {
        if (REGISTRY.containsKey(type.getId())) {
            VStuff.LOGGER.warn("RopeRendererType '{}' is being registered twice, overwriting.", type.getId());
        }
        REGISTRY.put(type.getId(), type);
        return type;
    }

    public static RopeRendererType get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    /**
     * @param ropeTypeId   the ID of the rope type
     * @param rendererTypeId the ID of the renderer type to instantiate
     * @param params the renderer_params from rope type JSON
     */
    @Nullable
    public static IRopeRenderer getOrCreate(ResourceLocation ropeTypeId, ResourceLocation rendererTypeId, JsonObject params) {

        return RENDERER_CACHE.computeIfAbsent(ropeTypeId, (location) -> {
            RopeRendererType type = REGISTRY.get(rendererTypeId);
            if (type == null) {
                VStuff.LOGGER.warn(
                        "No RopeRendererType registered for id '{}' (used by rope type '{}')." +
                                " Did you forget to register it in client setup?",
                        rendererTypeId, location
                );
                return null;
            }

            return type.create(params);
        });
    }

    public static void clearCache() {
        RENDERER_CACHE.clear();
    }

    /**
     * ze normal tube renderer.
     * Required params: { "texture": "vstuff:textures/rope/rope_normal.png" }
     */
    public static final RopeRendererType NORMAL = register(new RopeRendererType(
            VStuff.asResource("normal"),
            (params) -> {
                ResourceLocation texture = ResourceLocation.tryParse(
                        params.get("texture").getAsString()
                );
                return new NormalRopeRenderer(texture);
            }
    ));

    /**
     * le chain rope renderer
     * Required params: { "texture": "vstuff:textures/rope/rope_chain.png" }
     */
    public static final RopeRendererType CHAIN = register(new RopeRendererType(
            VStuff.asResource("chain"),
            params -> {
                ResourceLocation texture = ResourceLocation.bySeparator(
                        params.get("texture").getAsString(), ':'
                );
                return new ChainRopeRenderer(texture);
            }
    ));

    /**
     * coloured rope renderer
     * Required params: { "color": "#FF6961" }
     * colour is ARGB hex, # is optional, alpha defaults to FF if omitted.
     */
    public static final RopeRendererType SOLID_COLOUR = register(new RopeRendererType(
            VStuff.asResource("solid_colour"),
            params -> {
                int argb = parseColour(params.get("color").getAsString());
                return new SolidColourRopeRenderer(argb);
            },
            (guiGraphics, params) -> {
                int argb = parseColour(params.get("color").getAsString());
                // draw a filled 16x16 swatch — alpha always fully opaque in the GUI
                guiGraphics.fill(0, 0, 16, 16, argb | 0xFF000000);
            }
    ));

    /**
     * Parse a hex colour string.
     * Accepts: "#RRGGBB", "RRGGBB", "#AARRGGBB", "AARRGGBB".
     * If only 6 digits, alpha is assumed to be 0xFF.
     */
    private static int parseColour(String hex) {
        String stripped = hex.startsWith("#") ? hex.substring(1) : hex;
        if (stripped.length() == 6) {
            // No alpha, prepend FF
            stripped = "FF" + stripped;
        }
        return (int) Long.parseLong(stripped, 16);
    }

    public static void init() {}
}