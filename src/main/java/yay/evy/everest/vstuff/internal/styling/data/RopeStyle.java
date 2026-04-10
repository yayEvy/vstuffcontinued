package yay.evy.everest.vstuff.internal.styling.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

public record RopeStyle(
        ResourceLocation id,
        Component name,
        ResourceLocation category,
        ResourceLocation rendererTypeId,
        JsonObject rendererParams,      // parsed by the renderer on client
        SoundEvent placeSound,
        SoundEvent breakSound
) {
    public static ResourceLocation getOrDefaultStyleId(CompoundTag ropeTag) {
        if (!ropeTag.contains("style", Tag.TAG_COMPOUND)) return RopeStyleManager.FALLBACK_ID;
        return TagUtils.readResourceLocation(ropeTag.getCompound("style"));
    }

    public static RopeStyle getOrDefault(CompoundTag tag) {
        return RopeStyleManager.get(getOrDefaultStyleId(tag));
    }

    public static RopeStyle get(ResourceLocation styleId) {
        return RopeStyleManager.get(styleId);
    }

    public static void set(Player player, ResourceLocation style) {
        InteractionHand hand = EntityUtils.holdingInHand(player, stack -> stack.is(VStuffItems.STYLING_AVAILABLE));
        if (hand == null) return;
        set(player.getItemInHand(hand), style);
    }

    public static void set(ItemStack stack, ResourceLocation style) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("style", TagUtils.writeResourceLocation(style));
    }
}
