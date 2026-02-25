package yay.evy.everest.vstuff.internal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record RopeStyle(ResourceLocation id, Component name, RenderStyle renderStyle, ResourceLocation texture) {

    public enum RenderStyle {
        NORMAL,
        CHAIN,
        NORMAL_T,
        CHAIN_T
    }

    public RopeStyle makeTranslucent() {
        RenderStyle translucentStyle = renderStyle == RenderStyle.CHAIN ? RenderStyle.CHAIN_T : RenderStyle.NORMAL_T;
        return new RopeStyle(id, name, translucentStyle, texture);
    }
}
