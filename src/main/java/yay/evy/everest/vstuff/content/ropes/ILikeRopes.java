package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public interface ILikeRopes {
    default void resetTag(ItemStack stack) {
        ResourceLocation lastStyle = null;
        if (stack.getTag().contains("style")) {
            lastStyle = TagUtils.readResourceLocation(stack.getTagElement("style"));
        }

        stack.setTag(null);

        if (lastStyle != null) {
            addStyleToTag(stack, lastStyle);
        }
        // clears tag then puts the style back if there was one
    }

    default void createRopeDrop(ServerLevel serverLevel, BlockPos pos, ResourceLocation style) {
        Vector3d worldPos = RopeUtils.getWorldPos(serverLevel, pos);

        ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());

        addStyleToTag(ropeStack, style);

        ItemEntity ropeDrop = new ItemEntity(
                serverLevel,
                worldPos.x,
                worldPos.y + 0.5,
                worldPos.z,
                ropeStack
        );

        serverLevel.addFreshEntity(ropeDrop);
    }

    default void createRopeDrop(ServerLevel serverLevel, BlockPos pos) {
        createRopeDrop(serverLevel, pos, null);
    }

    default void addStyleToTag(ItemStack stack, ResourceLocation style) {
        stack.getOrCreateTag().put("style", TagUtils.writeResourceLocation(RopeStyleManager.returnOrFallback(style)));
    }

    default void addStyleToTag(ItemStack stack, RopeStyle style) {
        addStyleToTag(stack, style.id());
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
