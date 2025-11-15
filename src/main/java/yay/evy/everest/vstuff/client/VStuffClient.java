package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import yay.evy.everest.vstuff.index.VStuffPartialModels;
import yay.evy.everest.vstuff.index.VStuffPonders;

public class VStuffClient {
    public static void initialize() {
        VStuffPartialModels.init();

        PonderIndex.addPlugin(new VStuffPonders());

        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                PhysGrabberBeamRenderer.render(poseStack, bufferSource, event.getPartialTick());
                bufferSource.endBatch();
            }
        });
    }
}
