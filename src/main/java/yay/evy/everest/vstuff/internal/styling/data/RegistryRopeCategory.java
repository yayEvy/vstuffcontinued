package yay.evy.everest.vstuff.internal.styling.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.internal.utility.CodecUtil;

import java.util.List;

public record RegistryRopeCategory(
        String rawName,
        Component name,
        int order,
        HolderSet<RegistryRopeStyle> styles   // filled by RopeTypeRegistry.buildSortedCategories()
) {
    public boolean hasStyles() {
        return !(styles.size() > 0);
    }

    public static final Codec<RegistryRopeCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("rawName").forGetter(RegistryRopeCategory::rawName),
            CodecUtil.COMPONENT_TRANSLATABLE.fieldOf("name").forGetter(RegistryRopeCategory::name),
            Codec.INT.fieldOf("order").forGetter(RegistryRopeCategory::order),
            RegistryCodecs.homogeneousList(VStuffRegistries.STYLES).optionalFieldOf("styles", HolderSet.direct()).forGetter(RegistryRopeCategory::styles)
    ).apply(instance, RegistryRopeCategory::new));
}