package yay.evy.everest.vstuff.content.ropestyler.handler;

import org.jetbrains.annotations.Nullable;

import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.HashMap;
import java.util.UUID;


public class RopeStyleHandlerServer {

    @Nullable private static UUID currentPlayer = null;
    private static final HashMap<UUID, RopeStyles.RopeStyle> selectedStyles = new HashMap<>();


    public static @Nullable UUID getCurrentPlayer() {
        return currentPlayer;
    }

    public static void setCurrentPlayer(@Nullable UUID uuid) {
        currentPlayer = uuid;
    }

    public static void addStyle(UUID uuid, RopeStyles.RopeStyle style) {
        selectedStyles.put(uuid, style);

        System.out.println("Set " + uuid + "style to " + style.asString());
    }

    public static RopeStyles.RopeStyle getStyle(UUID uuid) {
        if (selectedStyles.containsKey(uuid)) {
            System.out.println("successfully got style for uuid" + uuid);
            return selectedStyles.get(uuid);
        }
        System.out.println("defaulting to normal rope style");
        return new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
    }

}
