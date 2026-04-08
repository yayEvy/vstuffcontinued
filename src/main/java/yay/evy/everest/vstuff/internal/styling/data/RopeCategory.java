package yay.evy.everest.vstuff.internal.styling.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record RopeCategory(
        ResourceLocation id,
        Component name,
        int order,
        List<RopeStyle> types   // filled by RopeTypeRegistry.buildSortedCategories()
) {
    public boolean hasStyles() {
        return !types.isEmpty();
    }
}