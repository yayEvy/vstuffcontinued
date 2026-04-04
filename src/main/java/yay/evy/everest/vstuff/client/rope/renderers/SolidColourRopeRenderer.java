package yay.evy.everest.vstuff.client.rope.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.client.rope.IRopeRenderer;
import yay.evy.everest.vstuff.client.rope.RopeRenderContext;
import yay.evy.everest.vstuff.index.VStuffRenderTypes;

import static yay.evy.everest.vstuff.internal.utility.RopeRenderUtils.*;

public class SolidColourRopeRenderer implements IRopeRenderer {

    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");

    private final int r;
    private final int g;
    private final int b;

    public SolidColourRopeRenderer(int argb) {
        this.r = (argb >> 16) & 0xFF;
        this.g = (argb >> 8)  & 0xFF;
        this.b =  argb        & 0xFF;
    }

    @Override
    public RenderType getRenderType() {
        return VStuffRenderTypes.ropeRenderer(WHITE);
    }

    @Override
    public void render(RopeRenderContext ctx, PoseStack pose, MultiBufferSource bufferSource,
                       Vector3d[] curve, int[] light) {
        VertexConsumer consumer = bufferSource.getBuffer(getRenderType());
        renderColouredRope(pose, consumer, curve, light, ctx.startRelative(), ctx.endRelative());
    }

    private void renderColouredRope(PoseStack poseStack, VertexConsumer vertexConsumer,
                                    Vector3d[] curvePoints, int[] lightValues,
                                    Vector3d start, Vector3d end) {
        Matrix4f matrix = poseStack.last().pose();

        Vector3d overallDirection = new Vector3d(end).sub(start).normalize();
        Vector3d up = new Vector3d();
        Vector3d right = right(overallDirection, up);

        Vector3d[][] strips = new Vector3d[4][ROPE_CURVE_SEGMENTS + 1];

        double halfWidth  = getRopeWidth() * 0.6;
        Vector3d rightScaled = new Vector3d(right).mul(halfWidth);
        Vector3d upScaled    = new Vector3d(up).mul(halfWidth);

        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            Vector3d center = curvePoints[i];
            strips[0][i] = v3dAddAdd(center, rightScaled, upScaled);
            strips[1][i] = v3dSubAdd(center, rightScaled, upScaled);
            strips[2][i] = v3dSubAdd(center, upScaled,    rightScaled);
            strips[3][i] = v3dSubSub(center, rightScaled, upScaled);
        }

        renderFaceWithGapFilling(vertexConsumer, matrix, strips[1], strips[0], up,         curvePoints, lightValues);
        renderFaceWithGapFilling(vertexConsumer, matrix, strips[0], strips[2], right,      curvePoints, lightValues);
        renderFaceWithGapFilling(vertexConsumer, matrix, strips[3], strips[1], neg(right), curvePoints, lightValues);
        renderFaceWithGapFilling(vertexConsumer, matrix, strips[2], strips[3], neg(up),    curvePoints, lightValues);
    }

    private void renderFaceWithGapFilling(VertexConsumer consumer, Matrix4f matrix,
                                          Vector3d[] strip1, Vector3d[] strip2, Vector3d normal,
                                          Vector3d[] curvePoints, int[] lightValues) {
        double[] dist = new double[ROPE_CURVE_SEGMENTS + 1];
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++)
            dist[i + 1] = dist[i] + curvePoints[i].distance(curvePoints[i + 1]);

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            float vStart = (float) (dist[i]     * NORMAL_ROPE_V_SCALE);
            float vEnd   = (float) (dist[i + 1] * NORMAL_ROPE_V_SCALE);
            int ls = lightValues[i];
            int le = lightValues[i + 1];

            Vector3d v1 = strip1[i],     v2 = strip2[i];
            Vector3d v3 = strip1[i + 1], v4 = strip2[i + 1];

            renderFace(consumer, matrix, v1, v2, v3, v4, vStart, vEnd, ls, le, normal);

            // Gap-filling triangles — same as TexturedRopeRenderer but semi-transparent
            Vector3d c1 = new Vector3d(v1).add(v2).mul(0.5);
            Vector3d c2 = new Vector3d(v3).add(v4).mul(0.5);

            vertex(consumer, matrix, c1, 0.5f, vStart, ls, normal, 128);
            vertex(consumer, matrix, v2, 1.0f, vStart, ls, normal, 128);
            vertex(consumer, matrix, c2, 0.5f, vEnd,   le, normal, 128);

            vertex(consumer, matrix, v1, 0.0f, vStart, ls, normal, 128);
            vertex(consumer, matrix, c1, 0.5f, vStart, ls, normal, 128);
            vertex(consumer, matrix, c2, 0.5f, vEnd,   le, normal, 128);
        }
    }

    private void renderFace(VertexConsumer consumer, Matrix4f matrix,
                            Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4,
                            float vStart, float vEnd, int lightStart, int lightEnd,
                            Vector3d normal) {
        vertex(consumer, matrix, v1, 0.0f, vStart, lightStart, normal, 255);
        vertex(consumer, matrix, v2, 1.0f, vStart, lightStart, normal, 255);
        vertex(consumer, matrix, v3, 0.0f, vEnd,   lightEnd,   normal, 255);
        vertex(consumer, matrix, v2, 1.0f, vStart, lightStart, normal, 255);
        vertex(consumer, matrix, v4, 1.0f, vEnd,   lightEnd,   normal, 255);
        vertex(consumer, matrix, v3, 0.0f, vEnd,   lightEnd,   normal, 255);
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix,
                        Vector3d pos, float u, float v, int light, Vector3d normal, int alpha) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(r, g, b, alpha)
                .uv(u, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }
}