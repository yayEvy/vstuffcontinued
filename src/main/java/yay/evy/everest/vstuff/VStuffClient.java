package yay.evy.everest.vstuff;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import yay.evy.everest.vstuff.index.VStuffEntities;
import yay.evy.everest.vstuff.index.VStuffRenderTypes;
import yay.evy.everest.vstuff.infrastructure.ponder.VStuffPonders;

public class VStuffClient {

    public static void initVStuffClient(IEventBus modEventBus) {
        modEventBus.addListener(VStuffClient::initialize);
    }

    public static void initialize(final FMLClientSetupEvent event) {

        event.enqueueWork(() -> EntityRenderers.register(
                VStuffEntities.ROPE_THROWER.get(),
                ThrownItemRenderer::new
        ));

        PonderIndex.addPlugin(new VStuffPonders());
        VStuffRenderTypes.register();

    }
}