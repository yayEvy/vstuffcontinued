package yay.evy.everest.vstuff.client.rope.renderers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static yay.evy.everest.vstuff.internal.utility.RopeRenderUtils.*;

public final class RopeKnotRenderer {

    private static final float KNOT_SIZE_MULTIPLIER = 1.5f;

    private RopeKnotRenderer() {}

    /**
     * Renders rope knots at endpoints for ropes, not perfect yet so is disabled via config by default.
     */
    public static void renderKnot(VertexConsumer consumer, Matrix4f matrix,
                                  Vector3d center, int light) {
        double h = getRopeWidth() * KNOT_SIZE_MULTIPLIER * 0.5;

        Vector3d ppp = new Vector3d(center.x + h, center.y + h, center.z + h);
        Vector3d ppn = new Vector3d(center.x + h, center.y + h, center.z - h);
        Vector3d pnp = new Vector3d(center.x + h, center.y - h, center.z + h);
        Vector3d pnn = new Vector3d(center.x + h, center.y - h, center.z - h);
        Vector3d npp = new Vector3d(center.x - h, center.y + h, center.z + h);
        Vector3d npn = new Vector3d(center.x - h, center.y + h, center.z - h);
        Vector3d nnp = new Vector3d(center.x - h, center.y - h, center.z + h);
        Vector3d nnn = new Vector3d(center.x - h, center.y - h, center.z - h);

        Vector3d UP    = new Vector3d( 0,  1,  0);
        Vector3d DOWN  = new Vector3d( 0, -1,  0);
        Vector3d RIGHT = new Vector3d( 1,  0,  0);
        Vector3d LEFT  = new Vector3d(-1,  0,  0);
        Vector3d FRONT = new Vector3d( 0,  0,  1);
        Vector3d BACK  = new Vector3d( 0,  0, -1);

        renderFace(consumer, matrix, npp, ppp, npn, ppn, 0f, 1f, light, light, UP);
        renderFace(consumer, matrix, nnp, pnp, nnn, pnn, 0f, 1f, light, light, DOWN);
        renderFace(consumer, matrix, pnp, ppp, pnn, ppn, 0f, 1f, light, light, RIGHT);
        renderFace(consumer, matrix, npp, nnp, npn, nnn, 0f, 1f, light, light, LEFT);
        renderFace(consumer, matrix, nnp, pnp, npp, ppp, 0f, 1f, light, light, FRONT);
        renderFace(consumer, matrix, ppn, npn, pnn, nnn, 0f, 1f, light, light, BACK);
    }
}