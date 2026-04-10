package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public interface ILikeRopes {
    default void resetTag(ItemStack stack) {
        ResourceLocation lastStyle = null;
        if (stack.getTag().contains("style")) {
            lastStyle = TagUtils.readResourceLocation(stack.getTagElement("style"));
        }

        stack.setTag(null);

        if (lastStyle != null) {
            stack.getOrCreateTag().put("style", TagUtils.writeResourceLocation(lastStyle));
        }
        // clears tag then puts the style back if there was one
    }

    default Component getNameWithStyle(Item item, ItemStack stack) {
        return Component.translatable(item.getDescriptionId(stack))
                .append(" (")
                .append(RopeStyle.getOrDefault(stack.getOrCreateTag()).name())
                .append(")");
    }

    default boolean isItemFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("data");
    }
}
