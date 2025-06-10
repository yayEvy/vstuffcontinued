package yay.evy.everest.vstuff.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yay.evy.everest.vstuff.block.ModBlockEntities;
import yay.evy.everest.vstuff.ropes.RopePulleyRenderer;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register the rope pulley renderer
            BlockEntityRenderers.register(ModBlockEntities.ROPE_PULLEY.get(), RopePulleyRenderer::new);
        });
    }
}
