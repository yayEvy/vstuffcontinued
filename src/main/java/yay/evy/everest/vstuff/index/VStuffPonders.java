package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ponder.PhysPulleyPonder;

public class VStuffPonders {

    private static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(VStuff.MOD_ID);

    public static void register() {
        HELPER.forComponents(VStuffBlocks.PHYS_PULLEY)
                .addStoryBoard("phys_ponder", PhysPulleyPonder::physPulley);
    }


}
