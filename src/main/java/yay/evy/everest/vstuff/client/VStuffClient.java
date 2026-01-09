package yay.evy.everest.vstuff.client;

import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yay.evy.everest.vstuff.content.thrust.MechanicalThrusterVisualizer;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffMenus;
import yay.evy.everest.vstuff.index.VStuffPonders;
import yay.evy.everest.vstuff.util.CreativeRopeEditorScreen;
import net.minecraft.client.gui.screens.MenuScreens;

public class VStuffClient {

    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            PonderIndex.addPlugin(new VStuffPonders());

            VisualizerRegistry.setVisualizer(
                    VStuffBlockEntities.MECHANICAL_THRUSTER_BE.get(),
                    new MechanicalThrusterVisualizer()
            );

            MenuScreens.register(
                    VStuffMenus.CREATIVE_ROPE_EDITOR.get(),
                    CreativeRopeEditorScreen::new
            );
        });
    }
}
