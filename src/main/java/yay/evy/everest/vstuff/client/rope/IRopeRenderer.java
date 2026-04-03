package yay.evy.everest.vstuff.client.rope;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3d;

public interface IRopeRenderer {
    void render(RopeRenderContext ctx, PoseStack pose, MultiBufferSource bufferSource, Vector3d[] curve, int[] light);

    RenderType getRenderType();
}
