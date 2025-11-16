package yay.evy.everest.vstuff.client;


import yay.evy.everest.vstuff.index.VStuffPonders;
import net.createmod.ponder.foundation.PonderIndex;

public class VStuffClient {
    public static void initialize() {
        PonderIndex.addPlugin(new VStuffPonders());




    }
}
