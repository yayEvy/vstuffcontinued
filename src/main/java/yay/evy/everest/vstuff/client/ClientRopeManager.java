package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.rope.IRopeRenderer;
import yay.evy.everest.vstuff.client.rope.RopeRenderContext;
import yay.evy.everest.vstuff.client.rope.RopeRendererTypes;
import yay.evy.everest.vstuff.content.ropes.type.RopeType;
import yay.evy.everest.vstuff.content.ropes.type.RopeTypeManager;
import yay.evy.everest.vstuff.internal.utility.RopeRenderUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientConstraints = new HashMap<>();
    private static final Map<Integer, Pair<Vector3d,Vector3d>> previousStartRelativeAndEndRelativeVectors = new HashMap<>();

    private static final Map<Integer, List<Vector3d>> physRopeSegments = new HashMap<>();
    private static final Map<Integer, List<Vector3d>> prevPhysRopeSegments = new HashMap<>();
    private static final Map<Integer, List<Vector3d>> physRopeSegmentVelocities = new HashMap<>();
    private static final Map<Integer, Long> physRopeLastUpdateTime = new HashMap<>();
    public static final int PHYS_ROPE_ID_START = 100_000;


    public record ClientRopeData(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, RopeType type) {

            public ClientRopeData(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, RopeType type) {
                this.ship0 = ship0;
                this.ship1 = ship1;
                this.localPos0 = new Vector3d(localPos0);
                this.localPos1 = new Vector3d(localPos1);
                this.maxLength = maxLength;
                this.type = type;
            }

        public boolean canRender(Level level) {
            if (level == null) return false;

            var shipWorld = VSGameUtilsKt.getShipObjectWorld(level);

            if (ship0 != null && ship0 != 0L) {
                Ship a = shipWorld.getAllShips().getById(ship0);
                return a instanceof ClientShip;
            }

            if (ship1 != null && ship1 != 0L) {
                Ship b = shipWorld.getAllShips().getById(ship1);
                return b instanceof ClientShip;
            }

            return true;
        }

        public ClientRopeData withLength(double newLength) {
            return new ClientRopeData(ship0, ship1, localPos0, localPos1, newLength, type);
        }

        public ClientRopeData withStyle(RopeType newType) {
            return new ClientRopeData(ship0, ship1, localPos0, localPos1, maxLength, newType);
        }

    }

    public static void updateClientRopeLength(Integer ropeId, double length) {
        clientConstraints.computeIfPresent(ropeId, (k, ropeData) -> ropeData.withLength(length));
    }

    public static void updateClientRopeStyle(Integer ropeId, RopeType type) {
        clientConstraints.computeIfPresent(ropeId, (k, ropeData) -> ropeData.withStyle(type));
    }

    public static void addClientConstraint(Integer constraintId, Long shipA, Long shipB,
                                           Vector3d localPosA, Vector3d localPosB, double maxLength, RopeType type) {
        clientConstraints.put(constraintId, new ClientRopeData(shipA, shipB, localPosA, localPosB, maxLength, type));
    }

    public static void removeClientConstraint(Integer constraintId) {
        if (constraintId == null) return;
        clientConstraints.remove(constraintId);
        physRopeSegments.remove(constraintId);
        prevPhysRopeSegments.remove(constraintId);
        physRopeSegmentVelocities.remove(constraintId);
        physRopeLastUpdateTime.remove(constraintId);
    }

    public static Map<Integer, ClientRopeData> getClientConstraints() {
        return clientConstraints;
    }

    public static void clearAllClientConstraints() {
        clientConstraints.clear();
    }

    @Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class Renderer {

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

            try {
                Minecraft mc = Minecraft.getInstance();
                Level level = mc.level;
                if (level == null) return;

                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                Vec3 cameraPos = event.getCamera().getPosition();
                float partialTick = event.getPartialTick();

                boolean renderedAny = false;

                for (Map.Entry<Integer, ClientRopeData> entry : getClientConstraints().entrySet()) {
                    try {
                        boolean rendered = renderClientRope(
                                poseStack, bufferSource, entry.getValue(),
                                level, cameraPos, partialTick, entry.getKey()
                        );
                        if (rendered) renderedAny = true;
                    } catch (Exception e) {
                        VStuff.LOGGER.error("Error rendering rope {}: {}", entry.getKey(), e.getMessage());
                    }
                }

                if (renderedAny) {
                    bufferSource.endBatch();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private static boolean renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource,
                                                ClientRopeData ropeData,
                                                Level level, Vec3 cameraPos, float partialTick, int ropeId) {
            if (!level.isClientSide) return false;
            if (!ropeData.canRender(level)) return false;

            Vector3d startPos = RopeUtils.renderLocalToWorld(level, ropeData.localPos0(), ropeData.ship0());
            Vector3d endPos   = RopeUtils.renderLocalToWorld(level, ropeData.localPos1(), ropeData.ship1());

            if (VSGameUtilsKt.isBlockInShipyard(level, startPos.x, startPos.y, startPos.z)) return false;
            if (VSGameUtilsKt.isBlockInShipyard(level, endPos.x,   endPos.y,   endPos.z))   return false;

            Vector3d startRelative = new Vector3d(
                    startPos.x - cameraPos.x, startPos.y - cameraPos.y, startPos.z - cameraPos.z);
            Vector3d endRelative = new Vector3d(
                    endPos.x - cameraPos.x, endPos.y - cameraPos.y, endPos.z - cameraPos.z);

            int renderChunks = Minecraft.getInstance().options.renderDistance().get();
            double maxRenderDistSq = (renderChunks * 16d) * (renderChunks * 16d);
            if (startRelative.lengthSquared() > maxRenderDistSq
                    && endRelative.lengthSquared() > maxRenderDistSq) return false;

            if (startRelative.distance(endRelative) < 0.1) return false;

            ResourceLocation ropeTypeId = ropeData.type().id();
            RopeType ropeType = RopeTypeManager.get(ropeTypeId);
            if (ropeType == null) return false;

            IRopeRenderer renderer = RopeRendererTypes.getOrCreate(
                    ropeTypeId, ropeType.rendererTypeId(), ropeType.rendererParams());
            if (renderer == null) return false;

            double actualLength = startPos.distance(endPos);
            double maxLength    = ropeData.maxLength();

            float stableGameTime = (level.getGameTime() + partialTick) / 20.0f;

            Vector3d[] curve;
            List<Vector3d> physSegments = getInterpolatedPhysRopeSegments(ropeId, partialTick);
            if (physSegments != null && physSegments.size() >= 2) {
                curve = interpolateSegmentsToCurve(physSegments, cameraPos, startRelative, endRelative);
            } else {
                double sagAmount  = RopeRenderUtils.computeSag(actualLength, maxLength);
                float  windOffset = RopeRenderUtils.computeWindOffset(stableGameTime);
                curve = RopeRenderUtils.computeCurve(startRelative, endRelative, sagAmount, windOffset, stableGameTime);
            }
            int[] light  = RopeRenderUtils.computeLighting(curve, level, cameraPos);

            Pair<Vector3d,Vector3d> prevStartRelativeAndEndRelative = previousStartRelativeAndEndRelativeVectors.computeIfAbsent(ropeId, (id) -> new Pair<>(startRelative, endRelative));

            RopeRenderContext ctx = new RopeRenderContext(
                    startRelative, endRelative, prevStartRelativeAndEndRelative.getFirst(), prevStartRelativeAndEndRelative.getSecond(),
                    maxLength, actualLength, partialTick, level,
                    new net.minecraft.core.BlockPos((int) Math.floor(startPos.x), (int) Math.floor(startPos.y), (int) Math.floor(startPos.z)),
                    new net.minecraft.core.BlockPos((int) Math.floor(endPos.x),   (int) Math.floor(endPos.y),   (int) Math.floor(endPos.z))
            );

            previousStartRelativeAndEndRelativeVectors.put(ropeId, new Pair<>(startRelative, endRelative));

            poseStack.pushPose();
            renderer.render(ctx, poseStack, bufferSource, curve, light);
            poseStack.popPose();

            return true;
        }
    }

    private static Vector3d[] interpolateSegmentsToCurve(List<Vector3d> segments, Vec3 cameraPos, Vector3d startRelative, Vector3d endRelative) {
        List<Vector3d> worldPoints = new ArrayList<>();
        worldPoints.add(new Vector3d(startRelative.x + cameraPos.x, startRelative.y + cameraPos.y, startRelative.z + cameraPos.z));
        worldPoints.addAll(segments);
        worldPoints.add(new Vector3d(endRelative.x + cameraPos.x, endRelative.y + cameraPos.y, endRelative.z + cameraPos.z));

        Vector3d[] curve = new Vector3d[RopeRenderUtils.ROPE_CURVE_SEGMENTS + 1];
        int n = worldPoints.size() - 1;
        for (int i = 0; i <= RopeRenderUtils.ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / RopeRenderUtils.ROPE_CURVE_SEGMENTS;
            float scaled = t * n;
            int idx = (int) scaled;
            float frac = scaled - idx;
            Vector3d a = worldPoints.get(Math.min(idx, n));
            Vector3d b = worldPoints.get(Math.min(idx + 1, n));
            curve[i] = new Vector3d(
                    a.x + (b.x - a.x) * frac - cameraPos.x,
                    a.y + (b.y - a.y) * frac - cameraPos.y,
                    a.z + (b.z - a.z) * frac - cameraPos.z
            );
        }
        return curve;
    }
    public static void updatePhysRopeSegments(int ropeId, List<Vector3d> positions) {
        List<Vector3d> prev = physRopeSegments.get(ropeId);
        long now = System.currentTimeMillis();
        Long lastTime = physRopeLastUpdateTime.get(ropeId);

        if (prev != null && prev.size() == positions.size() && lastTime != null) {
            double dt = (now - lastTime) / 1000.0;
            double clampedDt = Math.min(dt, 0.1);
            if (clampedDt > 0) {
                List<Vector3d> velocities = new ArrayList<>(positions.size());
                for (int i = 0; i < positions.size(); i++) {
                    Vector3d a = prev.get(i);
                    Vector3d b = positions.get(i);
                    velocities.add(new Vector3d(
                            (b.x - a.x) / clampedDt,
                            (b.y - a.y) / clampedDt,
                            (b.z - a.z) / clampedDt
                    ));
                }
                physRopeSegmentVelocities.put(ropeId, velocities);
            }
            prevPhysRopeSegments.put(ropeId, prev);
        }

        physRopeSegments.put(ropeId, positions);
        physRopeLastUpdateTime.put(ropeId, now);
    }

    public static List<Vector3d> getPhysRopeSegments(int ropeId) {
        return physRopeSegments.get(ropeId);
    }

    public static List<Vector3d> getInterpolatedPhysRopeSegments(int ropeId, float partialTick) {
        List<Vector3d> current = physRopeSegments.get(ropeId);
        List<Vector3d> velocities = physRopeSegmentVelocities.get(ropeId);
        Long lastTime = physRopeLastUpdateTime.get(ropeId);
        if (current == null) return null;

        long now = System.currentTimeMillis();
        double timeSinceUpdate = lastTime != null ? (now - lastTime) / 1000.0 : 0;

        if (timeSinceUpdate > 0.1) return current;

        if (velocities == null || velocities.size() != current.size()) return current;

        List<Vector3d> result = new ArrayList<>(current.size());
        for (int i = 0; i < current.size(); i++) {
            Vector3d base = new Vector3d(current.get(i));
            Vector3d vel = velocities.get(i);
            base.add(vel.x * timeSinceUpdate, vel.y * timeSinceUpdate, vel.z * timeSinceUpdate);
            result.add(base);
        }
        return result;
    }
    public static void clearNormalRopeConstraints() {
        clientConstraints.keySet().removeIf(id -> id < PHYS_ROPE_ID_START);
    }
}