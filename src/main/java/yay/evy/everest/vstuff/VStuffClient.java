package yay.evy.everest.vstuff;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import yay.evy.everest.vstuff.client.rope.VStuffRopeRendererTypes;
import yay.evy.everest.vstuff.index.VStuffPartialModels;
import yay.evy.everest.vstuff.infrastructure.ponder.VStuffPonders;

public class VStuffClient {

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(VStuffClient::clientInit);
        modEventBus.addListener(VStuffClient::onRegister);
    }

    public static void onRegister(RegisterEvent event) {
        VStuffRopeRendererTypes.init();
    }

    private static void clientInit(final FMLClientSetupEvent event) {
        VStuffPartialModels.register();
        PonderIndex.addPlugin(new VStuffPonders());
    }
}