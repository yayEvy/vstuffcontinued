package yay.evy.everest.vstuff.content.ropes;

import com.google.gson.JsonObject;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.rendering.RendererParamHelper;
import yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle;

import java.util.*;
import java.util.function.Function;

import static yay.evy.everest.vstuff.internal.utility.CodecUtil.sanitizeFileName;
import static yay.evy.everest.vstuff.internal.rendering.RendererParamHelper.ColorParamBuilder;
import static yay.evy.everest.vstuff.internal.rendering.RendererParamHelper.TextureParamBuilder;
import static yay.evy.everest.vstuff.internal.styling.data.RegistryRopeStyle.Builder;
import static yay.evy.everest.vstuff.internal.styling.StyleLists.*;

public class VStuffRopeStyles {
    public static final ResourceKey<RegistryRopeStyle> FALLBACK = ResourceKey.create(VStuffRegistries.ROPE_STYLES, VStuff.asResource("normal"));

    public static void bootstrap(BootstapContext<RegistryRopeStyle> ctx) {

        new BootstrapHelper()
                .add(new Builder()
                        .name("Normal")
                        .defaultCategory(ctx)
                        .defaultRenderer(ctx)
                        .build())
                .add(new Builder()
                        .name("Chain")
                        .defaultCategory(ctx)
                        .renderer(ctx, "chain")
                        .sounds(SoundEvents.CHAIN_PLACE, SoundEvents.CHAIN_BREAK)
                        .build())
                .addAll(ctx, WOOLS, "wool_styles", "normal", SoundEvents.WOOL_PLACE, SoundEvents.WOOL_BREAK, s -> new TextureParamBuilder().blockTexture(sanitizeFileName(s)).halfSize().build())
                .addAll(ctx, DYE_COLORS, "color_styles", "solid_color", SoundEvents.ITEM_FRAME_ADD_ITEM, SoundEvents.ITEM_FRAME_REMOVE_ITEM, s -> new ColorParamBuilder().hex(s).build())
                .addAll(ctx, LOGS, "wood_styles", "normal", SoundEvents.WOOD_PLACE, SoundEvents.WOOD_BREAK, s -> new TextureParamBuilder().blockTexture(sanitizeFileName(s)).halfSize().build())
                .addAll(ctx, CASINGS, "casing_styles", "normal", SoundEvents.WOOD_PLACE, SoundEvents.WOOD_BREAK, s -> new TextureParamBuilder().ropeTexture(sanitizeFileName(s)).build())
                .modify("Industrial Iron", builder -> builder.sounds(SoundEvents.METAL_PLACE, SoundEvents.METAL_BREAK).build())
                .modify("Train Casing", builder -> builder.sounds(SoundEvents.METAL_PLACE, SoundEvents.METAL_BREAK).build())
                .addAll(ctx, PRIDE, "pride_styles", "normal", SoundEvents.WOOL_PLACE, SoundEvents.WOOL_BREAK, s -> new TextureParamBuilder().ropeTexture(sanitizeFileName(s)).build())
                .add(new Builder()
                        .name("Candycane")
                        .category(ctx, "merry_vstuffmas")
                        .defaultRenderer(ctx)
                        .rendererParams(new TextureParamBuilder().ropeTexture("candycane").build())
                        .sounds(SoundEvents.AMETHYST_BLOCK_CHIME, SoundEvents.AMETHYST_BLOCK_BREAK)
                        .build())
                .add(new Builder()
                        .name("Christmas Tree")
                        .category(ctx, "merry_vstuffmas")
                        .defaultRenderer(ctx)
                        .rendererParams(new TextureParamBuilder().ropeTexture("christmas_tree").build())
                        .sounds(SoundEvents.GRASS_PLACE, SoundEvents.GRASS_BREAK)
                        .build())
                .build(ctx);
    }

    private static class BootstrapHelper {
        private final Map<String, RegistryRopeStyle> styles;

        BootstrapHelper() {
            this.styles = new HashMap<>();
        }

        BootstrapHelper add(RegistryRopeStyle style) {
            this.styles.put(style.rawName(), style);
            return this;
        }

        BootstrapHelper addAll(BootstapContext<RegistryRopeStyle> ctx, List<String> names, String category, String renderer, SoundEvent placeSound, SoundEvent breakSound, Function<String, JsonObject> paramFunc) {
            for (String name : names) {
                RegistryRopeStyle style = new Builder()
                        .name(name)
                        .category(ctx, category)
                        .renderer(ctx, renderer)
                        .rendererParams(paramFunc.apply(name))
                        .sounds(placeSound, breakSound)
                        .build();

                this.styles.put(style.rawName(), style);
            }

            return this;
        }

        BootstrapHelper addAll(BootstapContext<RegistryRopeStyle> ctx, Map<String, String> nameToParamInputMap, String category, String renderer, SoundEvent placeSound, SoundEvent breakSound, Function<String, JsonObject> paramFunc) {
            for (Map.Entry<String, String> nameToParamInput : nameToParamInputMap.entrySet()) {
                String name = nameToParamInput.getKey();
                String input = nameToParamInput.getValue();
                RegistryRopeStyle style = new Builder()
                        .name(name)
                        .category(ctx, category)
                        .renderer(ctx, renderer)
                        .rendererParams(paramFunc.apply(input))
                        .sounds(placeSound, breakSound)
                        .build();

                this.styles.put(style.rawName(), style);
            }

            return this;
        }

        BootstrapHelper modify(String name, Function<Builder, RegistryRopeStyle> modifierFunc) {
            if (styles.containsKey(name)) {
                Builder styleBuilder = styles.get(name).toBuilder();
                styles.replace(name, modifierFunc.apply(styleBuilder));
            }
            return this;
        }

        void build(BootstapContext<RegistryRopeStyle> ctx) {
            for (Map.Entry<String, RegistryRopeStyle> styleEntry : styles.entrySet()) {
                String name = sanitizeFileName(styleEntry.getKey());
                register(ctx, name, styleEntry.getValue());
            }
        }
    }
    private static void register(BootstapContext<RegistryRopeStyle> ctx, String name, RegistryRopeStyle type) {
        ctx.register(ResourceKey.create(VStuffRegistries.ROPE_STYLES, VStuff.asResource(name)), type);
    }
}
