package yay.evy.everest.vstuff.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.internal.rendering.IRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.RopeRenderContext;
import yay.evy.everest.vstuff.index.VStuffRenderTypes;

import static yay.evy.everest.vstuff.internal.utility.RopeRenderUtils.*;

public class ChainRopeRenderer implements IRopeRenderer {

    private final ResourceLocation texture;

    public ChainRopeRenderer(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public void render(RopeRenderContext ctx, PoseStack pose, MultiBufferSource bufferSource, Vector3d[] curve, int[] light) {
        renderChainRope(pose, bufferSource.getBuffer(this.getRenderType()), curve, light);
    }

    @Override
    public RenderType getRenderType() {
        return VStuffRenderTypes.chainRenderer(this.texture);
    }

    private static void renderChainRope(PoseStack poseStack, VertexConsumer vertexConsumer,
                                        Vector3d[] curvePoints, int[] lightValues) {
        Matrix4f matrix = poseStack.last().pose();

        double totalLen = 0;
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            totalLen += curvePoints[i].distance(curvePoints[i + 1]);
            cumulativeDistances[i + 1] = totalLen;
        }

        double halfWidth = getRopeWidth() * 1.5;
        double linkLengthInBlocks = 1.0;
        double textureVScale = 1.2 / linkLengthInBlocks;

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            Vector3d pStart = curvePoints[i];
            Vector3d pEnd = curvePoints[i + 1];
            Vector3d segDir = new Vector3d(pEnd).sub(pStart).normalize();

            Vector3d up = new Vector3d();
            Vector3d right = right(segDir, up);

            Vector3d rScaled = new Vector3d(right).mul(halfWidth);
            Vector3d uScaled = new Vector3d(up).mul(halfWidth);

            float vStart = (float) (cumulativeDistances[i] * textureVScale);
            float vEnd = (float) (cumulativeDistances[i + 1] * textureVScale);

            renderFace(
                    vertexConsumer,
                    matrix,
                    v3dSubAdd(pStart, rScaled, uScaled),
                    v3dSubAdd(pStart, uScaled, rScaled),
                    v3dSubAdd(pEnd, rScaled, uScaled),
                    v3dSubAdd(pEnd, uScaled, rScaled),
                    vStart,
                    vEnd,
                    lightValues[i],
                    lightValues[i+1],
                    new Vector3d(right).add(up).normalize()
            );
            renderFace(
                    vertexConsumer,
                    matrix,
                    v3dAddAdd(pStart, rScaled, uScaled),
                    v3dSubSub(pStart, rScaled, uScaled),
                    v3dAddAdd(pEnd, rScaled, uScaled),
                    v3dSubSub(pEnd, rScaled, uScaled),
                    vStart,
                    vEnd,
                    lightValues[i],
                    lightValues[i+1],
                    new Vector3d(right).sub(up).normalize()
            );
        }
    }
}
