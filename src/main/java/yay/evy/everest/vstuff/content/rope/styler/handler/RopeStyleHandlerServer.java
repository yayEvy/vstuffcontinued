package yay.evy.everest.vstuff.content.rope.styler.handler;

import yay.evy.everest.vstuff.foundation.RopeStyles;

import java.util.HashMap;
import java.util.UUID;


public class RopeStyleHandlerServer {

    private static final HashMap<UUID, RopeStyles.RopeStyle> selectedStyles = new HashMap<>();


    public static void addStyle(UUID uuid, RopeStyles.RopeStyle style) {
        selectedStyles.put(uuid, style);
    }

    public static RopeStyles.RopeStyle getStyle(UUID uuid) {
        if (!selectedStyles.containsKey(uuid)) {
            selectedStyles.put(uuid, RopeStyles.normal());
        }

        return selectedStyles.get(uuid);
    }

}
