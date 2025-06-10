package yay.evy.everest.vstuff.ropes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RopePulleyRenderer implements BlockEntityRenderer<RopePulleyBlockEntity> {
    private static final ResourceLocation ROPE_TEXTURE = new ResourceLocation("vstuff", "textures/entity/rope.png");
    private static final float BASE_ROPE_WIDTH = 0.08f;
    private static final int BASE_ROPE_SEGMENTS = 48;
    private static final float ROPE_SAG_FACTOR = 0.25f;
    private static final double MAX_RENDER_DISTANCE = 300.0;
    private static final float WIND_STRENGTH = 0.02f;

    private static final Map<String, RopePositionCache> positionCache = new ConcurrentHashMap<>();

    private static class RopePositionCache {
        double lastRopeLength = 0;
        double smoothRopeLength = 0;
        Vec3 lastEndPos = Vec3.ZERO;
        Vec3 smoothEndPos = Vec3.ZERO;
        Vec3 targetEndPos = Vec3.ZERO;
        long lastUpdateTime = 0;
        boolean initialized = false;
        double lastAnchorDistance = -1;
        int stableFrames = 0;

        public void updateRopeLength(double newLength, float partialTick) {
            long currentTime = System.currentTimeMillis();

            if (!initialized) {
                smoothRopeLength = newLength;
                lastRopeLength = newLength;
                lastUpdateTime = currentTime;
                initialized = true;
                return;
            }

            float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000.0f, 0.05f);

            if (deltaTime > 0.001f) {
                double lengthDiff = Math.abs(newLength - lastRopeLength);
                if (lengthDiff < 0.01) {
                    stableFrames++;
                } else {
                    stableFrames = 0;
                }

                float smoothingFactor = Math.min(deltaTime * 8.0f, 0.25f);
                smoothRopeLength = Mth.lerp(smoothingFactor, smoothRopeLength, newLength);

                lastRopeLength = newLength;
                lastUpdateTime = currentTime;
            }
        }

        public void updateEndPosition(Vec3 newEndPos, Vec3 anchorPos, double ropeLength, double anchorDistance) {
            if (!initialized) {
                smoothEndPos = newEndPos;
                lastEndPos = newEndPos;
                targetEndPos = newEndPos;
                lastAnchorDistance = anchorDistance;
                return;
            }

            targetEndPos = newEndPos;

            float lerpFactor = 0.12f;

            if (stableFrames > 10) {
                lerpFactor = 0.18f;
            }

            smoothEndPos = smoothEndPos.lerp(targetEndPos, lerpFactor);
            lastEndPos = newEndPos;
            lastAnchorDistance = anchorDistance;
        }
    }




    public RopePulleyRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RopePulleyBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        double currentRopeLength = blockEntity.getCurrentRopeLength();
        double previousRopeLength = blockEntity.getPreviousRopeLength();

        double interpolatedLength = Mth.lerp(partialTick, previousRopeLength, currentRopeLength);

        if (interpolatedLength <= 0.005) return;

        String cacheKey = blockEntity.getBlockPos().toString();
        RopePositionCache cache = positionCache.computeIfAbsent(cacheKey, k -> new RopePositionCache());

        cache.updateRopeLength(interpolatedLength, partialTick);

        Vec3 ropeStart = new Vec3(0.5, 0.1, 0.5); // Center of block, slightly above bottom
        Vec3 calculatedEndPos = calculateRopeEndPosition(blockEntity, ropeStart, cache.smoothRopeLength, partialTick);

        double anchorDistance = 0;
        if (blockEntity.hasAnchor() && blockEntity.getAnchorPoint() != null) {
            anchorDistance = calculatedEndPos.length();
        }

        cache.updateEndPosition(calculatedEndPos,
                blockEntity.hasAnchor() ? new Vec3(anchorDistance, 0, 0) : Vec3.ZERO,
                cache.smoothRopeLength, anchorDistance);

        double distance = ropeStart.distanceTo(cache.smoothEndPos);
        if (distance < 0.01 || distance > 1000) {
            if (distance > 1000) {
                cache.smoothEndPos = calculatedEndPos;
                distance = ropeStart.distanceTo(cache.smoothEndPos);
            }
            if (distance < 0.01) return;
        }

        renderAdvancedRope(poseStack, bufferSource, ropeStart, cache.smoothEndPos,
                cache.smoothRopeLength, partialTick, level);

        renderPulleyMechanism(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }



    private float smoothStep(float t) {
        return t * t * (3.0f - 2.0f * t);
    }


    private void renderAdvancedRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                    Vec3 start, Vec3 end, double maxLength, float partialTick, Level level) {
        double ropeLength = start.distanceTo(end);
        if (ropeLength < 0.01) return;

        double distanceToCamera = Math.min(start.length(), end.length());
        if (distanceToCamera > MAX_RENDER_DISTANCE) return;

        int baseSegments = Math.max(16, (int) (ropeLength * 8));
        int segments = Math.max(baseSegments, (int) (BASE_ROPE_SEGMENTS * (1.0 - distanceToCamera / MAX_RENDER_DISTANCE)));
        segments = Mth.clamp(segments, 16, 64);

        float ropeWidth = BASE_ROPE_WIDTH * 3.0f;

        poseStack.pushPose();
        RenderType renderType = RenderType.entityCutout(ROPE_TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        renderRopeWithPhysics(poseStack, vertexConsumer, start, end, maxLength, segments, ropeWidth, partialTick, level);

        poseStack.popPose();
    }


    private void renderRopeWithPhysics(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       Vec3 start, Vec3 end, double maxLength,
                                       int segments, float ropeWidth, float partialTick, Level level) {
        if (segments <= 0 || ropeWidth <= 0) return;

        Matrix4f matrix = poseStack.last().pose();
        Vec3 direction = end.subtract(start);
        double actualLength = direction.length();
        if (actualLength < 0.005) return;

        direction = direction.normalize();
        double tension = Math.min(actualLength / Math.max(maxLength, 1.0), 1.0);

        double sagAmount = ROPE_SAG_FACTOR * (1.0 - tension * 0.7) * actualLength * 0.25;

        float gameTime = (float) (System.currentTimeMillis() % 300000) / 3000.0f; // Much slower
        float windOffset = (float) Math.sin(gameTime * 0.3) * WIND_STRENGTH * 0.3f; // Much gentler

        Minecraft mc = Minecraft.getInstance();
        org.joml.Vector3f lookVector = mc.gameRenderer.getMainCamera().getLookVector();
        Vec3 cameraLook = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());

        int actualSegments = Mth.clamp(segments, 16, 48);

        for (int i = 0; i < actualSegments; i++) {
            float t1 = (float) i / actualSegments;
            float t2 = (float) (i + 1) / actualSegments;

            Vec3 pos1 = calculateSmoothCatenaryPosition(start, end, t1, sagAmount, windOffset, gameTime);
            Vec3 pos2 = calculateSmoothCatenaryPosition(start, end, t2, sagAmount, windOffset, gameTime);

            Vec3 segmentDir = pos2.subtract(pos1);
            double segmentLength = segmentDir.length();
            if (segmentLength < 0.0001) continue;

            segmentDir = segmentDir.normalize();
            Vec3 right = segmentDir.cross(cameraLook).normalize();

            if (right.length() < 0.1) {
                right = segmentDir.cross(new Vec3(0, 1, 0)).normalize();
                if (right.length() < 0.1) {
                    right = new Vec3(1, 0, 0);
                }
            }

            int layers = 6;
            for (int layer = 0; layer < layers; layer++) {
                float angle = (float) (layer * Math.PI * 2.0 / layers);
                Vec3 rotatedRight = rotateVectorAroundAxis(right, segmentDir, angle);
                Vec3 offset = rotatedRight.scale(ropeWidth * 0.5);

                Vec3 vert1 = pos1.subtract(offset);
                Vec3 vert2 = pos1.add(offset);
                Vec3 vert3 = pos2.add(offset);
                Vec3 vert4 = pos2.subtract(offset);

                float u1 = t1 * 2.0f;
                float u2 = t2 * 2.0f;
                float vCoord1 = (float) layer / layers;
                float vCoord2 = (float) (layer + 1) / layers;

                int light = calculateDynamicLighting(pos1, pos2);
                Vec3 normal = calculateSegmentNormal(segmentDir, rotatedRight);

                // Always add vertices - no additional validation
                addRopeVertex(vertexConsumer, matrix, vert1, u1, vCoord1, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert2, u1, vCoord2, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert3, u2, vCoord2, light, normal);

                addRopeVertex(vertexConsumer, matrix, vert1, u1, vCoord1, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert3, u2, vCoord2, light, normal);
                addRopeVertex(vertexConsumer, matrix, vert4, u2, vCoord1, light, normal);
            }
        }
    }

    private Vec3 calculateRopeEndPosition(RopePulleyBlockEntity blockEntity, Vec3 ropeStart,
                                          double ropeLength, float partialTick) {
        Vec3[] physicsPositions = blockEntity.getPhysicsRopePositions();
        if (physicsPositions != null && physicsPositions.length >= 2) {
            BlockPos pulleyPos = blockEntity.getBlockPos();
            Vec3 lastPos = physicsPositions[physicsPositions.length - 1];
            return new Vec3(
                    lastPos.x - pulleyPos.getX() - 0.5,
                    lastPos.y - pulleyPos.getY() - 0.5,
                    lastPos.z - pulleyPos.getZ() - 0.5
            );
        }

        Vec3 constraintEndPos = getConstraintEndPosition(blockEntity, partialTick);
        if (constraintEndPos != null) {
            return constraintEndPos;
        }

        if (blockEntity.hasAnchor() && blockEntity.getAnchorPoint() != null) {
            BlockPos anchorPos = blockEntity.getAnchorPoint();
            BlockPos pulleyPos = blockEntity.getBlockPos();
            Level level = blockEntity.getLevel();

            if (level instanceof ServerLevel serverLevel) {
                Vec3 relativeAnchorPos = calculateShipAwareRelativePosition(
                        serverLevel, pulleyPos, anchorPos, partialTick);

                if (relativeAnchorPos != null) {
                    double distanceToAnchor = relativeAnchorPos.length();

                    if (ropeLength <= distanceToAnchor - 0.05) {
                        return new Vec3(ropeStart.x, ropeStart.y - ropeLength, ropeStart.z);
                    } else if (ropeLength >= distanceToAnchor + 0.05) {
                        double extraLength = ropeLength - distanceToAnchor;
                        return relativeAnchorPos.add(0, -extraLength, 0);
                    } else {
                        return relativeAnchorPos;
                    }
                }
            }

            Vec3 anchorDirection = new Vec3(
                    anchorPos.getX() - pulleyPos.getX(),
                    anchorPos.getY() - pulleyPos.getY() + 0.5,
                    anchorPos.getZ() - pulleyPos.getZ()
            );

            double distanceToAnchor = anchorDirection.length();

            if (ropeLength <= distanceToAnchor - 0.05) {
                return new Vec3(ropeStart.x, ropeStart.y - ropeLength, ropeStart.z);
            } else if (ropeLength >= distanceToAnchor + 0.05) {
                double extraLength = ropeLength - distanceToAnchor;
                return anchorDirection.add(0, -extraLength, 0);
            } else {
                return anchorDirection;
            }
        }

        return new Vec3(ropeStart.x, ropeStart.y - ropeLength, ropeStart.z);
    }

    private Vec3 calculateShipAwareRelativePosition(ServerLevel level, BlockPos pulleyPos,
                                                    BlockPos anchorPos, float partialTick) {
        try {
            Ship pulleyShip = VSGameUtilsKt.getShipObjectManagingPos(level, pulleyPos);
            Ship anchorShip = VSGameUtilsKt.getShipObjectManagingPos(level, anchorPos);

            Vector3d pulleyWorldPos = getWorldPositionForRendering(level, pulleyPos, pulleyShip, partialTick);
            Vector3d anchorWorldPos = getWorldPositionForRendering(level, anchorPos, anchorShip, partialTick);

            anchorWorldPos.add(0, 0.5, 0);

            Vector3d relativeWorld = new Vector3d(anchorWorldPos).sub(pulleyWorldPos);

            if (pulleyShip != null) {
                Vector3d relativeShip = new Vector3d();
                pulleyShip.getTransform().getWorldToShip().transformDirection(relativeWorld, relativeShip);
                return new Vec3(relativeShip.x, relativeShip.y, relativeShip.z);
            } else {
                return new Vec3(relativeWorld.x, relativeWorld.y, relativeWorld.z);
            }

        } catch (Exception e) {
            return null;
        }
    }
    private Vector3d getWorldPositionForRendering(ServerLevel level, BlockPos pos, Ship ship, float partialTick) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (ship != null) {
            Vector3d worldPos = new Vector3d();

            ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
            return worldPos;
        } else {
            return localPos;
        }
    }

    private Vec3 calculateSmoothCatenaryPosition(Vec3 start, Vec3 end, float t,
                                                 double sagAmount, float windOffset, float gameTime) {
        Vec3 linearPos = start.lerp(end, t);

        double sagCurve = Math.sin(t * Math.PI) * sagAmount * 2.0;

        double windSway = Math.sin((gameTime + t) * 0.8) * windOffset * sagAmount * 0.2;

        sagCurve = Mth.clamp(sagCurve, -sagAmount * 3, 0);
        windSway = Mth.clamp(windSway, -0.1, 0.1);

        return linearPos.add(windSway, sagCurve, windSway * 0.2);
    }
    private boolean isPositionReasonable(Vec3 pos, Vec3 reference, double maxDistance) {
        if (pos == null || reference == null) return false;
        double distance = pos.distanceTo(reference);
        return distance <= maxDistance && !Double.isNaN(distance) && !Double.isInfinite(distance);
    }


    private Vec3 calculateSegmentNormal(Vec3 segmentDir, Vec3 right) {
        Vec3 up = segmentDir.cross(right).normalize();
        return right.cross(up).normalize();
    }

    private void addRopeVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos,
                               float u, float v, int light, Vec3 normal) {
        consumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(200, 180, 140, 255) // Rope color from advanced renderer
                .uv(u, v)
                .overlayCoords(0)
                .uv2(light)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    private Vec3 rotateVectorAroundAxis(Vec3 vector, Vec3 axis, float angle) {
        Vec3 k = axis.normalize();
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        Vec3 vCrossK = vector.cross(k);
        double vDotK = vector.dot(k);

        return vector.scale(cosAngle)
                .add(vCrossK.scale(sinAngle))
                .add(k.scale(vDotK * (1 - cosAngle)));
    }

    private int calculateDynamicLighting(Vec3 pos1, Vec3 pos2) {
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

    private void renderPulleyMechanism(RopePulleyBlockEntity blockEntity, float partialTick,
                                       PoseStack poseStack, MultiBufferSource bufferSource,
                                       int packedLight, int packedOverlay) {

    }

    private Long getShipIdAtPos(ServerLevel level, BlockPos pos) {
        try {
            Ship shipObject = VSGameUtilsKt.getShipObjectManagingPos(level, pos);
            return shipObject != null ? shipObject.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }




    private Vector3d getWorldPosition(ServerLevel level, BlockPos pos, Long shipId) {
        Vector3d localPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (shipId != null) {
            Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level).getDimensionToGroundBodyIdImmutable()
                    .get(VSGameUtilsKt.getDimensionId(level));
            if (!shipId.equals(groundBodyId)) {
                Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
                if (shipObject != null) {
                    Vector3d worldPos = new Vector3d();
                    shipObject.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
                    return worldPos;
                }
            }
        }
        return localPos;
    }

    private Vec3 getConstraintEndPosition(RopePulleyBlockEntity blockEntity, float partialTick) {
        return null;
    }

    public static void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        positionCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue().lastUpdateTime > 10000); // Remove after 10 seconds
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRender(RopePulleyBlockEntity blockEntity, Vec3 cameraPos) {
        return blockEntity.getCurrentRopeLength() > 0.005;
    }

    @Override
    public boolean shouldRenderOffScreen(RopePulleyBlockEntity blockEntity) {
        return blockEntity.getCurrentRopeLength() > 1.0;
    }


    private Vec3 calculateCollisionAwarePosition(Vec3 start, Vec3 end, float t, double sagAmount,
                                                 float windOffset, Level level, Vec3 cameraPos) {
        Vec3 basePos = calculateSmoothCatenaryPosition(start, end, t, sagAmount, windOffset,
                (float) (System.currentTimeMillis() % 100000) / 1000.0f);
        Vec3 worldPos = basePos.add(cameraPos);

        if (level != null) {
            try {
                net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                        (int)worldPos.x, (int)worldPos.y, (int)worldPos.z);
                if (!level.getBlockState(blockPos).isAir()) {
                    return basePos.add(0, 1.0, 0);
                }
            } catch (Exception e) {
                // abcehfksahdjfskhfkl
            }
        }
        return basePos;
    }

    private int calculateAlpha(Vec3 pos1, Vec3 pos2, double tension) {
        double avgDistance = (pos1.length() + pos2.length()) * 0.5;
        double distanceAlpha = Math.max(0.4, 1.0 - (avgDistance / MAX_RENDER_DISTANCE));
        double tensionAlpha = 0.7 + (tension * 0.3);
        return (int) Mth.clamp(255 * distanceAlpha * tensionAlpha, 100, 255);
    }
}











