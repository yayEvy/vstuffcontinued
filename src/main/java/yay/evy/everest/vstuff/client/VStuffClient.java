package yay.evy.everest.vstuff.client;

import net.createmod.ponder.foundation.PonderIndex;
import yay.evy.everest.vstuff.index.VStuffPonders;

public class VStuffClient {


    public static void initialize() {
        PonderIndex.addPlugin(new VStuffPonders());


    }

}
