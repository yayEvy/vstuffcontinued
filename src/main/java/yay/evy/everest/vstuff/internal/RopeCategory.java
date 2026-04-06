package yay.evy.everest.vstuff.internal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record RopeCategory(
        ResourceLocation id,
        Component name,
        int order,
        List<RopeType> types   // filled by RopeTypeRegistry.buildSortedCategories()
) {}