package yay.evy.everest.vstuff.internal.styling.data;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public record RopeRestyle(List<ResourceLocation> input, @Nullable ResourceLocation fromCategory, @Nullable List<ResourceLocation> fromTypes, ResourceLocation result) {
    public boolean matches(RopeStyle currentType, ResourceLocation itemLoc) {
        if (!input.contains(itemLoc)) return false;
        if (fromCategory != null && !currentType.category().equals(fromCategory)) return false;
        return fromTypes == null || fromTypes.contains(currentType.id());
    }

    public boolean matchesItem(ResourceLocation itemLoc) {
        return input.contains(itemLoc);
    }
}