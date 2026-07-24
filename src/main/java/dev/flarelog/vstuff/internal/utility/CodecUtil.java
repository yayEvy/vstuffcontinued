package dev.flarelog.vstuff.internal.utility;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.joml.Vector3d;
import dev.flarelog.vstuff.VStuff;

import java.util.*;

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

    public static final Codec<Vector3d> VECTOR3D = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vector3d::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vector3d::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vector3d::z)
    ).apply(i, Vector3d::new));

    public static <T> Tag encodeToTag(T object, Codec<T> codec) {
        return object != null ? codec.encodeStart(NbtOps.INSTANCE, object)
                .getOrThrow(false ,VStuff.LOGGER::error) : null;
    }

    public static <T> T decodeFromTag(Tag tag, Codec<T> codec) {
        return tag != null ? codec.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false ,VStuff.LOGGER::error) : null;
    }


    /**
     * Codec that works with almost anything :trollface:
     **/
    public static final Codec<Object> ANY_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Object, T>> decode(DynamicOps<T> ops, T input) {
            return JSON_ELEMENT.decode(ops, input)
                    .map(pair -> pair.mapFirst(CodecUtil::fromJson));
        }

        @Override
        public <T> DataResult<T> encode(Object input, DynamicOps<T> ops, T prefix) {
            return JSON_ELEMENT.encode(toJson(input), ops, prefix);
        }
    };

    public static final Codec<Map<String, Object>> ANY_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ANY_CODEC);


    private static Object fromJson(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;
        if (el.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
                map.put(e.getKey(), fromJson(e.getValue()));
            }
            return map;
        }
        if (el.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement e : el.getAsJsonArray()) list.add(fromJson(e));
            return list;
        }
        JsonPrimitive prim = el.getAsJsonPrimitive();
        if (prim.isBoolean()) return prim.getAsBoolean();
        if (prim.isNumber()) return prim.getAsNumber();
        return prim.getAsString();
    }

    private static JsonElement toJson(Object value) {
        if (value == null) return JsonNull.INSTANCE;
        if (value instanceof Map<?, ?> map) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                obj.add(String.valueOf(e.getKey()), toJson(e.getValue()));
            }
            return obj;
        }
        if (value instanceof Iterable<?> iterable) {
            JsonArray arr = new JsonArray();
            for (Object o : iterable) arr.add(toJson(o));
            return arr;
        }
        if (value instanceof Boolean b) return new JsonPrimitive(b);
        if (value instanceof Number n) return new JsonPrimitive(n);
        return new JsonPrimitive(String.valueOf(value));
    }
}
