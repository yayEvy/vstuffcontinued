package yay.evy.everest.vstuff.internal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record RopeStyle(ResourceLocation id, Component name, RenderStyle renderStyle, ResourceLocation texture) {

    public enum RenderStyle {
        NORMAL,
        CHAIN
    }
}
