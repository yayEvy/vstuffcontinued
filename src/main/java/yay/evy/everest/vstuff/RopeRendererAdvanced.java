package yay.evy.everest.vstuff;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
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

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RopeRendererAdvanced {
    private static final ResourceLocation ROPE_TEXTURE = new ResourceLocation("vstuff", "textures/entity/rope.png");

    private static final float BASE_ROPE_WIDTH = 0.08f; // Slightly thinner
    private static final int BASE_ROPE_SEGMENTS = 48;
    private static final float ROPE_SAG_FACTOR = 0.25f;
    private static final double MAX_RENDER_DISTANCE = 300.0;
    private static final float WIND_STRENGTH = 0.02f;

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
                        renderServerRope(poseStack, bufferSource, entry.getValue(), level, cameraPos, partialTick);
                        renderedAny = true;
                    } catch (Exception e) {
                        System.err.println("Error rendering server rope: " + e.getMessage());
                    }
                }
            } else {
                for (Map.Entry<Integer, ClientConstraintTracker.ClientRopeData> entry : constraints.entrySet()) {
                    try {
                        renderClientRope(poseStack, bufferSource, entry.getValue(), level, cameraPos, partialTick);
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
            e.printStackTrace();
        }
    }

    private static void renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         ClientConstraintTracker.ClientRopeData ropeData, Level level,
                                         Vec3 cameraPos, float partialTick) {
        Vector3d startPos = ropeData.getWorldPosA(level, partialTick);
        Vector3d endPos = ropeData.getWorldPosB(level, partialTick);

        if (startPos != null && endPos != null) {
            renderAdvancedRope(poseStack, bufferSource, startPos, endPos, ropeData.maxLength, cameraPos, partialTick);
        }
    }

    private static void renderServerRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         ConstraintTracker.RopeConstraintData ropeData, Level level,
                                         Vec3 cameraPos, float partialTick) {
        try {
            Vector3d startPos = new Vector3d(ropeData.localPosA);
            Vector3d endPos = new Vector3d(ropeData.localPosB);

            if (startPos != null && endPos != null) {
                renderAdvancedRope(poseStack, bufferSource, startPos, endPos, ropeData.maxLength, cameraPos, partialTick);
            }
        } catch (Exception e) {
            System.err.println("Error in renderServerRope: " + e.getMessage());
            e.printStackTrace();
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

        int baseSegments = Math.max(16, (int) (ropeLength * 8)); // More segments for longer ropes
        int segments = Math.max(baseSegments, (int) (BASE_ROPE_SEGMENTS * (1.0 - distanceToCamera / MAX_RENDER_DISTANCE)));
        segments = Math.min(segments, 64); // Cap at 64 for performance

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
        float windOffset = (float) (Math.sin(partialTick * 0.05) * WIND_STRENGTH);

        Minecraft mc = Minecraft.getInstance();
        org.joml.Vector3f lookVector = mc.gameRenderer.getMainCamera().getLookVector();
        Vec3 cameraLook = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());

        int actualSegments = Math.max(segments, 24); // Minimum 24 segments for smoothness

        for (int i = 0; i < actualSegments; i++) {
            float t1 = (float) i / actualSegments;
            float t2 = (float) (i + 1) / actualSegments;

            Vec3 pos1 = calculateCatenaryPosition(start, end, t1, sagAmount, windOffset);
            Vec3 pos2 = calculateCatenaryPosition(start, end, t2, sagAmount, windOffset);

            Vec3 segmentDir = pos2.subtract(pos1).normalize();
            Vec3 right = segmentDir.cross(cameraLook).normalize();

            if (right.length() < 0.1) {
                right = segmentDir.cross(new Vec3(0, 1, 0)).normalize();
                if (right.length() < 0.1) {
                    right = new Vec3(1, 0, 0);
                }
            }

            int layers = 8;
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

