package yay.evy.everest.vstuff.ropes;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.rendering.RopeRendererType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RopeRendererAdvanced {
    private static final ResourceLocation ROPE_TEXTURE = new ResourceLocation("vstuff", "textures/entity/rope.png");
    private static final float ROPE_WIDTH = 0.28f;
    private static final int ROPE_CURVE_SEGMENTS = 32; // For calculating the curve
    private static final float ROPE_SAG_FACTOR = 1.02f;
    private static final double MAX_RENDER_DISTANCE = 300.0;
    private static final float WIND_STRENGTH = 0.02f;
    private static final Map<Integer, RopePositionCache> positionCache = new ConcurrentHashMap<>();

    private static class RopePositionCache {
        Vector3d lastStartPos = new Vector3d();
        Vector3d lastEndPos = new Vector3d();
        Vector3d smoothStartPos = new Vector3d();
        Vector3d smoothEndPos = new Vector3d();
        Vector3d startVelocity = new Vector3d();
        Vector3d endVelocity = new Vector3d();
        long lastUpdateTime = 0;

        public void updatePositions(Vector3d newStart, Vector3d newEnd, float partialTick) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000.0f, 0.1f);

            if (lastUpdateTime == 0) {
                smoothStartPos.set(newStart);
                smoothEndPos.set(newEnd);
                startVelocity.set(0, 0, 0);
                endVelocity.set(0, 0, 0);
            } else {
                if (deltaTime > 0) {
                    Vector3d newStartVel = new Vector3d(newStart).sub(lastStartPos).div(deltaTime);
                    Vector3d newEndVel = new Vector3d(newEnd).sub(lastEndPos).div(deltaTime);
                    startVelocity.lerp(newStartVel, 0.3f);
                    endVelocity.lerp(newEndVel, 0.3f);
                }
                Vector3d predictedStart = new Vector3d(newStart).add(new Vector3d(startVelocity).mul(partialTick * 0.05f));
                Vector3d predictedEnd = new Vector3d(newEnd).add(new Vector3d(endVelocity).mul(partialTick * 0.05f));
                float responsiveness = 0.3f;
                smoothStartPos.lerp(predictedStart, responsiveness);
                smoothEndPos.lerp(predictedEnd, responsiveness);
            }

            lastStartPos.set(newStart);
            lastEndPos.set(newEnd);
            lastUpdateTime = currentTime;
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        try {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level == null) return;

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            Vec3 cameraPos = event.getCamera().getPosition();
            float partialTick = event.getPartialTick();

            RenderType renderType = RopeRendererType.ropeRenderer(ROPE_TEXTURE);

            Map<Integer, ClientConstraintTracker.ClientRopeData> constraints = ClientConstraintTracker.getClientConstraints();
            Map<Integer, ConstraintTracker.RopeConstraintData> serverConstraints = ConstraintTracker.getActiveConstraints();

            boolean renderedAny = false;

            if (constraints.isEmpty()) {
                for (Map.Entry<Integer, ConstraintTracker.RopeConstraintData> entry : serverConstraints.entrySet()) {
                    try {
                        renderServerRope(poseStack, bufferSource, entry.getKey(), entry.getValue(), level, cameraPos, partialTick);
                        renderedAny = true;
                    } catch (Exception e) {
                        System.err.println("Error rendering server rope: " + e.getMessage());
                    }
                }
            } else {
                for (Map.Entry<Integer, ClientConstraintTracker.ClientRopeData> entry : constraints.entrySet()) {
                    try {
                        renderClientRope(poseStack, bufferSource, entry.getKey(), entry.getValue(), level, cameraPos, partialTick);
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

    private static void renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ClientConstraintTracker.ClientRopeData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick) {
        if (!level.isClientSide) {
            System.err.println("Warning: Client renderer called on server side!");
            return;
        }
        Vector3d startPos = ropeData.getWorldPosA(level, partialTick);
        Vector3d endPos = ropeData.getWorldPosB(level, partialTick);
        if (startPos != null && endPos != null) {
            RopePositionCache cache = positionCache.computeIfAbsent(constraintId, k -> new RopePositionCache());
            cache.updatePositions(startPos, endPos, partialTick);

            // Calculate actual current rope length for texture mapping
            double actualRopeLength = cache.smoothStartPos.distance(cache.smoothEndPos);

            renderRope(poseStack, bufferSource, cache.smoothStartPos, cache.smoothEndPos,
                    actualRopeLength, cameraPos, partialTick); // Use actual length, not maxLength
        }
    }

    private static void renderServerRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ConstraintTracker.RopeConstraintData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick) {
        try {
            Vector3d startPos = ropeData.getWorldPosA((ServerLevel) level, partialTick);
            Vector3d endPos = ropeData.getWorldPosB((ServerLevel) level, partialTick);
            if (startPos != null && endPos != null) {
                RopePositionCache cache = positionCache.computeIfAbsent(constraintId, k -> new RopePositionCache());
                cache.updatePositions(startPos, endPos, partialTick);

                // Calculate actual current rope length for texture mapping
                double actualRopeLength = cache.smoothStartPos.distance(cache.smoothEndPos);

                renderRope(poseStack, bufferSource, cache.smoothStartPos, cache.smoothEndPos,
                        actualRopeLength, cameraPos, partialTick); // Use actual length, not maxLength
            }
        } catch (Exception e) {
            System.err.println("Error in renderServerRope: " + e.getMessage());
        }
    }


    private static void renderRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Vector3d startPos, Vector3d endPos, double actualRopeLength,
                                   Vec3 cameraPos, float partialTick) {
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

        poseStack.pushPose();

        RenderType renderType = RopeRendererType.ropeRenderer(ROPE_TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        renderSingleRopeSegment(poseStack, vertexConsumer, start, end, actualRopeLength, partialTick);

        poseStack.popPose();
    }


    private static void renderRopeFace(VertexConsumer vertexConsumer, Matrix4f matrix,
                                       Vec3[] strip1, Vec3[] strip2, Vec3 normal) {
        // Calculate actual curve distances for proper UV mapping
        double totalCurveLength = 0;
        double[] segmentLengths = new double[ROPE_CURVE_SEGMENTS];

        // Calculate the actual 3D distance along the curve
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            double strip1Length = strip1[i].distanceTo(strip1[i + 1]);
            double strip2Length = strip2[i].distanceTo(strip2[i + 1]);
            segmentLengths[i] = (strip1Length + strip2Length) / 2.0;
            totalCurveLength += segmentLengths[i];
        }

        // Fix: Use consistent texture scaling
        double textureScale = 0.5 / ROPE_WIDTH;
        double currentLength = 0;

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            // Calculate V coordinates based on actual distance traveled
            float vStart = (float)(currentLength * textureScale);
            float vEnd = (float)((currentLength + segmentLengths[i]) * textureScale);

            Vec3 p1 = strip1[i];
            Vec3 p2 = strip2[i];
            Vec3 p3 = strip2[i + 1];
            Vec3 p4 = strip1[i + 1];

            int light = calculateDynamicLighting(p1, p2);

            // Render two triangles with consistent winding
            addRopeVertex(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p3, 1.0f, vEnd, light, normal);

            addRopeVertex(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p3, 1.0f, vEnd, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);

            currentLength += segmentLengths[i];
        }
    }

    private static void renderSingleRopeSegment(PoseStack poseStack, VertexConsumer vertexConsumer,
                                                Vec3 start, Vec3 end, double actualRopeLength, float partialTick) {
        Matrix4f matrix = poseStack.last().pose();
        Vec3 direction = end.subtract(start);
        double currentDistance = direction.length();
        if (currentDistance < 0.01) return;

        // Improved tension calculation for more realistic sag
        double tension = Math.min(currentDistance / Math.max(actualRopeLength, currentDistance), 1.0);
        double sagAmount = ROPE_SAG_FACTOR * (1.0 - tension * 0.6) * currentDistance * 0.35;

        float gameTime = (float) (System.currentTimeMillis() % 100000) / 1000.0f;
        float windOffset = (float) (Math.sin(gameTime * 0.8) * 0.3 + Math.sin(gameTime * 1.3) * 0.2) * WIND_STRENGTH;

        // Calculate curve points
        Vec3[] curvePoints = new Vec3[ROPE_CURVE_SEGMENTS + 1];
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / ROPE_CURVE_SEGMENTS;
            curvePoints[i] = calculateCatenaryPosition(start, end, t, sagAmount, windOffset, gameTime);
        }

        // Calculate the ACTUAL curve length by measuring the curve points
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

        // Create the 4 corner strips with overlapping geometry
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

        // Render all faces with the SAME curve points and total curve length for consistent UV mapping
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topLeftStrip, topRightStrip, up, curvePoints, totalCurveLength);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topRightStrip, bottomRightStrip, right, curvePoints, totalCurveLength);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, bottomRightStrip, bottomLeftStrip, up.scale(-1), curvePoints, totalCurveLength);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, bottomLeftStrip, topLeftStrip, right.scale(-1), curvePoints, totalCurveLength);

        // Add diagonal faces to fill corner gaps - IMPORTANT: Use the same curvePoints and totalCurveLength
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topLeftStrip, bottomRightStrip, right.add(up).normalize(), curvePoints, totalCurveLength);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topRightStrip, bottomLeftStrip, right.subtract(up).normalize(), curvePoints, totalCurveLength);
    }

    private static void renderRopeFaceWithGapFilling(VertexConsumer vertexConsumer, Matrix4f matrix,
                                                     Vec3[] strip1, Vec3[] strip2, Vec3 normal,
                                                     Vec3[] curvePoints, double totalCurveLength) {
        // Use the SAME curve points that were passed in - don't recalculate!
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            // Use the curve points distance that was already calculated
            double segmentLength = curvePoints[i].distanceTo(curvePoints[i + 1]);
            cumulativeDistances[i + 1] = cumulativeDistances[i] + segmentLength;
        }

        // Use consistent texture scaling for the entire rope
        double textureScale = 0.5 / ROPE_WIDTH;

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            // Use the cumulative distances from the SAME curve calculation
            float vStart = (float) (cumulativeDistances[i] * textureScale);
            float vEnd = (float) (cumulativeDistances[i + 1] * textureScale);

            Vec3 p1 = strip1[i];
            Vec3 p2 = strip2[i];
            Vec3 p3 = strip2[i + 1];
            Vec3 p4 = strip1[i + 1];

            int light = calculateDynamicLighting(p1, p2);

            // Main triangles
            addRopeVertex(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);

            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p3, 1.0f, vEnd, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);

            // Reduced overlap triangles
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



    private static void renderRopeFaceClean(VertexConsumer vertexConsumer, Matrix4f matrix,
                                            Vec3[] strip1, Vec3[] strip2, Vec3 normal, double totalCurveLength, float uvOffset) {
        // Fix: Use consistent texture scaling
        double textureScale = 0.5 / ROPE_WIDTH;

        // Calculate cumulative distances
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            double segmentLength = strip1[i].distanceTo(strip1[i + 1]);
            cumulativeDistances[i + 1] = cumulativeDistances[i] + segmentLength;
        }

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            float vStart = ((float) (cumulativeDistances[i] * textureScale)) + uvOffset;
            float vEnd = ((float) (cumulativeDistances[i + 1] * textureScale)) + uvOffset;

            Vec3 p1 = strip1[i];
            Vec3 p2 = strip2[i];
            Vec3 p3 = strip2[i + 1];
            Vec3 p4 = strip1[i + 1];

            int light = calculateDynamicLighting(p1, p2);

            addRopeVertex(vertexConsumer, matrix, p1, 0.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);

            addRopeVertex(vertexConsumer, matrix, p2, 1.0f, vStart, light, normal);
            addRopeVertex(vertexConsumer, matrix, p3, 1.0f, vEnd, light, normal);
            addRopeVertex(vertexConsumer, matrix, p4, 0.0f, vEnd, light, normal);
        }
    }


    private static void addRopeVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                      float u, float v, int light, Vec3 normal) {
        float clampedU = Math.max(0.0f, Math.min(1.0f, u));
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255) // Ensure full white color - no tinting
                .uv(clampedU, v)
                .overlayCoords(0) // Make sure overlay is 0 to avoid red tinting
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static void addRopeVertexWithAlpha(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                               float u, float v, int light, Vec3 normal, int alpha) {
        float clampedU = Math.max(0.0f, Math.min(1.0f, u));
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, alpha) // Keep white color
                .uv(clampedU, v)
                .overlayCoords(0) // Ensure no overlay tinting
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }





    private static void renderRopeLayer(VertexConsumer vertexConsumer, Matrix4f matrix, Vec3[] curvePoints,
                                        Vec3 right, Vec3 up, float halfWidth, double totalCurveLength, float uvOffset) {
        // Create 6 strips for good coverage without too much overlap (hexagonal cross-section)
        Vec3[][] strips = new Vec3[6][ROPE_CURVE_SEGMENTS + 1];

        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            Vec3 center = curvePoints[i];

            // Create 6 points around the rope center (hexagonal cross-section)
            for (int side = 0; side < 6; side++) {
                double angle = (side * Math.PI * 2.0) / 6.0;
                Vec3 offset = right.scale(Math.cos(angle) * halfWidth).add(up.scale(Math.sin(angle) * halfWidth));
                strips[side][i] = center.add(offset);
            }
        }

        // Render faces between adjacent strips
        for (int side = 0; side < 6; side++) {
            int nextSide = (side + 1) % 6;
            Vec3 normal = calculateNormal(strips[side], strips[nextSide]);
            renderRopeFaceClean(vertexConsumer, matrix, strips[side], strips[nextSide], normal, totalCurveLength, uvOffset);
        }
    }




    private static Vec3 calculateNormal(Vec3[] strip1, Vec3[] strip2) {
        // Calculate normal from the middle of the strips
        int midPoint = strip1.length / 2;
        Vec3 v1 = strip1[midPoint + 1].subtract(strip1[midPoint]);
        Vec3 v2 = strip2[midPoint].subtract(strip1[midPoint]);
        return v1.cross(v2).normalize();
    }









    private static Vec3 calculateCatenaryPosition(Vec3 start, Vec3 end, float t,
                                                  double sagAmount, float windOffset, float gameTime) {
        // Smooth interpolation between start and end
        Vec3 linearPos = start.lerp(end, t);

        // Simple single catenary sag curve - only one sag point in the middle
        double sagCurve = Math.sin(t * Math.PI) * sagAmount;

        // Remove the secondary wave that was causing multiple sag points
        // Keep only subtle wind effects
        double windSway = Math.sin((gameTime * 0.7 + t * 2)) * windOffset * sagAmount * 0.3; // Reduced frequency
        double windSwayZ = Math.cos((gameTime * 0.5 + t * 1.5)) * windOffset * sagAmount * 0.15; // Reduced frequency

        // Apply effects - single smooth sag
        return linearPos.add(windSway, -sagCurve, windSwayZ);
    }








    private static int calculateDynamicLighting(Vec3 pos1, Vec3 pos2) {
        // Temporarily return full bright lighting to test
        return (15 << 20) | (15 << 4);
    }

    public static void cleanupCache() {
        Map<Integer, ClientConstraintTracker.ClientRopeData> activeConstraints = ClientConstraintTracker.getClientConstraints();
        positionCache.keySet().retainAll(activeConstraints.keySet());
    }
}

