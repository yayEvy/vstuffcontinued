package yay.evy.everest.vstuff.content.physgrabber;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PhysGrabberBeamRenderHandler {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PhysGrabberBeamRenderer.grabbedShip = PhysGrabberClientHandler.getGrabbedShip();
        PhysGrabberBeamRenderer.playerEyePos = mc.player.getEyePosition(event.getPartialTick());

        if (PhysGrabberBeamRenderer.grabbedShip != null) {
            PoseStack poseStack = event.getPoseStack();
            var bufferSource = mc.renderBuffers().bufferSource();

            PhysGrabberBeamRenderer.render(poseStack, bufferSource, event.getPartialTick());

            bufferSource.endBatch();
        }
    }
}


