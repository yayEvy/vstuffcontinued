package yay.evy.everest.vstuff.internal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public enum VStuffRopeStyle {

    NORMAL("Normal", "normal", StyleType.NORMAL),
    CHAIN("Chain", "chain", StyleType.CHAIN),

    RED_DYE("Red", "red_dye", StyleType.DYE),
    ORANGE_DYE("Orange", "orange_dye", StyleType.DYE),
    YELLOW_DYE("Yellow", "yellow_dye", StyleType.DYE),
    LIME_DYE("Lime", "lime_dye", StyleType.DYE),
    GREEN_DYE("Green", "green_dye", StyleType.DYE),
    CYAN_DYE("Cyan", "cyan_dye", StyleType.DYE),
    LIGHT_BLUE_DYE("Light Blue", "light_blue_dye", StyleType.DYE),
    BLUE_DYE("Blue", "blue_dye", StyleType.DYE),
    PURPLE_DYE("Purple", "purple_dye", StyleType.DYE),
    MAGENTA_DYE("Magenta", "magenta_dye", StyleType.DYE),
    PINK_DYE("Pink", "pink_dye", StyleType.DYE),
    BROWN_DYE("Brown", "brown_dye", StyleType.DYE),
    BLACK_DYE("Black", "black_dye", StyleType.DYE),
    GRAY_DYE("Gray", "gray_dye", StyleType.DYE),
    LIGHT_GRAY_DYE("Light Gray", "light_gray_dye", StyleType.DYE),
    WHITE_DYE("White", "white_dye", StyleType.DYE),

    RED_WOOL(Component.translatable("block.minecraft.red_wool"), "red_wool", StyleType.WOOL),
    ORANGE_WOOL(Component.translatable("block.minecraft.orange_wool"), "orange_wool", StyleType.WOOL),
    YELLOW_WOOL(Component.translatable("block.minecraft.yellow_wool"), "yellow_wool", StyleType.WOOL),
    LIME_WOOL(Component.translatable("block.minecraft.lime_wool"), "lime_wool", StyleType.WOOL),
    GREEN_WOOL(Component.translatable("block.minecraft.green_wool"), "green_wool", StyleType.WOOL),
    CYAN_WOOL(Component.translatable("block.minecraft.green_wool"), "green_wool", StyleType.WOOL),
    LIGHT_BLUE_WOOL(Component.translatable("block.minecraft.light_blue_wool"), "light_blue_wool", StyleType.WOOL),
    BLUE_WOOL(Component.translatable("block.minecraft.blue_wool"), "blue_wool", StyleType.WOOL),
    PURPLE_WOOL(Component.translatable("block.minecraft.purple_wool"), "purple_wool", StyleType.WOOL),
    MAGENTA_WOOL(Component.translatable("block.minecraft.magenta_wool"), "magenta_wool", StyleType.WOOL),
    PINK_WOOL(Component.translatable("block.minecraft.pink_wool"), "pink_wool", StyleType.WOOL),
    BROWN_WOOL(Component.translatable("block.minecraft.brown_wool"), "brown_wool", StyleType.WOOL),
    BLACK_WOOL(Component.translatable("block.minecraft.black_wool"), "black_wool", StyleType.WOOL),
    GRAY_WOOL(Component.translatable("block.minecraft.gray_wool"), "gray_wool", StyleType.WOOL),
    LIGHT_GRAY_WOOL(Component.translatable("block.minecraft.light_gray_wool"), "light_gray_wool", StyleType.WOOL),
    WHITE_WOOL(Component.translatable("block.minecraft.white_wool"), "white_wool", StyleType.WOOL),

    ANDESITE(Component.translatable("block.create.andesite_casing"), "andesite", StyleType.NORMAL),
    BRASS(Component.translatable("block.create.brass_casing"), "brass", StyleType.NORMAL),
    COPPER(Component.translatable("block.create.copper_casing"), "copper", StyleType.NORMAL),
    RAILWAY(Component.translatable("block.create.railway_casing"), "railway", StyleType.NORMAL),
    INDUSTRIAL("Industrial Iron", "industrial", StyleType.NORMAL),

    GAY("Gay", "gay", StyleType.NORMAL),

    OAK(Component.translatable("block.minecraft.oak_log"), "oak", StyleType.LOG),
    BIRCH(Component.translatable("block.minecraft.birch_log"), "birch", StyleType.LOG),
    SPRUCE(Component.translatable("block.minecraft.spruce_log"), "spruce", StyleType.LOG),
    DARK_OAK(Component.translatable("block.minecraft.dark_oak_log"), "dark_oak", StyleType.LOG),
    JUNGLE(Component.translatable("block.minecraft.jungle_log"), "jungle", StyleType.LOG),
    ACACIA(Component.translatable("block.minecraft.acacia_log"), "acacia", StyleType.LOG),
    CHERRY(Component.translatable("block.minecraft.cherry_log"), "cherry", StyleType.LOG),
    MANGROVE(Component.translatable("block.minecraft.mangrove_log"), "mangrove", StyleType.LOG),

    STRIPPED_OAK(Component.translatable("block.minecraft.stripped_oak_log"), "stripped_oak", StyleType.LOG),
    STRIPPED_BIRCH(Component.translatable("block.minecraft.stripped_birch_log"), "stripped_birch", StyleType.LOG),
    STRIPPED_SPRUCE(Component.translatable("block.minecraft.stripped_spruce_log"), "stripped_spruce", StyleType.LOG),
    STRIPPED_DARK_OAK(Component.translatable("block.minecraft.stripped_dark_oak_log"), "stripped_dark_oak", StyleType.LOG),
    STRIPPED_JUNGLE(Component.translatable("block.minecraft.stripped_jungle_log"), "stripped_jungle", StyleType.LOG),
    STRIPPED_ACACIA(Component.translatable("block.minecraft.stripped_acacia_log"), "stripped_acacia", StyleType.LOG),
    STRIPPED_CHERRY(Component.translatable("block.minecraft.stripped_cherry_log"), "stripped_cherry", StyleType.LOG),
    STRIPPED_MANGROVE(Component.translatable("block.minecraft.stripped_mangrove_log"), "stripped_mangrove", StyleType.LOG)


            ;

    private final String literalName;
    public final Component name;
    public final String id;
    public final StyleType type;
    public final RenderStyle renderStyle;
    public final ResourceLocation texture;

    static final String LANG = "ropestyle." + VStuff.MOD_ID + ".";

    VStuffRopeStyle(String name, String id, StyleType type) {
        this.literalName = name;
        this.name = Component.translatable(LANG + id);
        this.id = id;
        this.type = type;
        this.renderStyle = type == StyleType.CHAIN ? RenderStyle.CHAIN : RenderStyle.NORMAL;
        switch (type) {
            case WOOL -> this.texture = woolStyle(id);
            case CHAIN -> this.texture = chainStyle();
            case LOG -> this.texture = logStyle(id);
            case DYE -> this.texture = dyedStyle(id);
            default -> this.texture = ropeStyle(id);
        }

        register(this);
    }

    VStuffRopeStyle(Component component, String id, StyleType type) {
        this.literalName = null;
        this.name = component;
        this.id = id;
        this.type = type;
        this.renderStyle = type == StyleType.CHAIN ? RenderStyle.CHAIN : RenderStyle.NORMAL;

        switch (type) {
            case WOOL -> this.texture = woolStyle(id);
            case CHAIN -> this.texture = chainStyle();
            case LOG -> this.texture = logStyle(id);
            case DYE -> this.texture = dyedStyle(id);
            default -> this.texture = ropeStyle(id);
        }

        register(this);
    }

    public void provideLang(BiConsumer<String, String> consumer) {
        if (literalName != null) consumer.accept(LANG + id, literalName);
    }

    public static void register(VStuffRopeStyle style) {
        STYLE_REGISTRY.put(style.id, style);
    }

    public static VStuffRopeStyle fromString(String id) {
        VStuffRopeStyle style = STYLE_REGISTRY.get(id);
        return style == null ? NORMAL : style;
    }

    public static final Map<String, VStuffRopeStyle> STYLE_REGISTRY = new HashMap<>();

    public static ResourceLocation ropeStyle(String style) {
        return VStuff.asTextureResource("rope/rope_" + style);
    }

    public static ResourceLocation dyedStyle(String color) {
        return VStuff.asTextureResource("rope/dyed/rope_" + color);
    }

    public static ResourceLocation woolStyle(String wool) {
        return new ResourceLocation("minecraft", "textures/block/" + wool);
    }

    public static ResourceLocation chainStyle() {
        return VStuff.asTextureResource("rope/rope_chain");
    }

    public static ResourceLocation logStyle(String log) {
        return new ResourceLocation("minecraft", "textures/block/" + log + "_log");
    }

    public enum StyleType {
        NORMAL,
        WOOL,
        CHAIN,
        LOG,
        DYE
    }

    public enum RenderStyle {
        NORMAL,
        CHAIN
    }

}
