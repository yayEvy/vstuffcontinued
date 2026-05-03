package yay.evy.everest.vstuff.internal.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

public class CodecUtil {

    public static PrimitiveCodec<Component> COMPONENT_TRANSLATABLE = new PrimitiveCodec<Component>() {
        @Override
        public <T> DataResult<Component> read(DynamicOps<T> dynamicOps, T t) {
            return dynamicOps.getStringValue(t).map(Component::translatable);
        }

        @Override
        public <T> T write(DynamicOps<T> dynamicOps, Component component) {
            return dynamicOps.createString(((TranslatableContents) component.getContents()).getKey());
        }
    };

    public static PrimitiveCodec<Component> COMPONENT_LITERAL = new PrimitiveCodec<Component>() { // idk might need it
        @Override
        public <T> DataResult<Component> read(DynamicOps<T> dynamicOps, T t) {
            return dynamicOps.getStringValue(t).map(Component::literal);
        }

        @Override
        public <T> T write(DynamicOps<T> dynamicOps, Component component) {
            return dynamicOps.createString(((LiteralContents) component.getContents()).text());
        }
    };

    public static Codec<JsonElement> JSON_ELEMENT = Codec.PASSTHROUGH.xmap(
        dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
        json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );

    public static final Codec<JsonObject> JSON_OBJECT = CodecUtil.JSON_ELEMENT.xmap(JsonElement::getAsJsonObject, jsonObject -> jsonObject);

}
