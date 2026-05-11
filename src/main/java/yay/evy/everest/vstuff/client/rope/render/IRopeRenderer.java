package yay.evy.everest.vstuff.client.rope.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;

@OnlyIn(Dist.CLIENT)
public interface IRopeRenderer {
    void render(RopeRenderContext ctx, PoseStack pose, MultiBufferSource bufferSource, Vector3d[] curve, int[] light);

    RenderType getRenderType();
}
