package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.VStuff;

import java.util.ArrayList;
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

    public static RopeType retype(RopeType fromType, Item item) {
        RopeRestyle group = get(fromType.restyleGroup());
        if (group.canRestyle(item))
            return group.getTypeForItem(item);
        return fromType;
    }

    public static boolean canRetype(RopeType fromType, Item item) {
        RopeRestyle group = get(fromType.restyleGroup()); // if the group can restyle from that item, and that the new style is a different style
        return group.canRestyle(item) && !(group.getTypeForItem(item).id().equals(fromType.id()));
    }

    public static boolean isValidRetyping(Item item) {
        return validRetypingItems().contains(getItemLocation(item));
    }

    public static List<ResourceLocation> validRetypingItems() {
        List<ResourceLocation> validItems = new ArrayList<>();
        RESTYLES.values().forEach(ropeRestyle -> validItems.addAll(ropeRestyle.itemLocToTypeIdMap.keySet()));
        return validItems;
    }

    public record RopeRestyle(ResourceLocation id, Map<ResourceLocation, ResourceLocation> itemLocToTypeIdMap) {
        public boolean canRestyle(Item item) {
            return canRestyle(getItemLocation(item));
        }

        public boolean canRestyle(ResourceLocation itemLoc) {
            return itemLocToTypeIdMap.containsKey(itemLoc);
        }

        public RopeType getTypeForItem(Item item) {
            return getTypeForItemLoc(getItemLocation(item));
        }

        public RopeType getTypeForItemLoc(ResourceLocation item) {
            return RopeTypeManager.get(itemLocToTypeIdMap.get(item));
        }
    }

    static ResourceLocation getItemLocation(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }


}
