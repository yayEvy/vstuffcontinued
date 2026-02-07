package yay.evy.everest.vstuff.internal;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RopeStyleCategory {

    public final ResourceLocation id;
    public final Component name;
    public final int order;

    public final List<RopeStyle> styles = new ArrayList<>();

    public RopeStyleCategory(ResourceLocation id, Component name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public Component getName() {
        return name;
    }
}

