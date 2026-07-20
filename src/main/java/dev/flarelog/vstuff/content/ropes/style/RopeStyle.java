package dev.flarelog.vstuff.content.ropes.style;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.data.provider.RopeLangProvider;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.rendering.IRopeRenderer;
import dev.flarelog.vstuff.internal.rendering.RopeRendererType;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import dev.flarelog.vstuff.internal.utility.TagUtils;

import java.util.ArrayList;
import java.util.List;

public record RopeStyle (
        Component name,
        String rawName,
        List<Holder<RopeCategory>> categories,
        Holder<RopeRendererType> renderer,
        JsonObject rendererParams,      // parsed by the renderer on client
        SoundEvent placeSound,
        SoundEvent breakSound
) {

    public static Tag keyToTag(ResourceKey<RopeStyle> style) {
        return CodecUtil.encodeToTag(style, ResourceKey.codec(VStuffRegistries.ROPE_STYLE));
    }

    public static ResourceKey<RopeStyle> tagToKey(Tag tag) {
        return CodecUtil.decodeFromTag(tag, ResourceKey.codec(VStuffRegistries.ROPE_STYLE));
    }

    public IRopeRenderer createRenderer() {
        return renderer.get().create(rendererParams);
    }

    public static final Codec<RopeStyle> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecUtil.COMPONENT_TRANSLATABLE.fieldOf("name").forGetter(RopeStyle::name),
            Codec.STRING.fieldOf("rawName").forGetter(RopeStyle::rawName),
            RopeCategory.CODEC.listOf().fieldOf("categories").forGetter(RopeStyle::categories),
            RopeRendererType.CODEC.fieldOf("renderer").forGetter(RopeStyle::renderer),
            CodecUtil.JSON_OBJECT.fieldOf("rendererParams").forGetter(RopeStyle::rendererParams),
            SoundEvent.DIRECT_CODEC.optionalFieldOf("placeSound", SoundEvents.WOOL_PLACE).forGetter(RopeStyle::placeSound),
            SoundEvent.DIRECT_CODEC.optionalFieldOf("breakSound", SoundEvents.WOOL_BREAK).forGetter(RopeStyle::breakSound)
    ).apply(i, RopeStyle::new)); // codec of doom and despair

    public static class Builder {
        private String rawName;
        private Component name;
        private final ArrayList<Holder<RopeCategory>> categories;
        private Holder<RopeRendererType> renderer;
        private JsonObject rendererParams;
        private SoundEvent placeSound;
        private SoundEvent breakSound;

        public Builder() {
            this.categories = new ArrayList<>();
            this.placeSound = SoundEvents.WOOL_PLACE;
            this.breakSound = SoundEvents.WOOL_BREAK;
        }

        public Builder name(String rawName) {
            this.rawName = rawName;
            String translationKey = "ropestyle.vstuff." + TagUtils.sanitizeFileName(rawName);
            this.name = Component.translatable(translationKey);
            RopeLangProvider.addTranslation(translationKey, rawName);
            return this;
        }

        public Builder categories(List<Holder<RopeCategory>> categories) {
            this.categories.addAll(categories);
            return this;
        }

        public Builder category(Holder<RopeCategory> category) {
            this.categories.add(category);
            return this;
        }

        public Builder category(BootstapContext<RopeStyle> ctx, ResourceLocation category) {
            return category(ctx.lookup(VStuffRegistries.ROPE_CATEGORY).getOrThrow(ResourceKey.create(VStuffRegistries.ROPE_CATEGORY, category)));
        }

        public Builder category(BootstapContext<RopeStyle> ctx, String category) {
            return category(ctx, VStuff.asResource(category));
        }

        public Builder uncategorized(BootstapContext<RopeStyle> ctx) {
            return category(ctx, "uncategorized");
        }

        public Builder defaultCategory(BootstapContext<RopeStyle> ctx) {
            return category(ctx, "basic_styles");
        }

        public Builder renderer(Holder<RopeRendererType> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder renderer(BootstapContext<RopeStyle> ctx, ResourceLocation renderer) {
            return renderer(ctx.lookup(VStuffRegistries.ROPE_RENDERER_TYPE).getOrThrow(ResourceKey.create(VStuffRegistries.ROPE_RENDERER_TYPE, renderer)));
        }

        public Builder renderer(BootstapContext<RopeStyle> ctx, String renderer) {
            return renderer(ctx, VStuff.asResource(renderer));
        }

        public Builder defaultRenderer(BootstapContext<RopeStyle> ctx) {
            return renderer(ctx, "normal");
        }

        public Builder rendererParams(JsonObject rendererParams) {
            this.rendererParams = rendererParams;
            return this;
        }


        public Builder sounds(SoundEvent placeSound, SoundEvent breakSound) {
            this.placeSound = placeSound;
            this.breakSound = breakSound;
            return this;
        }

        public RopeStyle build() {
            return new RopeStyle(name, rawName, categories, renderer, rendererParams, placeSound, breakSound);
        }
    }
}
