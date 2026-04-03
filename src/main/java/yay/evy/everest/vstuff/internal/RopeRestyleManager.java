package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RopeRestyleManager {

    private static final HashMap<ResourceLocation, RopeRestyle> RESTYLES = new HashMap<>();

    public static final ResourceLocation NONE_ID = VStuff.asResource("none");

    public static void clear() {
        RESTYLES.clear();
    }

    public static void register(RopeRestyle restyle) {
        RESTYLES.put(restyle.id(), restyle);
    }

    public static RopeRestyle get(ResourceLocation id) {
        return RESTYLES.get(id);
    }

    public static RopeRestyle getNone() {
        return RESTYLES.get(NONE_ID);
    }

    public static List<RopeRestyle> getAll() {
        return RESTYLES.values().stream().toList();
    }

    public static RopeStyleManager.RopeStyle restyle(RopeStyleManager.RopeStyle fromStyle, Item item) {
        RopeRestyle group = get(fromStyle.restyleGroup());
        if (group.canRestyle(item))
            return group.getStyleForItem(item);
        return fromStyle;
    }

    public record RopeRestyle(ResourceLocation id, Map<ResourceLocation, ResourceLocation> itemLocToStyleIdMap) {
        public boolean canRestyle(Item item) {
            return canRestyle(getItemLocation(item));
        }

        public boolean canRestyle(ResourceLocation itemLoc) {
            return itemLocToStyleIdMap.containsKey(itemLoc);
        }

        public RopeStyleManager.RopeStyle getStyleForItem(Item item) {
            return getStyleForItemLoc(getItemLocation(item));
        }

        public RopeStyleManager.RopeStyle getStyleForItemLoc(ResourceLocation item) {
            return RopeStyleManager.get(itemLocToStyleIdMap.get(item));
        }
    }

    static ResourceLocation getItemLocation(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }


}
