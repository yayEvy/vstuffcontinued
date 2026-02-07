package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RopeStyleManager {

    private static final Map<ResourceLocation, RopeStyle> STYLES = new HashMap<>();

    public static void clear() {
        STYLES.clear();
    }

    public static void register(RopeStyle style) {
        STYLES.put(style.id(), style);
    }

    public static RopeStyle get(ResourceLocation id) {
        return STYLES.getOrDefault(id, STYLES.get(new ResourceLocation(VStuff.MOD_ID, "normal")));
    }

    public static RopeStyle getDefault() {
        return STYLES.get(new ResourceLocation(VStuff.MOD_ID, "normal"));
    }

    public static ResourceLocation defaultId = new ResourceLocation(VStuff.MOD_ID, "normal");

    public static Collection<RopeStyle> getAll() {
        return STYLES.values();
    }
}
