package dev.flarelog.vstuff;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import dev.flarelog.vstuff.index.VStuffPartialModels;
import dev.flarelog.vstuff.infrastructure.ponder.VStuffPonders;

public class VStuffClient {

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(VStuffClient::clientInit);
    }

    private static void clientInit(final FMLClientSetupEvent event) {
        VStuffPartialModels.register();
        PonderIndex.addPlugin(new VStuffPonders());
    }
}