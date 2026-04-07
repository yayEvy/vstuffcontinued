package yay.evy.everest.vstuff.internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RopeRestyleManager {

    private static final List<RopeRestyle> RESTYLES = new ArrayList<>();

    public static void clear() { RESTYLES.clear(); }

    public static void register(RopeRestyle rule) { RESTYLES.add(rule); }

    public static List<RopeRestyle> getAll() { return RESTYLES; }
    
    @Nullable
    public static RopeType retype(RopeType fromType, Item item) {
        ResourceLocation itemLoc = getItemLocation(item);
        for (RopeRestyle rule : RESTYLES) {
            if (rule.matches(fromType, itemLoc)) {
                RopeType result = RopeTypeManager.get(rule.result());
                if (result != null && !result.id().equals(fromType.id()))
                    return result;
            }
        }
        return null;
    }

    public static boolean canRetype(RopeType fromType, Item item) {
        return retype(fromType, item) != null;
    }

    public static boolean isValidRetyping(Item item) {
        ResourceLocation itemLoc = getItemLocation(item);
        return RESTYLES.stream().anyMatch(r -> r.matchesItem(itemLoc));
    }

    public static ResourceLocation getItemLocation(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }


    public record RopeRestyle(List<ResourceLocation> input, @Nullable ResourceLocation fromCategory, @Nullable List<ResourceLocation> fromTypes, ResourceLocation result) {
        public boolean matches(RopeType currentType, ResourceLocation itemLoc) {
            if (!input.contains(itemLoc)) return false;
            if (fromCategory != null && !currentType.category().equals(fromCategory)) return false;
            return fromTypes == null || fromTypes.contains(currentType.id());
        }

        public boolean matchesItem(ResourceLocation itemLoc) {
            return input.contains(itemLoc);
        }
    }

}
