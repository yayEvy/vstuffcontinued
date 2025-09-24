package yay.evy.everest.vstuff.util;

import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;

public class RopeStyles {

    public static ResourceLocation getRopeStyle(String style) {
        return new ResourceLocation(VStuff.MOD_ID, "textures/entity/rope/rope_" + style + ".png");
    }

    public static ResourceLocation getDyedRopeStyle(String color) {
        return new ResourceLocation(VStuff.MOD_ID, "textures/entity/rope/rope_dyed_" + color + ".png");
    }

    public static ResourceLocation getDyedWoolStyle(String color) {
        return new ResourceLocation("minecraft", "textures/block/" + color + "_wool.png");
    }

    // New method to get the vanilla Minecraft chain texture
    public static ResourceLocation getChainStyle() {
        return new ResourceLocation("minecraft", "textures/item/chain.png");
    }


    public enum RenderStyle {
        NORMAL,
        CHAIN
    }

    public enum PrimitiveRopeStyle {
        NORMAL,
        DYED,
        WOOL,
        CHAIN,
        OTHER,
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

            if (basicStyle == PrimitiveRopeStyle.CHAIN) {
                this.renderStyle = RenderStyle.CHAIN;
            } else {
                this.renderStyle = RenderStyle.NORMAL;
            }

            switch (basicStyle) {
                case DYED -> this.associatedTexture = getDyedRopeStyle(style);
                case WOOL -> this.associatedTexture = getDyedWoolStyle(style);
                case CHAIN -> this.associatedTexture = getChainStyle(); // Use new method
                default -> this.associatedTexture = getRopeStyle(style);
            }
        }

        public RopeStyle(String style, String basicStyleSTR, String langKey) {
            PrimitiveRopeStyle basicStyle;

            switch (basicStyleSTR) {
                case "dyed" -> basicStyle = PrimitiveRopeStyle.DYED;
                case "wool" -> basicStyle = PrimitiveRopeStyle.WOOL;
                case "chain" -> basicStyle = PrimitiveRopeStyle.CHAIN;
                default -> basicStyle = PrimitiveRopeStyle.NORMAL;
            }

            this.langKey = langKey;
            this.style = style;
            this.basicStyle = basicStyle;

            if (basicStyle == PrimitiveRopeStyle.CHAIN) {
                this.renderStyle = RenderStyle.CHAIN;
            } else {
                this.renderStyle = RenderStyle.NORMAL;
            }

            switch (basicStyle) {
                case DYED -> this.associatedTexture = getDyedRopeStyle(style);
                case WOOL -> this.associatedTexture = getDyedWoolStyle(style);
                case CHAIN -> this.associatedTexture = getChainStyle(); // Use new method
                default -> this.associatedTexture = getRopeStyle(style);
            }
        }

        public String asString() {
            return "RopeStyle with style " + style +
                    ", basicStyle " + basicStyle.toString() +
                    ", renderStyle " + renderStyle.toString() +
                    ", texture at " + associatedTexture.getPath() +
                    ", langKey " + langKey;
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

        public boolean isDyeable() {
            return ((this.basicStyle == PrimitiveRopeStyle.WOOL) || (this.basicStyle == PrimitiveRopeStyle.NORMAL));

        }
    }
}
