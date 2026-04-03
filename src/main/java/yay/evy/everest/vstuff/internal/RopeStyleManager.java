package yay.evy.everest.vstuff.internal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RopeStyleManager {

    private static final Map<ResourceLocation, RopeStyle> STYLES = new HashMap<>();
//    private static final HashMap<UUID, ResourceLocation> selectedStyles = new HashMap<>();

    public static final ResourceLocation DEFAULT_ID = VStuff.asResource("normal");

    public static void clear() {
        STYLES.clear();
    }

    public static void register(RopeStyle style) {
        STYLES.put(style.id(), style);
    }

//    public static RopeStyle get(ResourceLocation id) {
//        return STYLES.get(id);
//    }

    public static Collection<RopeStyle> getAll() {
        return STYLES.values();
    }

    public static RopeStyle getDefault() {
        return STYLES.get(DEFAULT_ID);
    }

    public static ResourceLocation getOrDefaultStyleId(CompoundTag ropeTag) {
        if (!ropeTag.contains("style", Tag.TAG_COMPOUND)) return DEFAULT_ID;
        return TagUtils.readResourceLocation(ropeTag.getCompound("style"));
    }

    public static RopeStyle getOrDefaultStyle(ItemStack stack) {
        if (!stack.hasTag()) return getDefault();
        return get(getOrDefaultStyleId(stack.getOrCreateTag()));
    }

    public static ResourceLocation getStyleId(CompoundTag ropeTag) {
        return TagUtils.readResourceLocation(ropeTag.getCompound("style"));
    }

    public static void setStyle(Player player, ResourceLocation style) {
        if (player == null) return;
        InteractionHand hand = EntityUtils.holdingInHand(player, (s) -> VStuffItems.ROPE.isIn(s) || VStuffItems.ROPE_THROWER.isIn(s));
        if (hand == null) return;
        setStyle(player.getItemInHand(hand), style);
    }

    public static void setStyle(ItemStack stack, ResourceLocation style) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("style", TagUtils.writeResourceLocation(style));
    }

    public record RopeStyle(ResourceLocation id, ResourceLocation restyleGroup, Component name, RopeRenderType ropeRenderType, ResourceLocation texture) {}

    public enum RopeRenderType {
        NORMAL,
        NORMAL_TRANSLUCENT,
        CHAIN,
        CHAIN_TRANSLUCENT
    }
}
