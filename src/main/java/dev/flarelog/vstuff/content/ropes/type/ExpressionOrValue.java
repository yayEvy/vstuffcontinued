package dev.flarelog.vstuff.content.ropes.type;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import lombok.Getter;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class ExpressionOrValue<T> {

    public static final Codec<ExpressionOrValue<Double>> DOUBLE = codec(Codec.DOUBLE, Double.class);
    public static final Codec<ExpressionOrValue<Float>> FLOAT = codec(Codec.FLOAT, Float.class);
    public static final Codec<ExpressionOrValue<Integer>> INT = codec(Codec.INT, Integer.class);
    public static final Codec<ExpressionOrValue<Long>> LONG = codec(Codec.LONG, Long.class);
    public static final Codec<ExpressionOrValue<Boolean>> BOOL = codec(Codec.BOOL, Boolean.class);

    private final T value;
    private final String expression;
    private final Class<T> clazz;

    @Getter(lombok.AccessLevel.NONE)
    private volatile JexlExpression compiled;

    private ExpressionOrValue(T value, String expression, Class<T> clazz) {
        this.value = value;
        this.expression = expression;
        this.clazz = Objects.requireNonNull(clazz, "clazz");
    }

    @ParametersAreNonnullByDefault
    public static <T> ExpressionOrValue<T> value(T value, Class<T> clazz) {
        return new ExpressionOrValue<>(value, null, clazz);
    }

    @ParametersAreNonnullByDefault
    public static <T> ExpressionOrValue<T> expression(String expression, Class<T> clazz) {
        return new ExpressionOrValue<>(null, expression, clazz);
    }

    public boolean isExpression() {
        return expression != null;
    }

    public T resolve(JexlEngine jexl, JexlContext context) {
        if (!isExpression()) {
            return value;
        }
        JexlExpression expr = compiled;
        if (expr == null) {
            expr = jexl.createExpression(expression);
            compiled = expr;
        }
        return coerce(expr.evaluate(context));
    }

    private T coerce(Object result) {
        if (result == null) return null;
        if (clazz.isInstance(result)) return clazz.cast(result);

        if (result instanceof Number n) {
            if (clazz == Double.class)  return clazz.cast(n.doubleValue());
            if (clazz == Float.class)   return clazz.cast(n.floatValue());
            if (clazz == Integer.class) return clazz.cast(n.intValue());
            if (clazz == Long.class)    return clazz.cast(n.longValue());
        }
        throw new IllegalStateException("Expression '" + expression + "' returned "
                + result.getClass().getSimpleName() + ", expected " + clazz.getSimpleName());
    }

    public static <T> Codec<ExpressionOrValue<T>> codec(Codec<T> valueCodec, Class<T> clazzz) {
        Codec<ExpressionOrValue<T>> valueSide = valueCodec.xmap(
                v -> ExpressionOrValue.value(v, clazzz),
                ExpressionOrValue::getValue
        );
        Codec<ExpressionOrValue<T>> expressionSide = Codec.STRING.xmap(
                s -> ExpressionOrValue.expression(s, clazzz),
                ExpressionOrValue::getExpression
        );

        return Codec.either(valueSide, expressionSide).xmap(
                either -> either.map(Function.identity(), Function.identity()),
                eov -> eov.isExpression() ? Either.right(eov) : Either.left(eov)
        );
    }
}