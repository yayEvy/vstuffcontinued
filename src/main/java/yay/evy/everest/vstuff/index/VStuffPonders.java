package yay.evy.everest.vstuff.index;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.ponder.PhysPulleyPonder;
import yay.evy.everest.vstuff.content.ponder.RotationalThrusterPonder;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;

public class VStuffPonders implements PonderPlugin {


    @Override
    public String getModId() {
        return VStuff.MOD_ID;
    }


    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        register(helper);
    }


    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addStoryBoard(VStuffBlocks.PHYS_PULLEY, "usage", PhysPulleyPonder::physPulley);
        HELPER.addStoryBoard(VStuffBlocks.ROTATIONAL_THRUSTER, "usage", RotationalThrusterPonder::rotationalThruster);


    }

}
