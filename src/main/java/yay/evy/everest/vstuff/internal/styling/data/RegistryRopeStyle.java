package yay.evy.everest.vstuff.internal.styling.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.infrastructure.data.provider.RopeLangProvider;
import yay.evy.everest.vstuff.internal.rendering.RendererParamHelper;
import yay.evy.everest.vstuff.internal.utility.CodecUtil;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;

import static yay.evy.everest.vstuff.VStuff.asResource;

public record RegistryRopeStyle (
        String rawName,
        Component name,
        Holder<RegistryRopeCategory> category,
        Holder<RegistryRopeRendererType> renderer,
        JsonObject rendererParams,// parsed by the renderer on client
        SoundEvent placeSound,
        SoundEvent breakSound
) {

    public Builder toBuilder() {
        return new Builder()
                .name(rawName)
                .category(category)
                .renderer(renderer)
                .rendererParams(rendererParams)
                .sounds(placeSound, breakSound);
    }

    public static void set(Player player, String style) {
        InteractionHand hand = EntityUtils.holdingInHand(player, stack -> stack.is(VStuffItems.STYLING_AVAILABLE));
        if (hand == null) return;
        set(player.getItemInHand(hand), style);
    }

    public static void set(ItemStack stack, String style) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("style", style);
    }

    public static final Codec<RegistryRopeStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("rawName").forGetter(RegistryRopeStyle::rawName),
        CodecUtil.COMPONENT_TRANSLATABLE.fieldOf("name").forGetter(RegistryRopeStyle::name),
        RegistryFileCodec.create(VStuffRegistries.ROPE_CATEGORIES, RegistryRopeCategory.CODEC).fieldOf("category").forGetter(RegistryRopeStyle::category),
        RegistryFileCodec.create(VStuffRegistries.ROPE_RENDERERS, RegistryRopeRendererType.CODEC).fieldOf("renderer").forGetter(RegistryRopeStyle::renderer),
        CodecUtil.JSON_OBJECT.fieldOf("rendererParams").forGetter(RegistryRopeStyle::rendererParams),
        SoundEvent.DIRECT_CODEC.fieldOf("placeSound").forGetter(RegistryRopeStyle::placeSound),
        SoundEvent.DIRECT_CODEC.fieldOf("breakSound").forGetter(RegistryRopeStyle::breakSound)
    ).apply(instance, RegistryRopeStyle::new));

    public static class Builder {
        private String rawName;
        private Component name;
        private Holder<RegistryRopeCategory> category;
        private Holder<RegistryRopeRendererType> renderer;
        private JsonObject rendererParams = new RendererParamHelper.TextureParamBuilder().build();
        private SoundEvent placeSound = SoundEvents.WOOL_PLACE;
        private SoundEvent breakSound = SoundEvents.WOOL_BREAK;

        public Builder name(String rawName) {
            this.rawName = rawName;
            String translationKey = "ropestyle.vstuff." + CodecUtil.sanitizeFileName(rawName);
            this.name = Component.translatable(translationKey);
            RopeLangProvider.addTranslation(translationKey, rawName);
            return this;
        }

        public Builder category(Holder<RegistryRopeCategory> category) {
            this.category = category;
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
            return new RegistryRopeStyle(rawName, name, category, renderer, rendererParams, placeSound, breakSound);
        }
    }

}
