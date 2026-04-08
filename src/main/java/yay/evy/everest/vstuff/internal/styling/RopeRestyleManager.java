package yay.evy.everest.vstuff.internal.styling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RopeRestyle;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RopeRestyleManager {

    private static final List<RopeRestyle> RESTYLES = new ArrayList<>();

    public static void clear() { RESTYLES.clear(); }

    public static void register(RopeRestyle rule) { RESTYLES.add(rule); }

    public static List<RopeRestyle> getAll() { return RESTYLES; }
    
    @Nullable
    public static RopeStyle retype(RopeStyle fromType, Item item) {
        ResourceLocation itemLoc = getItemLocation(item);
        for (RopeRestyle rule : RESTYLES) {
            if (rule.matches(fromType, itemLoc)) {
                RopeStyle result = RopeStyleManager.get(rule.result());
                if (result != null && !result.id().equals(fromType.id()))
                    return result;
            }
        }
        return null;
    }

    public static boolean canRetype(RopeStyle fromType, Item item) {
        return retype(fromType, item) != null;
    }

    public static boolean isValidRetyping(Item item) {
        ResourceLocation itemLoc = getItemLocation(item);
        return RESTYLES.stream().anyMatch(r -> r.matchesItem(itemLoc));
    }

    public static ResourceLocation getItemLocation(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

}
