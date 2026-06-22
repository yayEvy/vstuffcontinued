package yay.evy.everest.vstuff.internal.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import yay.evy.everest.vstuff.VStuff;

import java.util.Optional;

public class CodecUtil {

    public static PrimitiveCodec<Component> COMPONENT_TRANSLATABLE = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Component> read(DynamicOps<T> dynamicOps, T t) {
            return dynamicOps.getStringValue(t).map(Component::translatable);
        }

        @Override
        public <T> T write(DynamicOps<T> dynamicOps, Component component) {
            return dynamicOps.createString(((TranslatableContents) component.getContents()).getKey());
        }
    };

    public static Codec<JsonElement> JSON_ELEMENT = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );

    public static final Codec<JsonObject> JSON_OBJECT = CodecUtil.JSON_ELEMENT.xmap(JsonElement::getAsJsonObject, jsonObject -> jsonObject);

    public static <T> void encodeToTag(CompoundTag tag, String name, Codec<T> codec, T object) {
        codec.encodeStart(NbtOps.INSTANCE, object)
                .resultOrPartial(VStuff.LOGGER::error)
                .ifPresent(encoded -> tag.put(name, encoded));
    }

    public static <T> Optional<T> decodeFromTag(CompoundTag tag, String name, Codec<T> codec) {
        Tag object = tag.get(name);
        return codec.parse(NbtOps.INSTANCE, object)
                .resultOrPartial(VStuff.LOGGER::error);
    }

}
