package dev.flarelog.vstuff.internal.styling;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.index.VStuffItems;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.styling.data.RopeCategory;
import dev.flarelog.vstuff.internal.styling.data.RopeStyle;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import dev.flarelog.vstuff.internal.utility.EntityUtils;
import dev.flarelog.vstuff.internal.utility.TagUtils;

import java.util.*;

public final class RopeStyleManager {

    // linked hash map of doom and despair
    public static LinkedHashMap<RopeCategory, List<RopeStyle>> getCategoriesWithStyles(RegistryAccess regAccess) {
        Registry<RopeStyle> styleReg = regAccess.registryOrThrow(VStuffRegistries.ROPE_STYLE);
        Registry<RopeCategory> categoryReg = regAccess.registryOrThrow(VStuffRegistries.ROPE_CATEGORY);

        List<RopeCategory> categories = categoryReg.stream().sorted(Comparator.comparingInt(RopeCategory::order)).toList();

        LinkedHashMap<RopeCategory, List<RopeStyle>> categoryWithStylesMap = new LinkedHashMap<>();

        for (RopeCategory category : categories) {
            ResourceLocation categoryKey = categoryReg.getKey(category); // resource location of the category
            List<RopeStyle> stylesForCategory = styleReg
                    .stream()
                    .filter(style -> style.categories()
                            .stream()
                            .anyMatch(h -> h.is(categoryKey))
                    ) // filter styles by if any of their categories match the given resource location
                    .toList();

            categoryWithStylesMap.put(category, stylesForCategory);
        }

        return categoryWithStylesMap;
    }

    public static final ResourceLocation DEFAULT_ID = VStuff.asResource("normal");

    public static final ResourceKey<RopeStyle> DEFAULT_KEY = ResourceKey.create(VStuffRegistries.ROPE_STYLE, DEFAULT_ID);

    public static RopeStyle get(String name) {
        return Optional.ofNullable(VStuff.registrate().get(name, VStuffRegistries.ROPE_STYLE).getUnchecked()).orElseThrow();
    }

    public static RopeStyle resolveStyle(ResourceKey<RopeStyle> styleKey, RegistryAccess regAccess) {
        return regAccess.registryOrThrow(VStuffRegistries.ROPE_STYLE).get(styleKey);
    }

    public static ResourceKey<RopeStyle> get(CompoundTag tag) {
        if (!tag.contains("style", Tag.TAG_COMPOUND)) return DEFAULT_KEY;
        return TagUtils.readResourceKey(tag.getCompound("style"));
    }

    public static void encodeStyle(CompoundTag tag, RopeStyle style) {
        CodecUtil.encodeToTag(tag, "style", RopeStyle.CODEC, style);
    }

    public static RopeStyle decodeStyle(CompoundTag tag) {
        return CodecUtil.decodeFromTag(tag, "style", RopeStyle.CODEC).orElse(get("normal"));
    }

    public static void set(Player player, ResourceKey<RopeStyle> style) {
        InteractionHand hand = EntityUtils.holdingInHand(player, stack -> stack.is(VStuffItems.STYLING_AVAILABLE));
        if (hand == null) return;
        set(player.getItemInHand(hand), style);
    }

    public static void set(ItemStack stack, ResourceKey<RopeStyle> style) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("style", TagUtils.writeResourceKey(style));
    }

    public static final List<String> COLORS = List.of("Red", "Orange", "Yellow", "Lime", "Green", "Cyan",
            "Blue", "Light Blue", "Purple", "Pink", "Magenta", "Brown", "Black", "Gray", "Light Gray", "White");

    public static final List<String> WOOLS = COLORS.stream().map(color -> color + " Wool").toList();

    public static final Map<String, String> DYE_COLORS = Map.ofEntries(
            Map.entry("Red",        "#FF6961"),
            Map.entry("Orange",     "#FF9F33"),
            Map.entry("Yellow",     "#FFFF00"),
            Map.entry("Lime",       "#7FFF00"),
            Map.entry("Green",      "#3D7B3F"),
            Map.entry("Cyan",       "#169C9C"),
            Map.entry("Blue",       "#3C44AA"),
            Map.entry("Light Blue", "#3ABEC7"),
            Map.entry("Purple",     "#8932B8"),
            Map.entry("Pink",       "#F38BAA"),
            Map.entry("Magenta",    "#C74EBD"),
            Map.entry("Brown",      "#835432"),
            Map.entry("Black",      "#1D1D21"),
            Map.entry("Gray",       "#474F52"),
            Map.entry("Light Gray", "#9D9D97"),
            Map.entry("White",      "#F9FFFE")
    );

}