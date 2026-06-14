package yay.evy.everest.vstuff.client.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static yay.evy.everest.vstuff.internal.utility.RopeRenderUtils.*;

public final class RopeKnotRenderer {

    private static final float KNOT_SIZE_MULTIPLIER = 1.5f;

    private RopeKnotRenderer() {}

    public static void renderKnot(VertexConsumer consumer, Matrix4f matrix,
                                  Vector3d center, int light,
                                  Vector3d direction, Vector3d up, Vector3d right) {

        double h = getRopeWidth() * KNOT_SIZE_MULTIPLIER * 0.5;

        Vector3d rScaled = new Vector3d(right).mul(h);
        Vector3d uScaled = new Vector3d(up).mul(h);
        Vector3d dScaled = new Vector3d(direction).mul(h);

        Vector3d ppp = new Vector3d(center).add(rScaled).add(uScaled).add(dScaled);
        Vector3d ppn = new Vector3d(center).add(rScaled).add(uScaled).sub(dScaled);
        Vector3d pnp = new Vector3d(center).add(rScaled).sub(uScaled).add(dScaled);
        Vector3d pnn = new Vector3d(center).add(rScaled).sub(uScaled).sub(dScaled);
        Vector3d npp = new Vector3d(center).sub(rScaled).add(uScaled).add(dScaled);
        Vector3d npn = new Vector3d(center).sub(rScaled).add(uScaled).sub(dScaled);
        Vector3d nnp = new Vector3d(center).sub(rScaled).sub(uScaled).add(dScaled);
        Vector3d nnn = new Vector3d(center).sub(rScaled).sub(uScaled).sub(dScaled);

        Vector3d localUp    = up;
        Vector3d localDown  = neg(up);
        Vector3d localRight = right;
        Vector3d localLeft  = neg(right);
        Vector3d localFront = direction;
        Vector3d localBack  = neg(direction);

        renderFace(consumer, matrix, npp, ppp, npn, ppn, 0f, 1f, light, light, localUp);
        renderFace(consumer, matrix, nnp, pnp, nnn, pnn, 0f, 1f, light, light, localDown);
        renderFace(consumer, matrix, pnp, ppp, pnn, ppn, 0f, 1f, light, light, localRight);
        renderFace(consumer, matrix, npp, nnp, npn, nnn, 0f, 1f, light, light, localLeft);
        renderFace(consumer, matrix, nnp, pnp, npp, ppp, 0f, 1f, light, light, localFront);
        renderFace(consumer, matrix, ppn, npn, pnn, nnn, 0f, 1f, light, light, localBack);
    }
}