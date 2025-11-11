package yay.evy.everest.vstuff.index;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ponder.PhysPulleyPonder;
import yay.evy.everest.vstuff.content.ponder.RotationalThrusterPonder;
import yay.evy.everest.vstuff.index.VStuffBlocks;

public class VStuffPonders implements PonderPlugin {

    @Override
    public String getModId() {
        return VStuff.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Use getId() to provide ResourceLocation
        helper.forComponents(VStuffBlocks.PHYS_PULLEY.getId())
                .addStoryBoard("phys_ponder", PhysPulleyPonder::physPulley);

        helper.forComponents(VStuffBlocks.ROTATIONAL_THRUSTER.getId())
                .addStoryBoard("thruster_ponder", RotationalThrusterPonder::rotationalThruster);
    }
}
