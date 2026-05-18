package yay.evy.everest.vstuff;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yay.evy.everest.vstuff.client.RopeRendererTypes;
import yay.evy.everest.vstuff.index.VStuffPartialModels;
import yay.evy.everest.vstuff.infrastructure.ponder.VStuffPonders;

public class VStuffClient {

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(VStuffClient::clientInit);
    }

    private static void clientInit(final FMLClientSetupEvent event) {
        VStuffPartialModels.register();
        RopeRendererTypes.init();
        PonderIndex.addPlugin(new VStuffPonders());
    }
}