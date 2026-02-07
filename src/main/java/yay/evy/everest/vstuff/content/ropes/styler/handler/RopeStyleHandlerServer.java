package yay.evy.everest.vstuff.content.ropes.styler.handler;

import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.internal.RopeStyleManager;

import java.util.HashMap;
import java.util.UUID;


public class RopeStyleHandlerServer {

    private static final HashMap<UUID, ResourceLocation> selectedStyles = new HashMap<>();


    public static void addStyle(UUID uuid, ResourceLocation style) {
        selectedStyles.put(uuid, style);
    }

    public static ResourceLocation getStyle(UUID uuid) {
        if (selectedStyles.containsKey(uuid)) {
            return selectedStyles.get(uuid);
        }
        return RopeStyleManager.defaultId;
    }

}
