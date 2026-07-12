package dev.flarelog.vstuff.infrastructure.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.ponder.scenes.PhysPulleyScenes;
import dev.flarelog.vstuff.infrastructure.ponder.scenes.MechanicalThrusterScenes;
import dev.flarelog.vstuff.infrastructure.ponder.scenes.ReactionWheelScenes;
import dev.flarelog.vstuff.index.VStuffBlocks;

public class VStuffPonders implements PonderPlugin {


    @Override
    public String getModId() {
        return VStuff.MOD_ID;
    }


    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addStoryBoard(VStuffBlocks.PHYS_PULLEY, "phys_pulley", PhysPulleyScenes::physPulley);
        HELPER.addStoryBoard(VStuffBlocks.MECHANICAL_THRUSTER, "mechanical_thruster", MechanicalThrusterScenes::mechanicalThruster);
        HELPER.addStoryBoard(VStuffBlocks.REACTION_WHEEL, "reaction_wheel", ReactionWheelScenes::reactionWheel);
    }


}
