package yay.evy.everest.vstuff.content.ropestyler.handler;

import yay.evy.everest.vstuff.internal.RopeStyles;

import java.util.HashMap;
import java.util.UUID;


public class RopeStyleHandlerServer {

    private static final HashMap<UUID, RopeStyles.RopeStyle> selectedStyles = new HashMap<>();


    public static void addStyle(UUID uuid, RopeStyles.RopeStyle style) {
        selectedStyles.put(uuid, style);

        //VStuff.LOGGER.info("Set UUID {} style to {}", uuid, style.asString());
    }

    public static RopeStyles.RopeStyle getStyle(UUID uuid) {
        if (selectedStyles.containsKey(uuid)) {
          //  VStuff.LOGGER.info("Successfully got style for UUID {}", uuid);
            return selectedStyles.get(uuid);
        }
       // VStuff.LOGGER.info("Could not get style for UUID {}, defaulting to normal rope style", uuid);
        return new RopeStyles.RopeStyle("normal", RopeStyles.PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
    }

}
