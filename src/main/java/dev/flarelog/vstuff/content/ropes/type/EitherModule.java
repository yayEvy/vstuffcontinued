package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.mojang.datafixers.util.Either;

import java.io.IOException;

public class EitherModule extends SimpleModule {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public EitherModule() {
        super("EitherModule");
        addSerializer(new EitherSerializer());
        addDeserializer((Class) Either.class, new EitherDeserializer(null, null));
    }

    static class EitherSerializer extends StdSerializer<Either<?, ?>> {
        @SuppressWarnings("unchecked")
        EitherSerializer() {
            super((Class<Either<?, ?>>) (Class<?>) Either.class);
        }

        @Override
        public void serialize(Either<?, ?> either, JsonGenerator gen, SerializerProvider provider) throws IOException {
            provider.defaultSerializeValue(either.map(l -> (Object) l, r -> (Object) r), gen);
        }
    }

    static class EitherDeserializer extends StdDeserializer<Either<?, ?>> implements ContextualDeserializer {
        private final JavaType leftType, rightType;

        EitherDeserializer(JavaType leftType, JavaType rightType) {
            super(Either.class);
            this.leftType = leftType;
            this.rightType = rightType;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType t = ctxt.getContextualType();
            if (t == null && property != null) t = property.getType();
            if (t == null || t.getRawClass() != Either.class) return this;
            return new EitherDeserializer(t.containedTypeOrUnknown(0), t.containedTypeOrUnknown(1));
        }

        @Override
        public Either<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = ctxt.readTree(p);
            IOException leftError;
            try {
                return Either.left(read(p, ctxt, node, leftType));
            } catch (IOException e) {
                leftError = e;
            }
            try {
                return Either.right(read(p, ctxt, node, rightType));
            } catch (IOException rightError) {
                rightError.addSuppressed(leftError);
                return ctxt.reportInputMismatch(this, "Value matched neither side of Either: %s", rightError.getMessage());
            }
        }

        private Object read(JsonParser p, DeserializationContext ctxt, JsonNode n, JavaType type) throws IOException {
            JsonParser sub = n.traverse(p.getCodec());
            sub.nextToken();
            return ctxt.readValue(sub, type);
        }
    }
}
