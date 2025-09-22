package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ponder.PhysPulleyPonder;
import yay.evy.everest.vstuff.content.ponder.RotationalThrusterPonder;

public class VStuffPonders {

    private static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(VStuff.MOD_ID);

    public static void register() {
        HELPER.forComponents(VStuffBlocks.PHYS_PULLEY)
                .addStoryBoard("phys_ponder", PhysPulleyPonder::physPulley);
        HELPER.forComponents(VStuffBlocks.ROTATIONAL_THRUSTER)
                .addStoryBoard("thruster_ponder", RotationalThrusterPonder::rotationalThruster);
    }


}
