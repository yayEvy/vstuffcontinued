package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffPartialModels;
import yay.evy.everest.vstuff.index.VStuffPonders;
import net.createmod.ponder.foundation.PonderIndex;

public class VStuffClient {
    public static void initialize() {
        VStuffPartialModels.init();
        PonderIndex.addPlugin(new VStuffPonders());




    }
}
