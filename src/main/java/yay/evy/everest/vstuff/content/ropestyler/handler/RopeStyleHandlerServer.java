package yay.evy.everest.vstuff.content.ropestyler.handler;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.HashMap;
import java.util.UUID;


public class RopeStyleHandlerServer {

    private static final HashMap<UUID, RopeStyles.RopeStyle> selectedStyles = new HashMap<>();

    public static void addStyle(UUID uuid, RopeStyles.RopeStyle style) {
        selectedStyles.put(uuid, style);

        VStuff.LOGGER.info("Set {} style to {}", uuid, style.asString());
    }

    public static RopeStyles.RopeStyle getStyle(UUID uuid) {
        if (selectedStyles.containsKey(uuid)) {
            VStuff.LOGGER.info("successfully got style for uuid {}", uuid);
            return selectedStyles.get(uuid);
        }
        VStuff.LOGGER.info("Could not get style for {}, defaulting to normal rope style", uuid);
        return new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
    }

}
