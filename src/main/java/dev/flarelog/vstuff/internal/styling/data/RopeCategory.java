package dev.flarelog.vstuff.internal.styling.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import dev.flarelog.vstuff.infrastructure.data.provider.RopeLangProvider;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.utility.CodecUtil;

import static dev.flarelog.vstuff.internal.utility.TagUtils.sanitizeFileName;

public record RopeCategory(
        Component name,
        String rawName,
        int order
) {
    public RopeCategory(String name, int order) {
        this(Component.translatable("ropecategory.vstuff." + sanitizeFileName(name)), name, order);
        RopeLangProvider.addTranslation("ropecategory.vstuff." + sanitizeFileName(name), name);
    }

    public static final Codec<RopeCategory> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecUtil.COMPONENT_TRANSLATABLE.fieldOf("name").forGetter(RopeCategory::name),
            Codec.STRING.fieldOf("rawName").forGetter(RopeCategory::rawName),
            Codec.INT.fieldOf("order").forGetter(RopeCategory::order)
    ).apply(i, RopeCategory::new));

    public static final Codec<Holder<RopeCategory>> CODEC = RegistryFileCodec.create(VStuffRegistries.ROPE_CATEGORY, DIRECT_CODEC);

}