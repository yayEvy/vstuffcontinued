package yay.evy.everest.vstuff.infrastructure.data.provider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RopeLangProvider {

    public static void addTranslation(String key, String value) {
        TRANSLATIONS.put(key, value);
    }

    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    public static void provideLang(BiConsumer<String, String> consumer) {
        for (Map.Entry<String, String> translation : TRANSLATIONS.entrySet()) {
            consumer.accept(translation.getKey(), translation.getValue());
        }
    }
}