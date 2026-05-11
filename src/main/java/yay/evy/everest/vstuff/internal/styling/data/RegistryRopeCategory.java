package yay.evy.everest.vstuff.internal.styling.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.api.registry.VStuffRegistries;
import yay.evy.everest.vstuff.infrastructure.data.provider.RopeLangProvider;
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
            RegistryCodecs.homogeneousList(VStuffRegistries.ROPE_STYLES).optionalFieldOf("styles", HolderSet.direct()).forGetter(RegistryRopeCategory::styles)
    ).apply(instance, RegistryRopeCategory::new));

    public static final Codec<List<Holder<RegistryRopeCategory>>> LIST_CODEC = RegistryFileCodec.create(VStuffRegistries.ROPE_CATEGORIES, RegistryRopeCategory.CODEC).listOf();
    public static final Codec<List<Holder<RegistryRopeCategory>>> NETWORK_LIST_CODEC = CODEC.xmap(Holder::direct, Holder::value).listOf();

    public static class Builder {
        private String rawName;
        private Component name;
        private int order;

        public Builder name(String rawName) {
            this.rawName = rawName;
            String translationKey = "ropecategory.vstuff." + CodecUtil.sanitizeFileName(rawName);
            this.name = Component.translatable(translationKey);
            RopeLangProvider.addTranslation(translationKey, rawName);
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public RegistryRopeCategory build() {
            return new RegistryRopeCategory(rawName, name, order, HolderSet.direct());
        }
    }
}