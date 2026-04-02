package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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

    public static RopeStyleManager.RopeStyle restyle(RopeStyleManager.RopeStyle fromStyle, Item item) {
        RopeRestyle group = get(fromStyle.restyleGroup());
        if (group.canRestyle(item))
            return group.getStyleForItem(item);
        return fromStyle;
    }

    public record RopeRestyle(ResourceLocation id, Map<Item, ResourceLocation> itemToStyleIdMap) {
        public boolean canRestyle(Item item) {
            return itemToStyleIdMap.containsKey(item);
        }

        public RopeStyleManager.RopeStyle getStyleForItem(Item item) {
            return RopeStyleManager.get(itemToStyleIdMap.get(item));
        }
    }

}
