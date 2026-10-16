package dev.flarelog.vstuff.internal.utility;

import java.util.Locale;

public class TagUtils {
    public static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
    }
}
