package dev.flarelog.vstuff.content.ropes.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import dev.flarelog.vstuff.index.VStuffItems;
import dev.flarelog.vstuff.internal.styling.RopeStyleManager;
import dev.flarelog.vstuff.internal.styling.data.RopeStyle;
import dev.flarelog.vstuff.internal.utility.RopeUtils;
import dev.flarelog.vstuff.internal.utility.TagUtils;

public interface ILikeRopes {
    default void resetTag(ItemStack stack) {
        ResourceKey<RopeStyle> lastStyle = null;
        if (stack.getTag().contains("style")) {
            lastStyle = TagUtils.readResourceKey(stack.getTagElement("style"));
        }

        stack.setTag(null);

        if (lastStyle != null) {
            addStyleToTag(stack, lastStyle);
        }
        // clears tag then puts the style back if there was one
    }

    default void createRopeDrop(ServerLevel serverLevel, BlockPos pos, ResourceKey<RopeStyle> style) {
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

    default void createRopeDrop(Player player, ResourceKey<RopeStyle> style) {
        ItemStack ropeStack = new ItemStack(VStuffItems.ROPE.get());

        addStyleToTag(ropeStack, style);

        player.drop(ropeStack, false);
    }

    default void createRopeDrop(ServerLevel serverLevel, BlockPos pos) {
        createRopeDrop(serverLevel, pos, null);
    }

    default void addStyleToTag(ItemStack stack, ResourceKey<RopeStyle> style) {
        stack.getOrCreateTag().put("style", TagUtils.writeResourceKey(style));
    }

    default Component getNameWithStyle(Item item, ItemStack stack) {
        if (stack.getTagElement("style") == null) addStyleToTag(stack, RopeStyleManager.DEFAULT_KEY);
        ResourceLocation location = TagUtils.readResourceKey(stack.getTagElement("style")).location();
        return Component.translatable(item.getDescriptionId(stack))
                .append(" (")
                .append(Component.translatable("ropestyle." + location.getNamespace() + "." + location.getPath())) // this should always be the correct translation key
                .append(")");
    }

    default boolean isItemFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("data");
    }
}
