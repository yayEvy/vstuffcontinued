package yay.evy.everest.vstuff.ropes;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RopeRendererAdvanced {
    private static final ResourceLocation ROPE_TEXTURE = new ResourceLocation("vstuff", "textures/entity/rope.png");
    private static final float BASE_ROPE_WIDTH = 0.08f;
    private static final int BASE_ROPE_SEGMENTS = 48;
    private static final float ROPE_SAG_FACTOR = 0.25f;
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

                float responsiveness = 0.3f; // Higher = more responsive, lower = more stable
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
                bufferSource.endBatch(RenderType.entityCutoutNoCull(ROPE_TEXTURE));
            }
        } catch (Exception e) {
            System.err.println("Error in rope rendering: " + e.getMessage());
        }
    }


    private static void renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ClientConstraintTracker.ClientRopeData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick) {
        // Check if we're actually on client side and handle appropriately
        Vector3d startPos = null;
        Vector3d endPos = null;

        if (level.isClientSide) {
            // For client-side rendering, use the client-safe methods
            startPos = ropeData.getWorldPosA(level, partialTick);
            endPos = ropeData.getWorldPosB(level, partialTick);
        } else {
            // Fallback - this shouldn't happen in client rendering, but just in case
            System.err.println("Warning: Client renderer called on server side!");
            return;
        }

        if (startPos != null && endPos != null) {
            RopePositionCache cache = positionCache.computeIfAbsent(constraintId, k -> new RopePositionCache());
            cache.updatePositions(startPos, endPos, partialTick);

            renderAdvancedRope(poseStack, bufferSource, cache.smoothStartPos, cache.smoothEndPos,
                    ropeData.maxLength, cameraPos, partialTick);
        }
    }


    private static void renderServerRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ConstraintTracker.RopeConstraintData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick) {
        try {
            // For server ropes, try to get actual world positions if possible
            Vector3d startPos = ropeData.getWorldPosA((ServerLevel) level, partialTick);
            Vector3d endPos = ropeData.getWorldPosB((ServerLevel) level, partialTick);

            if (startPos != null && endPos != null) {
                RopePositionCache cache = positionCache.computeIfAbsent(constraintId, k -> new RopePositionCache());
                cache.updatePositions(startPos, endPos, partialTick);

                renderAdvancedRope(poseStack, bufferSource, cache.smoothStartPos, cache.smoothEndPos,
                        ropeData.maxLength, cameraPos, partialTick);
            }
        } catch (Exception e) {
            System.err.println("Error in renderServerRope: " + e.getMessage());
        }
    }

    private static void renderAdvancedRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                           Vector3d startPos, Vector3d endPos, double maxLength,
                                           Vec3 cameraPos, float partialTick) {
        Vec3 start = new Vec3(startPos.x - cameraPos.x, startPos.y - cameraPos.y, startPos.z - cameraPos.z);
        Vec3 end = new Vec3(endPos.x - cameraPos.x, endPos.y - cameraPos.y, endPos.z - cameraPos.z);

        double distanceToCamera = Math.min(start.length(), end.length());
        if (distanceToCamera > MAX_RENDER_DISTANCE) {
            return;
        }

        double ropeLength = start.distanceTo(end);
        if (ropeLength < 0.1) {
            return;
        }

        // SMOOTHING: More segments for smoother curves, but optimize based on distance
        int baseSegments = Math.max(32, (int) (ropeLength * 12)); // Increased base segments
        int segments = Math.max(baseSegments, (int) (BASE_ROPE_SEGMENTS * (1.0 - distanceToCamera / MAX_RENDER_DISTANCE)));
        segments = Math.min(segments, 96); // Increased max segments for smoothness

        float ropeWidth = BASE_ROPE_WIDTH * 3.0f;

        poseStack.pushPose();
        RenderType renderType = RenderType.entityCutout(ROPE_TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        renderRopeWithPhysics(poseStack, vertexConsumer, start, end, maxLength, segments, ropeWidth, partialTick);

        poseStack.popPose();
    }



    private static void renderRopeWithPhysics(PoseStack poseStack, VertexConsumer vertexConsumer,
                                              Vec3 start, Vec3 end, double maxLength,
                                              int segments, float ropeWidth, float partialTick) {
        if (segments <= 0 || ropeWidth <= 0) return;

        Matrix4f matrix = poseStack.last().pose();
        Vec3 direction = end.subtract(start);
        double actualLength = direction.length();
        if (actualLength < 0.01) return;

        direction = direction.normalize();
        double tension = Math.min(actualLength / Math.max(maxLength, 1.0), 1.0);
        double sagAmount = ROPE_SAG_FACTOR * (1.0 - tension * 0.5) * actualLength * 0.4;

        // SMOOTHING: Smoother wind animation
        float gameTime = (float) (System.currentTimeMillis() % 100000) / 1000.0f;
        float windOffset = (float) (Math.sin(gameTime * 0.8) * 0.3 + Math.sin(gameTime * 1.3) * 0.2) * WIND_STRENGTH;

        Minecraft mc = Minecraft.getInstance();
        org.joml.Vector3f lookVector = mc.gameRenderer.getMainCamera().getLookVector();
        Vec3 cameraLook = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());

        // SMOOTHING: Higher minimum segments for smoother curves
        int actualSegments = Math.max(segments, 48); // Increased minimum

        for (int i = 0; i < actualSegments; i++) {
            float t1 = (float) i / actualSegments;
            float t2 = (float) (i + 1) / actualSegments;

            Vec3 pos1 = calculateSmoothCatenaryPosition(start, end, t1, sagAmount, windOffset, gameTime);
            Vec3 pos2 = calculateSmoothCatenaryPosition(start, end, t2, sagAmount, windOffset, gameTime);

            Vec3 segmentDir = pos2.subtract(pos1).normalize();
            Vec3 right = segmentDir.cross(cameraLook).normalize();

            if (right.length() < 0.1) {
                right = segmentDir.cross(new Vec3(0, 1, 0)).normalize();
                if (right.length() < 0.1) {
                    right = new Vec3(1, 0, 0);
                }
            }

            // SMOOTHING: More layers for rounder rope
            int layers = 12; // Increased from 8
            for (int layer = 0; layer < layers; layer++) {
                float angle = (float) (layer * Math.PI * 2.0 / layers);
                Vec3 rotatedRight = rotateVectorAroundAxis(right, segmentDir, angle);
                Vec3 offset = rotatedRight.scale(ropeWidth * 0.5);

                Vec3 vert1 = pos1.subtract(offset);
                Vec3 vert2 = pos1.add(offset);
                Vec3 vert3 = pos2.add(offset);
                Vec3 vert4 = pos2.subtract(offset);

                float u1 = t1 * 3.0f;
                float u2 = t2 * 3.0f;
                float vCoord1 = (float) layer / layers;
                float vCoord2 = (float) (layer + 1) / layers;

                int light = calculateDynamicLighting(pos1, pos2);
                Vec3 normal = calculateSegmentNormal(segmentDir, rotatedRight);

                addRopeVertex(vertexConsumer, matrix, vert1, u1, vCoord1, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert2, u1, vCoord2, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert3, u2, vCoord2, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert1, u1, vCoord1, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert3, u2, vCoord2, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert4, u2, vCoord1, light, normal);
            }
        }
    }

    private static Vec3 calculateSmoothCatenaryPosition(Vec3 start, Vec3 end, float t,
                                                        double sagAmount, float windOffset, float gameTime) {
        Vec3 linearPos = start.lerp(end, t);

        // Smoother sag curve using a combination of sine functions
        double sagCurve = Math.sin(t * Math.PI) * sagAmount * 3.0;
        sagCurve += Math.sin(t * Math.PI * 2) * sagAmount * 0.3; // Add secondary curve for realism

        // Smoother wind with multiple frequencies
        double windSway = (Math.sin((gameTime + t * 2) * 1.2) * 0.6 +
                Math.sin((gameTime + t * 3) * 0.8) * 0.4) * windOffset * sagAmount * 0.5;

        Vec3 basePos = linearPos.add(windSway, -Math.abs(sagCurve), windSway * 0.3);

        // Simplified collision detection for better performance
        return basePos;
    }

    public static void cleanupCache() {
        Map<Integer, ClientConstraintTracker.ClientRopeData> activeConstraints = ClientConstraintTracker.getClientConstraints();
        positionCache.keySet().retainAll(activeConstraints.keySet());
    }


    private static Vec3 calculateSegmentNormal(Vec3 segmentDir, Vec3 right) {
        Vec3 up = segmentDir.cross(right).normalize();
        return right.cross(up).normalize();
    }

    private static void addRopeVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                      float u, float v, int light, Vec3 normal) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(200, 180, 140, 255)
                .uv(u, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static Vec3 rotateVectorAroundAxis(Vec3 vector, Vec3 axis, float angle) {
        Vec3 k = axis.normalize();
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        Vec3 vCrossK = vector.cross(k);
        double vDotK = vector.dot(k);

        return vector.scale(cosAngle)
                .add(vCrossK.scale(sinAngle))
                .add(k.scale(vDotK * (1 - cosAngle)));
    }

    private static Vec3 calculateCatenaryPosition(Vec3 start, Vec3 end, float t, double sagAmount, float windOffset) {
        Vec3 linearPos = start.lerp(end, t);
        double sagCurve = Math.sin(t * Math.PI) * sagAmount * 3.0;
        double windSway = Math.sin(t * Math.PI * 3) * windOffset * sagAmount * 0.5;

        Vec3 basePos = linearPos.add(windSway, -Math.abs(sagCurve), windSway * 0.3);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Vec3 worldPos = basePos.add(cameraPos);
            net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos((int)worldPos.x, (int)worldPos.y, (int)worldPos.z);

            if (!mc.level.getBlockState(blockPos).isAir()) {
                Vec3[] offsets = {
                        new Vec3(0, 1.0, 0),
                        new Vec3(0.5, 0.5, 0),
                        new Vec3(-0.5, 0.5, 0),
                        new Vec3(0, 0, 0.5),
                        new Vec3(0, 0, -0.5)
                };

                for (Vec3 offset : offsets) {
                    Vec3 testPos = worldPos.add(offset);
                    net.minecraft.core.BlockPos testBlockPos = new net.minecraft.core.BlockPos((int)testPos.x, (int)testPos.y, (int)testPos.z);
                    if (mc.level.getBlockState(testBlockPos).isAir()) {
                        return basePos.add(offset);
                    }
                }
            }
        }

        return basePos;
    }

    private static int calculateDynamicLighting(Vec3 pos1, Vec3 pos2) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Vec3 worldPos = pos1.add(cameraPos);
            net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos((int)worldPos.x, (int)worldPos.y, (int)worldPos.z);

            int blockLight = mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, blockPos);
            int skyLight = mc.level.getBrightness(net.minecraft.world.level.LightLayer.SKY, blockPos);

            blockLight = Math.min(blockLight, 8);
            skyLight = Math.min(skyLight, 10);

            return (skyLight << 20) | (blockLight << 4);
        }
        return (8 << 20) | (6 << 4);
    }

    private static void addTexturedVertexWithNormal(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                                    float u, float v, int light, Vec3 normal) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static void addTexturedVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                          float u, float v, int light) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(0)
                .uv2(light)
                .normal(0, 1, 0)
                .endVertex();
    }

    private static Vec3 calculateCollisionAwarePosition(Vec3 start, Vec3 end, float t, double sagAmount,
                                                        float windOffset, Level level, Vec3 cameraPos) {
        Vec3 basePos = calculateCatenaryPosition(start, end, t, sagAmount, windOffset);
        Vec3 worldPos = basePos.add(cameraPos);

        if (level != null) {
            try {
                net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos((int)worldPos.x, (int)worldPos.y, (int)worldPos.z);
                if (!level.getBlockState(blockPos).isAir()) {
                    return basePos.add(0, 1.0, 0);
                }
            } catch (Exception e) {
            }
        }
        return basePos;
    }

    private static Vec3 calculatePhysicsRopePosition(Vec3 start, Vec3 end, float t, double sagAmount, float windOffset) {
        Vec3 linearPos = start.lerp(end, t);
        double sagCurve = Math.sin(t * Math.PI) * sagAmount;
        double windSway = Math.sin(t * Math.PI * 2) * windOffset * sagAmount;
        return linearPos.add(windSway, -sagCurve, 0);
    }

    private static int calculateAlpha(Vec3 pos1, Vec3 pos2, double tension) {
        double avgDistance = (pos1.length() + pos2.length()) * 0.5;
        double distanceAlpha = Math.max(0.4, 1.0 - (avgDistance / MAX_RENDER_DISTANCE));
        double tensionAlpha = 0.7 + (tension * 0.3);
        return (int) Mth.clamp(255 * distanceAlpha * tensionAlpha, 100, 255);
    }

    private static void addAdvancedVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                                          float u, float v, int light, int alpha) {
        try {
            consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                    .color(255, 255, 255, alpha)
                    .uv(u, v)
                    .overlayCoords(0, 10)
                    .uv2(light)
                    .normal(0, 1, 0)
                    .endVertex();
        } catch (Exception e) {
            consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                    .color(255, 255, 255, 255)
                    .uv(u, v)
                    .endVertex();
        }
    }
}

