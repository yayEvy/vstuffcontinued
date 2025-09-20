package yay.evy.everest.vstuff.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import yay.evy.everest.vstuff.blocks.ModBlocks;

public class VStuffClient {
    public static void registerRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ROTATIONAL_THRUSTER.get(), RenderType.translucent());
    }
}