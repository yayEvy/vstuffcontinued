package dev.flarelog.vstuff.content.ropes.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.flarelog.vstuff.VStuff;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {

    private static final Pattern EXPR = Pattern.compile("^\\s*\\$\\{(.+)\\}\\s*$", Pattern.DOTALL);

    private static final JexlEngine JEXL = new JexlBuilder()
            .permissions(JexlPermissions.RESTRICTED)
            .namespaces(Map.of("math", Math.class))
            .strict(true).cache(256).create();

    public static ObjectNode parseTree(ObjectNode node, JexlContext context) {
        walk(node, context);
        return node;
    }

    private static JsonNode walk(JsonNode node, JexlContext ctx) {
        if (node == null || node.isNull() || node.isMissingNode()) return node;

        if (node.isTextual()) return tryCalculateExpression(node, ctx);

        if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                arr.set(i, walk(arr.get(i), ctx));
            }
            return arr;
        }

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            List<String> names = new ArrayList<>();
            obj.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                obj.set(name, walk(obj.get(name), ctx));
            }
        }
        return node;
    }

    private static JsonNode tryCalculateExpression(JsonNode textNode, JexlContext ctx) {
        Matcher m = EXPR.matcher(textNode.asText());
        if (!m.matches()) return textNode; // not exp

        Object result = JEXL.createExpression(m.group(1)).evaluate(ctx); // eval ts

        if (result instanceof Number n) {
            if (Double.isFinite(n.doubleValue())) {
                return JsonNodeFactory.instance.numberNode(n.doubleValue());
            } else {
                throw new IllegalStateException("Expression " + m.group(1) + " returned a non finite number value");
            }
        }

        if (result != null) {
            return VStuff.getOrCreateMapper().valueToTree(result);
        }

        throw new IllegalStateException("Expression '" + m.group(1) + "' returned " + result);
    }
}
