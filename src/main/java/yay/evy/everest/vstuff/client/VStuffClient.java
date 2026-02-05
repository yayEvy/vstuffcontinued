package yay.evy.everest.vstuff.client;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yay.evy.everest.vstuff.index.VStuffPonders;

public class VStuffClient {
    public static void initialize(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new VStuffPonders());
    }

}