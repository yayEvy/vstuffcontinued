package yay.evy.everest.vstuff.internal.utility;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfig;

public final class RopeRenderUtils {
    public static final int    ROPE_CURVE_SEGMENTS  = 32;
    public static final float  NORMAL_ROPE_V_SCALE  = 2.5f;
    public static final float  CHAIN_ROPE_V_SCALE   = 0.5f;
    public static final float  ROPE_SAG_FACTOR      = 1.02f;
    public static final float  WIND_STRENGTH        = 0.02f;

    private static final BlockPos.MutableBlockPos SHARED_MUTABLE_POS = new BlockPos.MutableBlockPos();

    public static float getRopeWidth() {
        return VStuffConfig.ROPE_THICKNESS.get().floatValue();
    }

    public static Vector3d[] computeCurve(Vector3d startRelative, Vector3d endRelative,
                                          double sagAmount, float windOffset,
                                          float stableGameTime) {
        Vector3d[] curve = new Vector3d[ROPE_CURVE_SEGMENTS + 1];
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / ROPE_CURVE_SEGMENTS;
            double x = startRelative.x + (endRelative.x - startRelative.x) * t;
            double y = startRelative.y + (endRelative.y - startRelative.y) * t;
            double z = startRelative.z + (endRelative.z - startRelative.z) * t;

            double sagCurve  = Math.sin(t * Math.PI) * sagAmount;
            double windSwayX = Math.sin((stableGameTime * 0.7 + t * 2)) * windOffset * Math.max(sagAmount, 0.1) * 0.3;
            double windSwayZ = Math.cos((stableGameTime * 0.5 + t * 1.5)) * windOffset * Math.max(sagAmount, 0.1) * 0.15;

            curve[i] = new Vector3d(x + windSwayX, y - sagCurve, z + windSwayZ);
        }
        return curve;
    }

    public static int[] computeLighting(Vector3d[] curve, Level level,
                                        net.minecraft.world.phys.Vec3 cameraPos) {
        int[] light = new int[ROPE_CURVE_SEGMENTS + 1];
        boolean[] isSolid = new boolean[ROPE_CURVE_SEGMENTS + 1];
        BlockPos.MutableBlockPos mutPos = SHARED_MUTABLE_POS;

        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            mutPos.set(
                    curve[i].x + cameraPos.x,
                    curve[i].y + cameraPos.y,
                    curve[i].z + cameraPos.z
            );
            BlockState state = level.getBlockState(mutPos);
            light[i] = net.minecraft.client.renderer.LevelRenderer.getLightColor(level, mutPos);
            if (state.getLightBlock(level, mutPos) > 0 && state.isCollisionShapeFullBlock(level, mutPos)) {
                isSolid[i] = true;
            }
        }

        int lastValid = light[0];
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            if (isSolid[i]) light[i] = lastValid;
            else lastValid = light[i];
        }

        lastValid = light[ROPE_CURVE_SEGMENTS];
        for (int i = ROPE_CURVE_SEGMENTS; i >= 0; i--) {
            if (isSolid[i]) {
                int block1 = LightTexture.block(light[i]);
                int sky1   = LightTexture.sky(light[i]);
                int block2 = LightTexture.block(lastValid);
                int sky2   = LightTexture.sky(lastValid);
                light[i]   = LightTexture.pack(Math.max(block1, block2), Math.max(sky1, sky2));
            } else {
                lastValid = light[i];
            }
        }

        return light;
    }

    public static double computeSag(double actualLength, double maxLength) {
        double stretchFactor = Math.min(actualLength / Math.max(maxLength, 0.1), 1.0);
        return ROPE_SAG_FACTOR * (1.0 - stretchFactor * stretchFactor) * actualLength * 0.35;
    }

    public static float computeWindOffset(float stableGameTime) {
        return (float) (Math.sin(stableGameTime * 0.8) * 0.3
                + Math.sin(stableGameTime * 1.3) * 0.2) * WIND_STRENGTH;
    }

    public static Vector3d right(Vector3d direction, Vector3d upOut) {
        Vector3d worldUp = new Vector3d(0, 1, 0);
        Vector3d right = new Vector3d();

        if (Math.abs(direction.dot(worldUp)) > 0.9) {
            right.set(1, 0, 0);
        } else {
            direction.cross(worldUp, right).normalize();
        }

        right.cross(direction, upOut).normalize();
        return right;
    }

    public static Vector3d neg(Vector3d v) {
        return new Vector3d(v).mul(-1);
    }

    public static Vector3d v3dAddAdd(Vector3d v, Vector3d a, Vector3d b) {
        return new Vector3d(v).add(a).add(b);
    }

    public static Vector3d v3dSubAdd(Vector3d v, Vector3d sub, Vector3d add) {
        return new Vector3d(v).sub(sub).add(add);
    }

    public static Vector3d v3dSubSub(Vector3d v, Vector3d a, Vector3d b) {
        return new Vector3d(v).sub(a).sub(b);
    }

    public static void renderFace(VertexConsumer consumer, Matrix4f matrix,
                                  Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4,
                                  float vStart, float vEnd,
                                  int lightStart, int lightEnd,
                                  Vector3d normal) {
        addRopeVertex(consumer, matrix, v1, 0.0f, vStart, lightStart, normal);
        addRopeVertex(consumer, matrix, v2, 1.0f, vStart, lightStart, normal);
        addRopeVertex(consumer, matrix, v3, 0.0f, vEnd,   lightEnd,   normal);
        addRopeVertex(consumer, matrix, v2, 1.0f, vStart, lightStart, normal);
        addRopeVertex(consumer, matrix, v4, 1.0f, vEnd,   lightEnd,   normal);
        addRopeVertex(consumer, matrix, v3, 0.0f, vEnd,   lightEnd,   normal);
    }

    public static void addRopeVertex(VertexConsumer consumer, Matrix4f matrix,
                                     Vector3d pos, float u, float v,
                                     int light, Vector3d normal) {
        addRopeVertex(consumer, matrix, pos, u, v, light, normal, 255);
    }

    public static void addRopeVertex(VertexConsumer consumer, Matrix4f matrix,
                                     Vector3d pos, float u, float v,
                                     int light, Vector3d normal, int alpha) {
        float clampedU = Math.max(0.0f, Math.min(1.0f, u));
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, alpha)
                .uv(clampedU, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }
}
