package yay.evy.everest.vstuff.internal.styling.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.data.provider.RopeLangProvider;
import yay.evy.everest.vstuff.client.rope.render.IRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.RendererParamHelper;
import yay.evy.everest.vstuff.internal.utility.CodecUtil;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;
import yay.evy.everest.vstuff.internal.utility.TagUtils;

import java.util.ArrayList;
import java.util.List;

import static yay.evy.everest.vstuff.VStuff.LOGGER;

public record RegistryRopeStyle (
        String rawName,
        Component name,
        List<Holder<RegistryRopeCategory>> categories,
        Holder<RegistryRopeRendererType> renderer,
        JsonObject rendererParams,// parsed by the renderer on client
        SoundEvent placeSound,
        SoundEvent breakSound
) {

    public Builder toBuilder() {
        return new Builder()
                .name(rawName)
                .categories(categories)
                .renderer(renderer)
                .rendererParams(rendererParams)
                .sounds(placeSound, breakSound);
    }

    public IRopeRenderer createRenderer() {
        return renderer.get().create(rendererParams);
    }

    public static void set(Player player, ResourceLocation style) {
        InteractionHand hand = EntityUtils.holdingInHand(player, stack -> stack.is(VStuffItems.STYLING_AVAILABLE));
        if (hand == null) return;
        set(player.getItemInHand(hand), style);
    }

    public static void set(ItemStack stack, ResourceLocation style) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("style", TagUtils.writeResourceLocation(style));
    }

    public static RegistryRopeStyle get(Entity entity, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(VStuffItems.STYLING_AVAILABLE)) return null;
        CompoundTag tag = stack.getOrCreateTag();
        Registry<RegistryRopeStyle> registry = entity.level().registryAccess().registryOrThrow(VStuffRegistries.ROPE_STYLES);
        if (!tag.contains("style", Tag.TAG_COMPOUND)) return registry.get(VStuff.asResource("normal"));
        return registry.get(TagUtils.readResourceLocation(tag.getCompound("style")));
    }

    public static final Codec<RegistryRopeStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("rawName").forGetter(RegistryRopeStyle::rawName),
        CodecUtil.COMPONENT_TRANSLATABLE.fieldOf("name").forGetter(RegistryRopeStyle::name),
        RegistryRopeCategory.LIST_CODEC.fieldOf("categories").forGetter(RegistryRopeStyle::categories),
        RegistryFileCodec.create(VStuffRegistries.ROPE_RENDERERS, RegistryRopeRendererType.CODEC).fieldOf("renderer").forGetter(RegistryRopeStyle::renderer),
        CodecUtil.JSON_OBJECT.fieldOf("rendererParams").forGetter(RegistryRopeStyle::rendererParams),
        SoundEvent.DIRECT_CODEC.fieldOf("placeSound").forGetter(RegistryRopeStyle::placeSound),
        SoundEvent.DIRECT_CODEC.fieldOf("breakSound").forGetter(RegistryRopeStyle::breakSound)
    ).apply(instance, RegistryRopeStyle::new));

    public static final Codec<RegistryRopeStyle> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("rawName").forGetter(RegistryRopeStyle::rawName),
        CodecUtil.COMPONENT_TRANSLATABLE.fieldOf("name").forGetter(RegistryRopeStyle::name),
        RegistryRopeCategory.NETWORK_LIST_CODEC.fieldOf("categories").forGetter(RegistryRopeStyle::categories),
        RegistryRopeRendererType.CODEC.xmap(Holder::direct, Holder::value).fieldOf("renderer").forGetter(RegistryRopeStyle::renderer),
        CodecUtil.JSON_OBJECT.fieldOf("rendererParams").forGetter(RegistryRopeStyle::rendererParams),
        SoundEvent.DIRECT_CODEC.fieldOf("placeSound").forGetter(RegistryRopeStyle::placeSound),
        SoundEvent.DIRECT_CODEC.fieldOf("breakSound").forGetter(RegistryRopeStyle::breakSound)
    ).apply(instance, RegistryRopeStyle::new));

    public static CompoundTag encode(RegistryRopeStyle style) {
        return (CompoundTag) RegistryRopeStyle.CODEC.encodeStart(NbtOps.INSTANCE, style)
                .resultOrPartial(e -> LOGGER.error("Encodin"))
                .orElseThrow();
    }

    public static RegistryRopeStyle parse(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(e -> LOGGER.error("s"))
                .orElseThrow();
    }

    public static class Builder {
        private String rawName;
        private Component name;
        private final ArrayList<Holder<RegistryRopeCategory>> categories;
        private Holder<RegistryRopeRendererType> renderer;
        private JsonObject rendererParams;
        private SoundEvent placeSound;
        private SoundEvent breakSound;

        public Builder() {
            this.categories = new ArrayList<>();
            this.rendererParams = new RendererParamHelper.TextureParamBuilder().build();
            this.placeSound = SoundEvents.WOOL_PLACE;
            this.breakSound = SoundEvents.WOOL_BREAK;
        }

        public Builder name(String rawName) {
            this.rawName = rawName;
            String translationKey = "ropestyle.vstuff." + CodecUtil.sanitizeFileName(rawName);
            this.name = Component.translatable(translationKey);
            RopeLangProvider.addTranslation(translationKey, rawName);
            return this;
        }

        public Builder categories(List<Holder<RegistryRopeCategory>> categories) {
            this.categories.addAll(categories);
            return this;
        }

        public Builder category(Holder<RegistryRopeCategory> category) {
            this.categories.add(category);
            return this;
        }

        public Builder category(BootstapContext<RegistryRopeStyle> ctx, ResourceLocation category) {
            return category(ctx.lookup(VStuffRegistries.ROPE_CATEGORIES).getOrThrow(ResourceKey.create(VStuffRegistries.ROPE_CATEGORIES, category)));
        }

        public Builder category(BootstapContext<RegistryRopeStyle> ctx, String category) {
            return category(ctx, VStuff.asResource(category));
        }

        public Builder uncategorized(BootstapContext<RegistryRopeStyle> ctx) {
            return category(ctx, "uncategorized");
        }

        public Builder defaultCategory(BootstapContext<RegistryRopeStyle> ctx) {
            return category(ctx, "basic_styles");
        }

        public Builder renderer(Holder<RegistryRopeRendererType> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder renderer(BootstapContext<RegistryRopeStyle> ctx, ResourceLocation renderer) {
            return renderer(ctx.lookup(VStuffRegistries.ROPE_RENDERERS).getOrThrow(ResourceKey.create(VStuffRegistries.ROPE_RENDERERS, renderer)));
        }

        public Builder renderer(BootstapContext<RegistryRopeStyle> ctx, String renderer) {
            return renderer(ctx, VStuff.asResource(renderer));
        }

        public Builder defaultRenderer(BootstapContext<RegistryRopeStyle> ctx) {
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

        public Builder defaultSounds() {
            return sounds(SoundEvents.WOOL_PLACE, SoundEvents.WOOL_BREAK);
        }

        public RegistryRopeStyle build() {
            return new RegistryRopeStyle(rawName, name, categories, renderer, rendererParams, placeSound, breakSound);
        }
    }

}
