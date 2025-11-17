package yay.evy.everest.vstuff.util;

import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

import java.util.HashMap;
import java.util.Map;

public class RopeStyles {

    private static final Map<String, RopeStyle> STYLE_REGISTRY = new HashMap<>();

    public static void register(RopeStyle style) {
        STYLE_REGISTRY.put(style.getStyle(), style);
    }

    public static RopeStyle fromString(String styleId) {
        return STYLE_REGISTRY.get(styleId);
    }

    // === Silvern's existing code below ===

    public static ResourceLocation getRopeStyle(String style) {
        return new ResourceLocation(VStuff.MOD_ID, "textures/entity/rope/rope_" + style + ".png");
    }

    public static ResourceLocation getDyedRopeStyle(String color) {
        return new ResourceLocation(VStuff.MOD_ID, "textures/entity/rope/rope_" + color + ".png");
    }
    public static ResourceLocation getDyedWoolStyle(String color) {
        return new ResourceLocation("vstuff", "textures/entity/rope/" + color + "_wool.png");
    }

    public static ResourceLocation getChainStyle() {
        return new ResourceLocation("vstuff", "textures/entity/rope/rope_chain.png");
    }

    public static ResourceLocation getLogStyle(String log) {
        return new ResourceLocation("minecraft", "textures/block/" + log + "_log.png");
    }

    public enum RenderStyle {
        NORMAL,
        CHAIN
    }

    public enum PrimitiveRopeStyle {
        NORMAL,
        WOOL,
        DYED,
        CHAIN,
        OTHER,
        LOG

    }

    public static class RopeStyle {
        private final ResourceLocation associatedTexture;
        private final RenderStyle renderStyle;
        private final String langKey;
        private final String style;
        private final PrimitiveRopeStyle basicStyle;

        public RopeStyle(String style, PrimitiveRopeStyle basicStyle, String langKey) {
            this.langKey = langKey;
            this.style = style;
            this.basicStyle = basicStyle;
            this.renderStyle = (basicStyle == PrimitiveRopeStyle.CHAIN ? RenderStyle.CHAIN : RenderStyle.NORMAL);

            switch (basicStyle) {
                case WOOL -> this.associatedTexture = getDyedWoolStyle(style);
                case CHAIN -> this.associatedTexture = getChainStyle();
                case LOG -> this.associatedTexture = getLogStyle(style);
                case DYED ->  this.associatedTexture = getDyedRopeStyle(style);
                default -> this.associatedTexture = getRopeStyle(style);
            }

            // auto-register when created
            RopeStyles.register(this);
        }

        public RopeStyle(String style, String basicStyleSTR, String langKey) {
            this(style,
                    switch (basicStyleSTR) {
                        case "wool" -> PrimitiveRopeStyle.WOOL;
                        case "dyed" -> PrimitiveRopeStyle.DYED;
                        case "chain" -> PrimitiveRopeStyle.CHAIN;
                        case "log" -> PrimitiveRopeStyle.LOG;
                        default -> PrimitiveRopeStyle.NORMAL;
                    },
                    langKey
            );
        }

        public String asString() {
            return style; // cleaner, just the ID
        }

        public ResourceLocation getTexture() {
            return this.associatedTexture;
        }

        public RenderStyle getRenderStyle() {
            return this.renderStyle;
        }

        public String getLangKey() {
            return this.langKey;
        }

        public String getStyle() {
            return this.style;
        }

        public PrimitiveRopeStyle getBasicStyle() {
            return this.basicStyle;
        }

    }
}
