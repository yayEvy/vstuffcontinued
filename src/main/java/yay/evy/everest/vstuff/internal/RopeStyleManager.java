package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import yay.evy.everest.vstuff.VStuff;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RopeStyleManager {

    private static final Map<ResourceLocation, RopeStyle> STYLES = new HashMap<>();
    private static final HashMap<UUID, ResourceLocation> selectedStyles = new HashMap<>();

    public static ResourceLocation defaultId = new ResourceLocation(VStuff.MOD_ID, "normal");

    public static void clear() {
        STYLES.clear();
    }

    public static void register(RopeStyle style) {
        STYLES.put(style.id(), style);
    }

    public static RopeStyle get(ResourceLocation id) {
        return STYLES.get(id);
    }

    public static Collection<RopeStyle> getAll() {
        return STYLES.values();
    }

    public static void setStyle(Player player, ResourceLocation style) {
        selectedStyles.put(player.getUUID(), style);
    }

    public static ResourceLocation getStyle(Player player) {
        if (player == null) return defaultId;
        return selectedStyles.getOrDefault(player.getUUID(), defaultId);
    }
}
