package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.flarelog.vstuff.VStuff;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.*;

public class ExpressionParser {
    private final Class<?> clazz;

    public ExpressionParser(Class<?> clazz) {
        this.clazz = clazz;
    }

    public <T> ObjectNode parseTree(ObjectNode node) {
        ObjectMapper mapper = VStuff.getOrCreateMapper();
        JavaType type = mapper.constructType(this.clazz);
        walk(mapper, node, type);
        return node;
    }

    private JsonNode walk(ObjectMapper mapper, JsonNode node, JavaType type) {
        if (node == null || node.isNull() || node.isMissingNode()) return node;
        
        if (node.isTextual()) {
            return isNumeric(type.getRawClass()) ? toNumber(node) : node;
        }
        
        if (node.isArray() && (type.isCollectionLikeType() || type.isArrayType())) {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                arr.set(i, walk(mapper, arr.get(i), type.getContentType()));
            }
            return arr;
        }
        
        if (node.isObject()) {
            Class<?> raw = type.getRawClass();
            if (raw == Object.class || JsonNode.class.isAssignableFrom(raw)) return node;

            ObjectNode obj = (ObjectNode) node;
            List<String> names = new ArrayList<>();
            obj.fieldNames().forEachRemaining(names::add);   
            
            if (type.isMapLikeType()) {
                for (String name : names) {
                    obj.set(name, walk(mapper, obj.get(name), type.getContentType()));
                }
            } else {
                Map<String, JavaType> propTypes = new HashMap<>();
                BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
                for (BeanPropertyDefinition p : desc.findProperties()) {
                    propTypes.put(p.getName(), p.getPrimaryType());
                }
                for (String name : names) {
                    JavaType propType = propTypes.get(name);
                    if (propType != null) {
                        obj.set(name, walk(mapper, obj.get(name), propType));
                    }
                }
            }
        }
        return node;
    }

    private static boolean isNumeric(Class<?> c) {
        return (c.isPrimitive() && c != boolean.class && c != char.class)
                || Number.class.isAssignableFrom(c);
    }

    private static JsonNode toNumber(JsonNode textNode) {
        Expression exp = new Expression(textNode.asText());
        return exp.checkSyntax() ? JsonNodeFactory.instance.numberNode(exp.calculate()) : textNode;
    }
}
