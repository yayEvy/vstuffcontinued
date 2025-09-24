package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.rendering.RopeRendererType;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.util.RopeStyles;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RopeRendererClient {
    private static final float ROPE_WIDTH = 0.28f;
    private static final int ROPE_CURVE_SEGMENTS = 32;
    private static final float ROPE_SAG_FACTOR = 1.02f;
    private static final double MAX_RENDER_DISTANCE = 300.0;
    private static final float WIND_STRENGTH = 0.02f;
    private static final Map<Integer, RopePositionCache> positionCache = new ConcurrentHashMap<>();

    private static class RopePositionCache {
        Vector3d prevStartPos = new Vector3d();
        Vector3d prevEndPos = new Vector3d();
        Vector3d currentStartPos = new Vector3d();
        Vector3d currentEndPos = new Vector3d();
        boolean initialized = false;

        private static final double TELEPORT_THRESHOLD = 5.0;

        public void updatePositions(Vector3d newStart, Vector3d newEnd) {
            if (!initialized) {
                prevStartPos.set(newStart);
                prevEndPos.set(newEnd);
                currentStartPos.set(newStart);
                currentEndPos.set(newEnd);
                initialized = true;
                return;
            }

            double startJump = currentStartPos.distance(newStart);
            double endJump = currentEndPos.distance(newEnd);

            if (startJump > TELEPORT_THRESHOLD || endJump > TELEPORT_THRESHOLD) {
                prevStartPos.set(newStart);
                prevEndPos.set(newEnd);
                currentStartPos.set(newStart);
                currentEndPos.set(newEnd);
            } else {
                prevStartPos.set(currentStartPos);
                prevEndPos.set(currentEndPos);

                currentStartPos.set(newStart);
                currentEndPos.set(newEnd);
            }
        }

        public Vector3d getInterpolatedStartPos(float partialTick) {
            if (!initialized) return new Vector3d(currentStartPos);
            return new Vector3d(prevStartPos).lerp(currentStartPos, partialTick);
        }

        public Vector3d getInterpolatedEndPos(float partialTick) {
            if (!initialized) return new Vector3d(prevEndPos).lerp(currentEndPos, partialTick);
            return new Vector3d(prevEndPos).lerp(currentEndPos, partialTick);
        }
    }
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (event.getPartialTick() < 0.1f) {
            cleanupPositionCache();
        }

        try {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level == null) return;

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            Vec3 cameraPos = event.getCamera().getPosition();
            float partialTick = event.getPartialTick();

            RenderType renderType = RopeRendererType.ropeRenderer(RopeStyles.getRopeStyle("normal"));

            Map<Integer, ClientConstraintTracker.ClientRopeData> constraints = ClientConstraintTracker.getClientConstraints();
            Map<Integer, ConstraintTracker.RopeConstraintData> serverConstraints = ConstraintTracker.getActiveConstraints();

            boolean renderedAny = false;

            if (constraints.isEmpty()) {
                for (Map.Entry<Integer, ConstraintTracker.RopeConstraintData> entry : serverConstraints.entrySet()) {
                    try {
                        renderServerRope(poseStack, bufferSource, entry.getKey(), entry.getValue(), level, cameraPos, partialTick, entry.getValue().style);
                        renderedAny = true;
                    } catch (Exception e) {
                        System.err.println("Error rendering server rope: " + e.getMessage());
                    }
                }
            } else {
                for (Map.Entry<Integer, ClientConstraintTracker.ClientRopeData> entry : constraints.entrySet()) {
                    try {
                        renderClientRope(poseStack, bufferSource, entry.getKey(), entry.getValue(), level, cameraPos, partialTick, entry.getValue().style);
                        renderedAny = true;
                    } catch (Exception e) {
                        System.err.println("Error rendering client rope: " + e.getMessage());
                    }
                }
            }

            if (renderedAny) {
                bufferSource.endBatch(renderType);
            }
        } catch (Exception e) {
            System.err.println("Error in rope rendering: " + e.getMessage());
        }
    }

    private static void cleanupPositionCache() {
        Map<Integer, ClientConstraintTracker.ClientRopeData> constraints = ClientConstraintTracker.getClientConstraints();
        Map<Integer, ConstraintTracker.RopeConstraintData> serverConstraints = ConstraintTracker.getActiveConstraints();

        positionCache.entrySet().removeIf(entry ->
                !constraints.containsKey(entry.getKey()) &&
                        !serverConstraints.containsKey(entry.getKey())
        );
    }
    private static void renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ClientConstraintTracker.ClientRopeData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick, RopeStyles.RopeStyle style) {
        if (!level.isClientSide) {
            System.err.println("Warning: Client renderer called on server side!");
            return;
        }

        Vector3d startPos = ropeData.getWorldPosA(level, partialTick);
        Vector3d endPos = ropeData.getWorldPosB(level, partialTick);

        if (startPos != null && endPos != null) {
            double actualRopeLength = startPos.distance(endPos);
            double maxRopeLength = ropeData.maxLength;

            renderRope(poseStack, bufferSource, startPos, endPos,
                    actualRopeLength, maxRopeLength, cameraPos, partialTick, level, style);
        }
    }


    private static void renderServerRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ConstraintTracker.RopeConstraintData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick, RopeStyles.RopeStyle style) {
        try {
            Vector3d startPos = ropeData.getWorldPosA((ServerLevel) level, 0.0f);
            Vector3d endPos = ropeData.getWorldPosB((ServerLevel) level, 0.0f);

            if (startPos != null && endPos != null) {
                RopePositionCache cache = positionCache.computeIfAbsent(constraintId, k -> new RopePositionCache());

                if (partialTick < 0.1f) {
                    cache.updatePositions(startPos, endPos);
                }

                Vector3d renderStart = cache.getInterpolatedStartPos(partialTick);
                Vector3d renderEnd = cache.getInterpolatedEndPos(partialTick);

                double actualRopeLength = renderStart.distance(renderEnd);
                double maxRopeLength = ropeData.maxLength;

                renderRope(poseStack, bufferSource, renderStart, renderEnd,
                        actualRopeLength, maxRopeLength, cameraPos, partialTick, level, style);
            }
        } catch (Exception e) {
            System.err.println("Error in renderServerRope: " + e.getMessage());
        }
    }

    private static void renderRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Vector3d startPos, Vector3d endPos, double actualRopeLength,
                                   double maxRopeLength, Vec3 cameraPos, float partialTick, Level level, RopeStyles.RopeStyle style) {
        Vec3 start = new Vec3(startPos.x - cameraPos.x, startPos.y - cameraPos.y, startPos.z - cameraPos.z);
        Vec3 end = new Vec3(endPos.x - cameraPos.x, endPos.y - cameraPos.y, endPos.z - cameraPos.z);

        double distanceToCamera = Math.min(start.length(), end.length());
        if (distanceToCamera > MAX_RENDER_DISTANCE) {
            return;
        }

        double currentDistance = start.distanceTo(end);
        if (currentDistance < 0.1) {
            return;
        }

        RenderType renderType;
        poseStack.pushPose();
        switch (style.getRenderStyle()) {
            case CHAIN:
                renderChainRope(poseStack, bufferSource, start, end,
                        actualRopeLength, maxRopeLength, partialTick, level, cameraPos, style, style.getTexture());
                break;

            default:
                renderType = RopeRendererType.ropeRenderer(style.getTexture());
                VertexConsumer vertexConsumerNormal = bufferSource.getBuffer(renderType);
                renderNormalRope(poseStack, vertexConsumerNormal, start, end,
                        actualRopeLength, maxRopeLength, partialTick, level, cameraPos);
                break;
        }


        poseStack.popPose();
    }

    private static void renderNormalRope(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         Vec3 start, Vec3 end, double actualRopeLength,
                                         double maxRopeLength, float partialTick, Level level, Vec3 cameraPos) {
        Matrix4f matrix = poseStack.last().pose();
        Vec3 direction = end.subtract(start);
        double currentDistance = direction.length();
        if (currentDistance < 0.01) return;

        double stretchFactor = Math.min(currentDistance / Math.max(maxRopeLength, 0.1), 1.0);
        double sagAmount = ROPE_SAG_FACTOR * (1.0 - stretchFactor * stretchFactor) * currentDistance * 0.35;

        long currentTimeNanos = System.nanoTime();
        float gameTime = (currentTimeNanos % 100_000_000_000L) / 1_000_000_000.0f;
        float windOffset = (float) (Math.sin(gameTime * 0.8) * 0.3 + Math.sin(gameTime * 1.3) * 0.2) * WIND_STRENGTH;

        Vec3[] curvePoints = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / ROPE_CURVE_SEGMENTS;
            curvePoints[i] = calculateCatenaryPosition(start, end, t, sagAmount, windOffset, gameTime);
        }

        double totalCurveLength = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            totalCurveLength += curvePoints[i].distanceTo(curvePoints[i + 1]);
        }

        Vec3 overallDirection = end.subtract(start).normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right;
        if (Math.abs(overallDirection.dot(worldUp)) > 0.9) {
            right = new Vec3(1, 0, 0);
        } else {
            right = overallDirection.cross(worldUp).normalize();
        }
        Vec3 up = right.cross(overallDirection).normalize();

        Vec3[] topRightStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        Vec3[] topLeftStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        Vec3[] bottomLeftStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        Vec3[] bottomRightStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];

        float halfWidth = ROPE_WIDTH * 0.6f;
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            Vec3 center = curvePoints[i];
            topRightStrip[i] = center.add(right.scale(halfWidth)).add(up.scale(halfWidth));
            topLeftStrip[i] = center.add(right.scale(-halfWidth)).add(up.scale(halfWidth));
            bottomLeftStrip[i] = center.add(right.scale(-halfWidth)).add(up.scale(-halfWidth));
            bottomRightStrip[i] = center.add(right.scale(halfWidth)).add(up.scale(-halfWidth));
        }

        double textureScale = 0.28 / ROPE_WIDTH;

        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topLeftStrip, topRightStrip, up, curvePoints, totalCurveLength, level, cameraPos, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topRightStrip, bottomRightStrip, right, curvePoints, totalCurveLength, level, cameraPos, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, bottomRightStrip, bottomLeftStrip, up.scale(-1), curvePoints, totalCurveLength, level, cameraPos, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, bottomLeftStrip, topLeftStrip, right.scale(-1), curvePoints, totalCurveLength, level, cameraPos, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topLeftStrip, bottomRightStrip, right.add(up).normalize(), curvePoints, totalCurveLength, level, cameraPos, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topRightStrip, bottomLeftStrip, right.subtract(up).normalize(), curvePoints, totalCurveLength, level, cameraPos, textureScale);

    }



    private static void renderRopeFaceWithGapFilling(VertexConsumer vertexConsumer, Matrix4f matrix,
                                                     Vec3[] strip1, Vec3[] strip2, Vec3 normal,
                                                     Vec3[] curvePoints, double totalCurveLength,
                                                     Level level, Vec3 cameraPos, double textureScale) {
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            double segmentLength = curvePoints[i].distanceTo(curvePoints[i + 1]);
            cumulativeDistances[i + 1] = cumulativeDistances[i] + segmentLength;
        }

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            float vStart = (float) (cumulativeDistances[i] * textureScale);
            float vEnd = (float) (cumulativeDistances[i + 1] * textureScale);

            Vec3 p1 = strip1[i];
            Vec3 p2 = strip2[i];
            Vec3 p3 = strip2[i + 1];
            Vec3 p4 = strip1[i + 1];

            int light = calculateDynamicLighting(level, p1, p2, cameraPos);

            addRopeVertex(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p3, 1.0f, vEnd, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);

            Vec3 center1 = p1.add(p2).scale(0.5);
            Vec3 center2 = p3.add(p4).scale(0.5);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center1, 0.5f, vStart, light, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center2, 0.5f, vEnd, light, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center1, 0.5f, vStart, light, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center2, 0.5f, vEnd, light, normal, 128);
        }
    }

    private static void renderChainRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                        Vec3 start, Vec3 end, double actualRopeLength,
                                        double maxRopeLength, float partialTick, Level level, Vec3 cameraPos,
                                        RopeStyles.RopeStyle style, ResourceLocation chainTexture) {
        Matrix4f matrix = poseStack.last().pose();

        RenderType renderType = RopeRendererType.ropeRendererChainStyle(chainTexture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        double stretchFactor = Math.min(actualRopeLength / Math.max(maxRopeLength, 0.1), 1.0);
        double sagAmount = ROPE_SAG_FACTOR * (1.0 - stretchFactor * stretchFactor) * actualRopeLength * 0.35;

        long currentTimeNanos = System.nanoTime();
        float gameTime = (currentTimeNanos % 100_000_000_000L) / 1_000_000_000.0f;
        float windOffset = (float) (Math.sin(gameTime * 0.8) * 0.3 + Math.sin(gameTime * 1.3) * 0.2) * WIND_STRENGTH;

        Vec3[] curvePoints = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / ROPE_CURVE_SEGMENTS;
            curvePoints[i] = calculateCatenaryPosition(start, end, t, sagAmount, windOffset, gameTime);
        }

        double totalCurveLength = 0;
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            double segLength = curvePoints[i].distanceTo(curvePoints[i + 1]);
            totalCurveLength += segLength;
            cumulativeDistances[i + 1] = totalCurveLength;
        }

        double repeatInterval = 1;
        double textureScale = 1 / repeatInterval;

        Vec3 overallDirection = end.subtract(start).normalize();
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 right = Math.abs(overallDirection.dot(worldUp)) > 0.9
                ? new Vec3(1, 0, 0)
                : overallDirection.cross(worldUp).normalize();
        Vec3 up = right.cross(overallDirection).normalize();

        Vec3[] topRightStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        Vec3[] topLeftStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        Vec3[] bottomLeftStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        Vec3[] bottomRightStrip = new Vec3[ROPE_CURVE_SEGMENTS + 1];

        float halfWidth = ROPE_WIDTH * 5f;
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            Vec3 center = curvePoints[i];
            topRightStrip[i] = center.add(right.scale(halfWidth)).add(up.scale(halfWidth));
            topLeftStrip[i] = center.add(right.scale(-halfWidth)).add(up.scale(halfWidth));
            bottomLeftStrip[i] = center.add(right.scale(-halfWidth)).add(up.scale(-halfWidth));
            bottomRightStrip[i] = center.add(right.scale(halfWidth)).add(up.scale(-halfWidth));
        }

        renderRopeFaceWithRepeatingUVs(vertexConsumer, matrix, topLeftStrip, bottomRightStrip,
                right.add(up).normalize(), curvePoints, cumulativeDistances, textureScale, level, cameraPos);

        renderRopeFaceWithRepeatingUVs(vertexConsumer, matrix, topRightStrip, bottomLeftStrip,
                right.subtract(up).normalize(), curvePoints, cumulativeDistances, textureScale, level, cameraPos);
    }


    private static void renderRopeFaceWithRepeatingUVs(VertexConsumer vertexConsumer, Matrix4f matrix,
                                                       Vec3[] strip1, Vec3[] strip2, Vec3 normal,
                                                       Vec3[] curvePoints, double[] cumulativeDistances,
                                                       double textureScale, Level level, Vec3 cameraPos) {
        double linkLength = 1;
        double ropeLength = cumulativeDistances[ROPE_CURVE_SEGMENTS];
        double textureScalePerBlock = 0.5 / linkLength;

        double leftover = ropeLength % linkLength;
        double uvOffset = -(leftover * 0.5 * textureScalePerBlock);

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            float vStart = (float) (cumulativeDistances[i]     * textureScalePerBlock + uvOffset);
            float vEnd   = (float) (cumulativeDistances[i + 1] * textureScalePerBlock + uvOffset);

            Vec3 p1 = strip1[i];
            Vec3 p2 = strip2[i];
            Vec3 p3 = strip2[i + 1];
            Vec3 p4 = strip1[i + 1];

            int light = calculateDynamicLighting(level, p1, p2, cameraPos);

            addRopeVertex(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd,   light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p3, 1.0f, vEnd,   light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd,   light, normal);
        }
    }



    private static void addRopeVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                      float u, float v, int light, Vec3 normal) {
        float clampedU = Math.max(0.0f, Math.min(1.0f, u));
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255)
                .uv(clampedU, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();


    }

    private static void addRopeVertexWithAlpha(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                               float u, float v, int light, Vec3 normal, int alpha) {
        float clampedU = Math.max(0.0f, Math.min(1.0f, u));
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, alpha)
                .uv(clampedU, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }


    private static Vec3 calculateCatenaryPosition(Vec3 start, Vec3 end, float t,
                                                  double sagAmount, float windOffset, float gameTime) {
        Vec3 linearPos = start.lerp(end, t);

        double sagCurve = Math.sin(t * Math.PI) * sagAmount;

        double windSway = Math.sin((gameTime * 0.7 + t * 2)) * windOffset * Math.max(sagAmount, 0.1) * 0.3;
        double windSwayZ = Math.cos((gameTime * 0.5 + t * 1.5)) * windOffset * Math.max(sagAmount, 0.1) * 0.15;

        return linearPos.add(windSway, -sagCurve, windSwayZ);
    }

    private static int calculateDynamicLighting(Level level, Vec3 pos1, Vec3 pos2, Vec3 cameraPos) {
        Vec3 worldPos1 = pos1.add(cameraPos);
        Vec3 worldPos2 = pos2.add(cameraPos);
        Vec3 midPoint = worldPos1.add(worldPos2).scale(0.5);

        BlockPos blockPos = new BlockPos((int)midPoint.x, (int)midPoint.y, (int)midPoint.z);

        try {
            int blockLight = level.getBrightness(LightLayer.BLOCK, blockPos);
            int skyLight = level.getBrightness(LightLayer.SKY, blockPos);

            blockLight = Math.max(2, Math.min(15, blockLight));
            skyLight = Math.max(2, Math.min(15, skyLight));

            return (skyLight << 20) | (blockLight << 4);
        } catch (Exception e) {
            return (8 << 20) | (8 << 4);
        }
    }
}
