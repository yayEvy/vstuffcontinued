package yay.evy.everest.vstuff.internal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record RopeStyle(ResourceLocation id, Component name, boolean chain, ResourceLocation texture) {}
