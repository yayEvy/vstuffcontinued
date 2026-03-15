package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.index.VStuffRenderTypes;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfig;
import yay.evy.everest.vstuff.internal.RopeStyle;
import yay.evy.everest.vstuff.internal.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RopeRendererClient {

    private static float getRopeWidth() {
        return VStuffConfig.ROPE_THICKNESS.get().floatValue();
    }

    private static final int ROPE_CURVE_SEGMENTS = 32;
    private static final float ROPE_SAG_FACTOR = 1.02f;
    private static final float NORMAL_ROPE_V_SCALE = 2.5f;
    private static final float CHAIN_ROPE_V_SCALE = 0.5f;
    private static final float WIND_STRENGTH = 0.02f;

    private static final BlockPos.MutableBlockPos sharedMutablePos = new BlockPos.MutableBlockPos();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
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

            Map<Integer, ClientRopeManager.ClientRopeData> constraints = ClientRopeManager.getClientConstraints();

            boolean renderedAny = false;

            for (Map.Entry<Integer, ClientRopeManager.ClientRopeData> entry : constraints.entrySet()) {
                try {
                    renderClientRope(
                            poseStack,
                            bufferSource,
                            entry.getKey(),
                            entry.getValue(),
                            level,
                            cameraPos,
                            partialTick,
                            entry.getValue().style()
                    );
                    renderedAny = true;
                } catch (Exception e) {
                    if (level.getGameTime() % 100 == 0) {
                        VStuff.LOGGER.error("Error rendering client rope: {}", e.getMessage());
                    }
                }
            }

            if (ClientRopeManager.hasPreviewRope()) {
                try {
                    ClientRopeManager.ClientRopeData previewRope = ClientRopeManager.getPreviewRope();
                    renderClientRope(
                            poseStack,
                            bufferSource,
                            -1,
                            previewRope,
                            level,
                            cameraPos,
                            partialTick,
                            previewRope.style()

                    );
                    renderedAny = true;
                } catch (Exception e) {
                    if (level.getGameTime() % 100 == 0) {
                        VStuff.LOGGER.error("Error rendering translucent rope: {}", e.getMessage());
                    }
                }
            }

            if (renderedAny) {
                bufferSource.endBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                         Integer constraintId, ClientRopeManager.ClientRopeData ropeData,
                                         Level level, Vec3 cameraPos, float partialTick, ResourceLocation style) {
        if (!level.isClientSide) return;
        if (!ropeData.isRenderable(level)) return;

        Vector3d startPos = RopeUtils.renderLocalToWorld(level, ropeData.localPos0(), ropeData.ship0());
        Vector3d endPos = RopeUtils.renderLocalToWorld(level, ropeData.localPos1(), ropeData.ship1());

        double actualRopeLength = startPos.distance(endPos);
        double maxRopeLength = ropeData.maxLength();

        if (VSGameUtilsKt.isBlockInShipyard(level, startPos.x, startPos.y, startPos.z)) return;
        if (VSGameUtilsKt.isBlockInShipyard(level, endPos.x, endPos.y, endPos.z)) return;

        renderRope(poseStack, bufferSource, startPos, endPos,
                    actualRopeLength, maxRopeLength, cameraPos, partialTick, level, style);

    }

    private static void renderRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Vector3d startPos, Vector3d endPos, double actualRopeLength,
                                   double maxRopeLength, Vec3 cameraPos, float partialTick, Level level, ResourceLocation style) {

        Vector3d startRelative = new Vector3d(startPos.x - cameraPos.x, startPos.y - cameraPos.y, startPos.z - cameraPos.z);
        Vector3d endRelative = new Vector3d(endPos.x - cameraPos.x, endPos.y - cameraPos.y, endPos.z - cameraPos.z);

        Minecraft mc = Minecraft.getInstance();
        int renderChunks = mc.options.renderDistance().get();
        double maxRenderDist = renderChunks * 16d;

        if (startRelative.lengthSquared() > maxRenderDist * maxRenderDist &&
                endRelative.lengthSquared() > maxRenderDist * maxRenderDist) {
            return;
        }

        double currentDistance = startRelative.distance(endRelative);
        if (currentDistance < 0.1) return;

        poseStack.pushPose();

        double stretchFactor = Math.min(actualRopeLength / Math.max(maxRopeLength, 0.1), 1.0);
        double sagAmount = ROPE_SAG_FACTOR * (1.0 - stretchFactor * stretchFactor) * actualRopeLength * 0.35;
        float stableGameTime = (level.getGameTime() + partialTick) / 20.0f;
        float windOffset = (float) (Math.sin(stableGameTime * 0.8) * 0.3 + Math.sin(stableGameTime * 1.3) * 0.2) * WIND_STRENGTH;

        Vector3d[] curvePoints = new Vector3d[ROPE_CURVE_SEGMENTS + 1];
        int[] lightValues = new int[ROPE_CURVE_SEGMENTS + 1];
        boolean[] isSolid = new boolean[ROPE_CURVE_SEGMENTS + 1];
        BlockPos.MutableBlockPos mutPos = sharedMutablePos;

        boolean underwater = false;
        int waterCheckInterval = ROPE_CURVE_SEGMENTS / 4;

        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / ROPE_CURVE_SEGMENTS;
            Vector3d point = calculateCatenaryPosition(startRelative, endRelative, t, sagAmount, windOffset, stableGameTime);
            curvePoints[i] = point;

            mutPos.set(point.x + cameraPos.x, point.y + cameraPos.y, point.z + cameraPos.z);
            BlockState state = level.getBlockState(mutPos);

            lightValues[i] = LevelRenderer.getLightColor(level, mutPos);


            if (state.getLightBlock(level, mutPos) > 0 && state.isCollisionShapeFullBlock(level, mutPos)) {
                isSolid[i] = true;
            }

            if (i % waterCheckInterval == 0 && !underwater) {
                if (level.getFluidState(mutPos).is(FluidTags.WATER)) {
                    underwater = true;
                }
            }
        }

        int lastValidLight = lightValues[0];
        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            if (isSolid[i]) {
                lightValues[i] = lastValidLight;
            } else {
                lastValidLight = lightValues[i];
            }
        }

        lastValidLight = lightValues[ROPE_CURVE_SEGMENTS];
        for (int i = ROPE_CURVE_SEGMENTS; i >= 0; i--) {
            if (isSolid[i]) {
                lightValues[i] = maxLight(lightValues[i], lastValidLight);
            } else {
                lastValidLight = lightValues[i];
            }
        }

        RenderType renderType = VStuffRenderTypes.renderStyleMap.get(RopeStyleManager.get(style).renderStyle()).apply(RopeStyleManager.get(style).texture());

        if (RopeStyleManager.get(style).renderStyle() == RopeStyle.RenderStyle.CHAIN) {
            renderChainRope(poseStack, bufferSource.getBuffer(renderType), curvePoints, lightValues);
        } else {
            renderNormalRope(poseStack, bufferSource.getBuffer(renderType), curvePoints, lightValues, startRelative, endRelative);
        }

        poseStack.popPose();
    }

    private static int maxLight(int packed1, int packed2) {
        int block1 = LightTexture.block(packed1);
        int sky1 = LightTexture.sky(packed1);
        int block2 = LightTexture.block(packed2);
        int sky2 = LightTexture.sky(packed2);

        return LightTexture.pack(Math.max(block1, block2), Math.max(sky1, sky2));
    }

    private static Vector3d right(Vector3d overall, Vector3d up) {
        Vector3d worldUp = new Vector3d(0, 1, 0);

        Vector3d right = new Vector3d();
        if (Math.abs(overall.dot(worldUp)) > 0.9) {
            right.set(1, 0, 0);
        } else {
            overall.cross(worldUp, right).normalize();
        }

        right.cross(overall, up).normalize();

        return right;
    }

    private static void renderNormalRope(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         Vector3d[] curvePoints, int[] lightValues, Vector3d start, Vector3d end) {
        Matrix4f matrix = poseStack.last().pose();

        Vector3d direction = new Vector3d(end).sub(start);
        Vector3d overallDirection = new Vector3d(direction).normalize();
        Vector3d up = new Vector3d();

        Vector3d right = right(overallDirection, up);

        Vector3d[] topRightStrip = new Vector3d[ROPE_CURVE_SEGMENTS + 1];
        Vector3d[] topLeftStrip = new Vector3d[ROPE_CURVE_SEGMENTS + 1];
        Vector3d[] bottomLeftStrip = new Vector3d[ROPE_CURVE_SEGMENTS + 1];
        Vector3d[] bottomRightStrip = new Vector3d[ROPE_CURVE_SEGMENTS + 1];

        double halfWidth = getRopeWidth() * 0.6f;
        Vector3d rightScaled = new Vector3d(right).mul(halfWidth);
        Vector3d upScaled = new Vector3d(up).mul(halfWidth);

        for (int i = 0; i <= ROPE_CURVE_SEGMENTS; i++) {
            Vector3d center = curvePoints[i];

            topRightStrip[i] = new Vector3d(center).add(rightScaled).add(upScaled);
            topLeftStrip[i] = new Vector3d(center).sub(rightScaled).add(upScaled);
            bottomLeftStrip[i] = new Vector3d(center).sub(rightScaled).sub(upScaled);
            bottomRightStrip[i] = new Vector3d(center).add(rightScaled).sub(upScaled);
        }

        double textureScale = NORMAL_ROPE_V_SCALE;

        Vector3d negUp = new Vector3d(up).mul(-1);
        Vector3d negRight = new Vector3d(right).mul(-1);

        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topLeftStrip, topRightStrip, up, curvePoints, lightValues, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, topRightStrip, bottomRightStrip, right, curvePoints, lightValues, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, bottomRightStrip, bottomLeftStrip, negUp, curvePoints, lightValues, textureScale);
        renderRopeFaceWithGapFilling(vertexConsumer, matrix, bottomLeftStrip, topLeftStrip, negRight, curvePoints, lightValues, textureScale);

    }

    private static void renderRopeFaceWithGapFilling(VertexConsumer vertexConsumer, Matrix4f matrix,
                                                     Vector3d[] strip1, Vector3d[] strip2, Vector3d normal,
                                                     Vector3d[] curvePoints, int[] lightValues, double textureScale) {
        double[] cumulativeDistances = new double[ROPE_CURVE_SEGMENTS + 1];
        cumulativeDistances[0] = 0;
        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            double segmentLength = curvePoints[i].distance(curvePoints[i + 1]);
            cumulativeDistances[i + 1] = cumulativeDistances[i] + segmentLength;
        }

        for (int i = 0; i < ROPE_CURVE_SEGMENTS; i++) {
            float vStart = (float) (cumulativeDistances[i] * textureScale);
            float vEnd = (float) (cumulativeDistances[i + 1] * textureScale);

            int lightStart = lightValues[i];
            int lightEnd = lightValues[i + 1];

            Vector3d v1 = strip1[i];
            Vector3d v2 = strip2[i];
            Vector3d v3 = strip1[i + 1];
            Vector3d v4 = strip2[i + 1];

            renderFace(vertexConsumer, matrix, v1, v2, v3, v4, vStart, vEnd, lightStart, lightEnd, normal);

            Vector3d center1 = new Vector3d(v1).add(v2).mul(0.5);
            Vector3d center2 = new Vector3d(v3).add(v4).mul(0.5);

            addRopeVertexWithAlpha(vertexConsumer, matrix, center1, 0.5f, vStart, lightStart, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, v2, 1.0f, vStart, lightStart, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center2, 0.5f, vEnd, lightEnd, normal, 128);

            addRopeVertexWithAlpha(vertexConsumer, matrix, v1, 0.0f, vStart, lightStart, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center1, 0.5f, vStart, lightStart, normal, 128);
            addRopeVertexWithAlpha(vertexConsumer, matrix, center2, 0.5f, vEnd, lightEnd, normal, 128);
        }
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

            Vector3d diag1 = new Vector3d(right).add(up).normalize();
            Vector3d diag2 = new Vector3d(right).sub(up).normalize();

            Vector3d startTR = new Vector3d(pStart).add(rScaled).add(uScaled);
            Vector3d startBL = new Vector3d(pStart).sub(rScaled).sub(uScaled);
            Vector3d endTR = new Vector3d(pEnd).add(rScaled).add(uScaled);
            Vector3d endBL = new Vector3d(pEnd).sub(rScaled).sub(uScaled);

            Vector3d startTL = new Vector3d(pStart).sub(rScaled).add(uScaled);
            Vector3d startBR = new Vector3d(pStart).add(rScaled).sub(uScaled);
            Vector3d endTL = new Vector3d(pEnd).sub(rScaled).add(uScaled);
            Vector3d endBR = new Vector3d(pEnd).add(rScaled).sub(uScaled);

            float vStart = (float) (cumulativeDistances[i] * textureVScale);
            float vEnd = (float) (cumulativeDistances[i + 1] * textureVScale);

            renderFace(vertexConsumer, matrix, startTL, startBR, endTL, endBR, vStart, vEnd, lightValues[i], lightValues[i+1], diag1);
            renderFace(vertexConsumer, matrix, startTR, startBL, endTR, endBL, vStart, vEnd, lightValues[i], lightValues[i+1], diag2);
        }
    }

    private static void renderFace(VertexConsumer consumer, Matrix4f matrix, Vector3d v1, Vector3d v2,
                                        Vector3d v3, Vector3d v4, float vStart, float vEnd, int lightEnd, int lightStart,
                                        Vector3d normal) {
        addRopeVertex(consumer, matrix, v1, 0.0f, vStart, lightStart, normal);
        addRopeVertex(consumer, matrix, v2, 1.0f, vStart, lightStart, normal);
        addRopeVertex(consumer, matrix, v3,   0.0f, vEnd,   lightEnd, normal);
        addRopeVertex(consumer, matrix, v2, 1.0f, vStart, lightStart, normal);
        addRopeVertex(consumer, matrix, v4,   1.0f, vEnd,   lightEnd, normal);
        addRopeVertex(consumer, matrix, v3,   0.0f, vEnd,   lightEnd, normal);
    }

    private static void addRopeVertex(VertexConsumer consumer, Matrix4f matrix, Vector3d pos,
                                      float u, float v, int light, Vector3d normal) {
        addRopeVertexWithAlpha(consumer, matrix, pos, u, v, light, normal, 255);
    }

    private static void addRopeVertexWithAlpha(VertexConsumer consumer, Matrix4f matrix, Vector3d pos,
                                               float u, float v, int light, Vector3d normal, int alpha) {
        float clampedU = Math.max(0.0f, Math.min(1.0f, u));
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, alpha)
                .uv(clampedU, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private static Vector3d calculateCatenaryPosition(Vector3d start, Vector3d end, float t,
                                                      double sagAmount, float windOffset, float gameTime) {
        double x = start.x + (end.x - start.x) * t;
        double y = start.y + (end.y - start.y) * t;
        double z = start.z + (end.z - start.z) * t;

        double sagCurve = Math.sin(t * Math.PI) * sagAmount;
        double windSway = Math.sin((gameTime * 0.7 + t * 2)) * windOffset * Math.max(sagAmount, 0.1) * 0.3;
        double windSwayZ = Math.cos((gameTime * 0.5 + t * 1.5)) * windOffset * Math.max(sagAmount, 0.1) * 0.15;

        return new Vector3d(x + windSway, y - sagCurve, z + windSwayZ);
    }
}