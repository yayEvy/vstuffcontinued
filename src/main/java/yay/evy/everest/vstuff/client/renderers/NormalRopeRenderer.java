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
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;

import static yay.evy.everest.vstuff.internal.utility.RopeRenderUtils.*;

public class NormalRopeRenderer implements IRopeRenderer {

    private final ResourceLocation texture;

    public NormalRopeRenderer(ResourceLocation texture){
        this.texture = texture;
    }

    @Override
    public void render(RopeRenderContext ctx, PoseStack pose, MultiBufferSource bufferSource, Vector3d[] curve, int[] light) {
        renderNormalRope(pose, bufferSource.getBuffer(this.getRenderType()), curve, light,
                ctx.startRelative(), ctx.endRelative(), ctx.prevStartRelative(), ctx.prevEndRelative());
    }

    @Override
    public RenderType getRenderType() {
        return VStuffRenderTypes.ropeRenderer(this.texture);
    }

    private void renderNormalRope(PoseStack poseStack, VertexConsumer vertexConsumer,
                                  Vector3d[] curvePoints, int[] lightValues, Vector3d start, Vector3d end,
                                  Vector3d prevStart, Vector3d prevEnd) {
        Matrix4f matrix = poseStack.last().pose();

        Vector3d overallDirection = new Vector3d(end).sub(start).normalize();
        Vector3d up = new Vector3d();
        Vector3d right = right(overallDirection, up);

        Vector3d prevOverallDirection = new Vector3d(prevEnd).sub(prevStart).normalize();
        Vector3d prevUp = new Vector3d();
        Vector3d prevRight = right(prevOverallDirection, prevUp);



        if (prevRight.dot(right) < 0) right.mul(-1);
        right.lerp(prevRight, 1.0f - ORIENTATION_SMOOTH_FACTOR).normalize();
        up = new Vector3d();
        right.cross(overallDirection, up).normalize();


        Vector3d[][] strips = new Vector3d[4][ROPE_CURVE_SEGMENTS + 1]; // top right, top left, bottom right, bottom left

        double halfWidth = getRopeWidth() * 0.6f; // ah yes half
        Vector3d rightScaled = new Vector3d(right).mul(halfWidth);
        Vector3d upScaled = new Vector3d(up).mul(halfWidth);

        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            Vector3d center = curvePoints[i];

            strips[0][i] = v3dAddAdd(center, rightScaled, upScaled);
            strips[1][i] = v3dSubAdd(center, rightScaled, upScaled);
            strips[2][i] = v3dSubAdd(center, upScaled, rightScaled);
            strips[3][i] = v3dSubSub(center, rightScaled, upScaled);
        }

        renderRopeFaceWithGapFilling(vertexConsumer, matrix, strips[1], strips[0], up, curvePoints, lightValues);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, strips[0], strips[2], right, curvePoints, lightValues);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, strips[3], strips[1], neg(right), curvePoints, lightValues);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, strips[2], strips[3], neg(up), curvePoints, lightValues);

        // todo make it work better with 99% of blocks, "im just gonna make rope knots a config thats off by default until we find a better way to implement it for like 99% of blocks, but ill keep it in cause it does look sweet..."
        // so for now it's a config, being off by default
        if (VStuffConfigs.client().ropeKnots.get()) {
            int startLight = lightValues[0];
            int endLight   = lightValues[ROPE_CURVE_SEGMENTS];
            RopeKnotRenderer.renderKnot(vertexConsumer, matrix, curvePoints[0], startLight);
            RopeKnotRenderer.renderKnot(vertexConsumer, matrix, curvePoints[ROPE_CURVE_SEGMENTS], endLight);
        }
    }

    private static void renderRopeFaceWithGapFilling(VertexConsumer vertexConsumer, Matrix4f matrix,
                                                     Vector3d[] strip1, Vector3d[] strip2, Vector3d normal,
                                                     Vector3d[] curvePoints, int[] lightValues) {
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            double segmentLength = curvePoints[i].distance(curvePoints[i + 1]);
            cumulativeDistances[i + 1] = cumulativeDistances[i] + segmentLength;
        }

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            float vStart = (float) (cumulativeDistances[i] * NORMAL_ROPE_V_SCALE);
            float vEnd = (float) (cumulativeDistances[i + 1] * NORMAL_ROPE_V_SCALE);

            int lightStart = lightValues[i];
            int lightEnd = lightValues[i + 1];

            Vector3d v1 = strip1[i];
            Vector3d v2 = strip2[i];
            Vector3d v3 = strip1[i + 1];
            Vector3d v4 = strip2[i + 1];

            renderFace(vertexConsumer, matrix, v1, v2, v3, v4, vStart, vEnd, lightStart, lightEnd, normal);

            Vector3d center1 = new Vector3d(v1).add(v2).mul(0.5);
            Vector3d center2 = new Vector3d(v3).add(v4).mul(0.5);

            addRopeVertex(vertexConsumer, matrix, center1, 0.5f, vStart, lightStart, normal, 128);
            addRopeVertex(vertexConsumer, matrix, v2, 1.0f, vStart, lightStart, normal, 128);
            addRopeVertex(vertexConsumer, matrix, center2, 0.5f, vEnd, lightEnd, normal, 128);

            addRopeVertex(vertexConsumer, matrix, v1, 0.0f, vStart, lightStart, normal, 128);
            addRopeVertex(vertexConsumer, matrix, center1, 0.5f, vStart, lightStart, normal, 128);
            addRopeVertex(vertexConsumer, matrix, center2, 0.5f, vEnd, lightEnd, normal, 128);
        }
    }
}
