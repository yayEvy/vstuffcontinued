package yay.evy.everest.vstuff.internal;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public record RopeType(
        ResourceLocation id,
        Component name,
        ResourceLocation category,
        ResourceLocation restyleGroup,
        ResourceLocation rendererTypeId,
        JsonObject rendererParams      // parsed by the renderer on client
) {
    public static ResourceLocation getOrDefaultTypeId(CompoundTag ropeTag) {
        if (!ropeTag.contains("type", Tag.TAG_COMPOUND)) return RopeTypeManager.FALLBACK_ID;
        return TagUtils.readResourceLocation(ropeTag.getCompound("type"));
    }

    public static RopeType getOrDefault(CompoundTag tag) {
        return RopeTypeManager.get(getOrDefaultTypeId(tag));
    }

    public static void set(Player player, ResourceLocation type) {
        InteractionHand hand = EntityUtils.holdingInHand(player, (s) -> VStuffItems.ROPE.isIn(s) || VStuffItems.ROPE_THROWER.isIn(s));
        if (hand == null) return;
        set(player.getItemInHand(hand), type);
    }

    public static void set(ItemStack stack, ResourceLocation type) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("type", TagUtils.writeResourceLocation(type));
    }
}
