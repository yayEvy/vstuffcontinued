package yay.evy.everest.vstuff.internal.styling.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.index.VStuffItems;
import yay.evy.everest.vstuff.internal.rendering.RendererParamHelper;
import yay.evy.everest.vstuff.internal.utility.CodecUtil;
import yay.evy.everest.vstuff.internal.rendering.RegistryRopeRendererType;
import yay.evy.everest.vstuff.internal.utility.EntityUtils;

import static yay.evy.everest.vstuff.VStuff.asResource;

public record RegistryRopeStyle (
        String rawName,
        Component name,
        Holder<RegistryRopeCategory> category,
        ResourceLocation renderer,
        JsonObject rendererParams,// parsed by the renderer on client
        SoundEvent placeSound,
        SoundEvent breakSound
) {

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
        RegistryFileCodec.create(VStuffRegistries.CATEGORIES, RegistryRopeCategory.CODEC).fieldOf("category").forGetter(RegistryRopeStyle::category),
        ResourceLocation.CODEC.optionalFieldOf("renderer", asResource(RegistryRopeRendererType.BuiltInRenderers.NORMAL)).forGetter(RegistryRopeStyle::renderer),
        CodecUtil.JSON_OBJECT.optionalFieldOf("rendererParams", RendererParamHelper.defaultTexture()).forGetter(RegistryRopeStyle::rendererParams),
        SoundEvent.DIRECT_CODEC.optionalFieldOf("placeSound", SoundEvents.WOOL_PLACE).forGetter(RegistryRopeStyle::placeSound),
        SoundEvent.DIRECT_CODEC.optionalFieldOf("breakSound", SoundEvents.WOOL_BREAK).forGetter(RegistryRopeStyle::breakSound)
    ).apply(instance, RegistryRopeStyle::new));

}
