package yay.evy.everest.vstuff;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import yay.evy.everest.vstuff.client.RopeRendererTypes;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffPartialModels;
import yay.evy.everest.vstuff.infrastructure.ponder.VStuffPonders;

public class VStuffClient {

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(VStuffClient::clientInit);
    }

    private static void clientInit(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> EntityRenderers.register(
                VStuffEntities.ROPE_THROWER.get(),
                ThrownItemRenderer::new
        ));

        VStuffPartialModels.register();
        RopeRendererTypes.init();
        PonderIndex.addPlugin(new VStuffPonders());
    }
}