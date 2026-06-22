package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.infrastructure.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.styling.data.RopeCategory;

public class VStuffRopeCategories {

    public static void bootstrap(BootstapContext<RopeCategory> ctx) {
        register(ctx, "basic_styles", new RopeCategory("Basic Styles", 0));
        register(ctx, "wool_styles", new RopeCategory("Wool Styles", 1));
        register(ctx, "color_styles", new RopeCategory("Color Styles", 2));
    }

    private static void register(BootstapContext<RopeCategory> ctx, String name, RopeCategory style) {
        ctx.register(ResourceKey.create(VStuffRegistries.ROPE_CATEGORY, VStuff.asResource(name)), style);
    }


}
