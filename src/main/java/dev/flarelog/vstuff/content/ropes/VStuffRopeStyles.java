package dev.flarelog.vstuff.content.ropes;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.rendering.RendererParamHelper;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;

import java.util.Map;

import static dev.flarelog.vstuff.content.ropes.style.RopeStyleManager.DYE_COLORS;
import static dev.flarelog.vstuff.content.ropes.style.RopeStyleManager.WOOLS;
import static dev.flarelog.vstuff.content.ropes.style.RopeStyle.Builder;
import static dev.flarelog.vstuff.internal.utility.TagUtils.sanitizeFileName;

public class VStuffRopeStyles {

    public static void bootstrap(BootstapContext<RopeStyle> ctx) {
        register(ctx, "normal",
                new Builder()
                        .name("Normal")
                        .defaultCategory(ctx)
                        .defaultRenderer(ctx)
                        .rendererParams(RendererParamHelper.ropeTexture("normal"))
                        .build()
        );

        register(ctx, "chain",
                new Builder()
                        .name("Chain")
                        .defaultCategory(ctx)
                        .renderer(ctx, "chain")
                        .sounds(SoundEvents.CHAIN_PLACE, SoundEvents.CHAIN_BREAK)
                        .rendererParams(RendererParamHelper.ropeTexture("chain"))
                        .build()
        );

        for (String wool : WOOLS) {
            register(ctx, sanitizeFileName(wool),
                    new Builder()
                            .name(wool)
                            .defaultRenderer(ctx)
                            .category(ctx, "wool_styles")
                            .rendererParams(RendererParamHelper.blockTexture(sanitizeFileName(wool), 0.5f))
                            .build()
            );
        }

        for (Map.Entry<String, String> color : DYE_COLORS.entrySet()) {
            register(ctx, sanitizeFileName(color.getKey()),
                    new Builder()
                            .name(color.getKey())
                            .renderer(ctx, "solid_color")
                            .category(ctx, "color_styles")
                            .rendererParams(RendererParamHelper.color(color.getValue()))
                            .sounds(SoundEvents.ITEM_FRAME_ADD_ITEM, SoundEvents.ITEM_FRAME_REMOVE_ITEM)
                            .build()
            );
        }
    }

    private static void register(BootstapContext<RopeStyle> ctx, String name, RopeStyle style) {
        ctx.register(ResourceKey.create(VStuffRegistries.ROPE_STYLE, VStuff.asResource(name)), style);
    }
}