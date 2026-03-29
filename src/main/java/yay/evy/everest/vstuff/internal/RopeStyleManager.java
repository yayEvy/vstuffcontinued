package yay.evy.everest.vstuff.internal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RopeStyleManager {

    private static final Map<ResourceLocation, RopeStyle> STYLES = new HashMap<>();
//    private static final HashMap<UUID, ResourceLocation> selectedStyles = new HashMap<>();

    private static final String NBT_KEY = "rope_style";

    public static ResourceLocation defaultId = new ResourceLocation(VStuff.MOD_ID, "normal");

    public static void clear() {
        STYLES.clear();
    }

    public static void register(RopeStyle style) {
        STYLES.put(style.id(), style);
    }

    public static RopeStyle get(ResourceLocation id) {
        return STYLES.get(id);
    }

    public static Collection<RopeStyle> getAll() {
        return STYLES.values();
    }

    public static void setStyle(Player player, ResourceLocation style) {
        InteractionHand hand = EntityUtils.holdingInHand(player, (s) -> VStuffItems.ROPE.isIn(s) || VStuffItems.ROPE_THROWER.isIn(s));
        if (hand == null) return;
        setStyle(player.getItemInHand(hand), style);
    }

    public static ResourceLocation getStyle(Player player) {
        if (player == null) return defaultId;
        InteractionHand hand = EntityUtils.holdingInHand(player, (s) -> VStuffItems.ROPE.isIn(s) || VStuffItems.ROPE_THROWER.isIn(s));
        if (hand == null) return defaultId;
        return getStyle(player.getItemInHand(hand));
    }

    public static void setStyle(ItemStack stack, ResourceLocation style) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(NBT_KEY, style.toString());
    }

    public static ResourceLocation getStyle(ItemStack stack) {
        if (stack.isEmpty()) return defaultId;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_KEY)) return defaultId;

        String raw = tag.getString(NBT_KEY);
        ResourceLocation parsed = ResourceLocation.tryParse(raw);

        return parsed != null ? parsed : defaultId;
    }
}
