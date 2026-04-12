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
import yay.evy.everest.vstuff.internal.rendering.IRopeRenderer;
import yay.evy.everest.vstuff.internal.rendering.RopeRenderContext;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.styling.RopeStyleManager;
import yay.evy.everest.vstuff.internal.utility.RopeRenderUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientConstraints = new HashMap<>();
    private static final Map<Integer, Pair<Vector3d,Vector3d>> previousStartRelativeAndEndRelativeVectors = new HashMap<>();

    // phys ropes :3dsmile:
    private static final Map<Integer, List<PhysRopePoint>> physRopeSegments = new HashMap<>();
    private static final Map<Integer, List<PhysRopePoint>> prevPhysRopeSegments = new HashMap<>();
    private static final Map<Integer, List<Vector3d>> physRopeSegmentVelocities = new HashMap<>();
    private static final Map<Integer, Long> physRopeLastUpdateTime = new HashMap<>();
    public static final int PHYS_ROPE_ID_START = 100_000;
    public record PhysRopePoint(Vector3d localPos, Long shipId) {}


    public record ClientRopeData(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, RopeStyle style) {

        public ClientRopeData(Long ship0, Long ship1, Vector3d localPos0, Vector3d localPos1, double maxLength, RopeStyle style) {
            this.ship0 = ship0;
            this.ship1 = ship1;
            this.localPos0 = new Vector3d(localPos0);
            this.localPos1 = new Vector3d(localPos1);
            this.maxLength = maxLength;
            this.style = style;
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
            return new ClientRopeData(ship0, ship1, localPos0, localPos1, newLength, style);
        }

        public ClientRopeData withStyle(RopeStyle newStyle) {
            return new ClientRopeData(ship0, ship1, localPos0, localPos1, maxLength, newStyle);
        }

    }

    public static void updateClientRopeLength(Integer ropeId, double length) {
        clientConstraints.computeIfPresent(ropeId, (k, ropeData) -> ropeData.withLength(length));
    }

    public static void updateClientRopeStyle(Integer ropeId, RopeStyle style) {
        clientConstraints.computeIfPresent(ropeId, (k, ropeData) -> ropeData.withStyle(style));
    }

    public static void addClientConstraint(Integer constraintId, Long shipA, Long shipB,
                                           Vector3d localPosA, Vector3d localPosB, double maxLength, RopeStyle style) {
        clientConstraints.put(constraintId, new ClientRopeData(shipA, shipB, localPosA, localPosB, maxLength, style));
    }

    public static void removeClientConstraint(Integer constraintId) {
        if (constraintId == null) return;

        clientConstraints.remove(constraintId);
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
                        VStuff.LOGGER.error("Error occurred rendering rope {}: {}: {}", entry.getKey(), e.getClass(), e.getMessage());
                        e.printStackTrace();
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

            ResourceLocation ropeTypeId = ropeData.style().id();
            RopeStyle ropeType = RopeStyleManager.get(ropeTypeId);
            if (ropeType == null) return false;

            IRopeRenderer renderer = RopeRendererTypes.getOrCreate(
                    ropeTypeId, ropeType.rendererTypeId(), ropeType.rendererParams());
            if (renderer == null) return false;

            double actualLength = startPos.distance(endPos);
            double maxLength    = ropeData.maxLength();

            float stableGameTime = (level.getGameTime() + partialTick) / 20.0f;

            Vector3d[] curve;
            List<PhysRopePoint> physPoints = getInterpolatedPhysRopeSegments(ropeId, partialTick, level);
            if (physPoints != null && physPoints.size() >= 2) {
                List<Vector3d> worldSegments = resolvePhysSegmentsToWorld(physPoints, level);
                curve = interpolateSegmentsToCurve(worldSegments, cameraPos, startRelative, endRelative);
            } else {
                double sagAmount  = RopeRenderUtils.computeSag(actualLength, maxLength);
                float  windOffset = RopeRenderUtils.computeWindOffset(stableGameTime);
                curve = RopeRenderUtils.computeCurve(startRelative, endRelative, sagAmount, windOffset, stableGameTime);
            }

            int[] light = RopeRenderUtils.computeLighting(curve, level, cameraPos);

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

    private static Vector3d[] interpolateSegmentsToCurve(List<Vector3d> worldPoints, Vec3 cameraPos,
                                                         Vector3d startRelative, Vector3d endRelative) {
        List<Vector3d> pts = new ArrayList<>();
        pts.add(new Vector3d(startRelative.x + cameraPos.x,
                startRelative.y + cameraPos.y,
                startRelative.z + cameraPos.z));
        pts.addAll(worldPoints);
        pts.add(new Vector3d(endRelative.x + cameraPos.x,
                endRelative.y + cameraPos.y,
                endRelative.z + cameraPos.z));

        Vector3d first = pts.get(0);
        Vector3d second = pts.get(1);
        Vector3d secondLast = pts.get(pts.size() - 2);
        Vector3d last = pts.get(pts.size() - 1);
        pts.add(0, new Vector3d(2*first.x - second.x, 2*first.y - second.y, 2*first.z - second.z));
        pts.add(new Vector3d(2*last.x - secondLast.x, 2*last.y - secondLast.y, 2*last.z - secondLast.z));

        Vector3d[] curve = new Vector3d[RopeRenderUtils.ROPE_CURVE_SEGMENTS + 1];
        int segments = pts.size() - 3;

        for (int i = 0; i <= RopeRenderUtils.ROPE_CURVE_SEGMENTS; i++) {
            float t = (float) i / RopeRenderUtils.ROPE_CURVE_SEGMENTS;
            float scaled = t * segments;
            int span = Math.min((int) scaled, segments - 1);
            float localT = scaled - span;

            Vector3d p0 = pts.get(span);
            Vector3d p1 = pts.get(span + 1);
            Vector3d p2 = pts.get(span + 2);
            Vector3d p3 = pts.get(span + 3);

            curve[i] = catmullRom(p0, p1, p2, p3, localT, cameraPos);
        }
        return curve;
    }


    private static Vector3d catmullRom(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d p3,
                                       float t, Vec3 cameraPos) {
        float t2 = t * t;
        float t3 = t2 * t;
        double x = 0.5 * ((2*p1.x) + (-p0.x + p2.x)*t + (2*p0.x - 5*p1.x + 4*p2.x - p3.x)*t2 + (-p0.x + 3*p1.x - 3*p2.x + p3.x)*t3);
        double y = 0.5 * ((2*p1.y) + (-p0.y + p2.y)*t + (2*p0.y - 5*p1.y + 4*p2.y - p3.y)*t2 + (-p0.y + 3*p1.y - 3*p2.y + p3.y)*t3);
        double z = 0.5 * ((2*p1.z) + (-p0.z + p2.z)*t + (2*p0.z - 5*p1.z + 4*p2.z - p3.z)*t2 + (-p0.z + 3*p1.z - 3*p2.z + p3.z)*t3);
        return new Vector3d(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
    }

    public static void updatePhysRopeSegments(int ropeId, List<Vector3d> positions, Level level) {
        long tick = level.getGameTime();

        List<PhysRopePoint> current = physRopeSegments.get(ropeId);
        if (current != null) {
            prevPhysRopeSegments.put(ropeId, current);
        }

        List<PhysRopePoint> newData = new ArrayList<>();
        for (Vector3d pos : positions) {
            newData.add(new PhysRopePoint(pos, null));
        }

        physRopeSegments.put(ropeId, newData);
        physRopeLastUpdateTime.put(ropeId, tick);
    }

    public static List<PhysRopePoint> getPhysRopeSegments(int ropeId) {
        return physRopeSegments.get(ropeId);
    }

    private static List<Vector3d> resolvePhysSegmentsToWorld(List<PhysRopePoint> points, Level level) {
        List<Vector3d> world = new ArrayList<>(points.size());
        for (PhysRopePoint point : points) {
            world.add(RopeUtils.renderLocalToWorld(level, point.localPos(), point.shipId()));
        }
        return world;
    }

    public static List<PhysRopePoint> getInterpolatedPhysRopeSegments(int ropeId, float partialTick, Level level) {
        List<PhysRopePoint> current = physRopeSegments.get(ropeId);
        List<PhysRopePoint> previous = prevPhysRopeSegments.get(ropeId);
        Long lastUpdateTick = physRopeLastUpdateTime.get(ropeId);

        if (current == null) return null;
        if (previous == null || previous.size() != current.size() || lastUpdateTick == null) {
            return current;
        }

        float ticksSinceUpdate = (float)(level.getGameTime() - lastUpdateTick);
        float lerpAlpha = ticksSinceUpdate + partialTick;
        lerpAlpha = Math.min(lerpAlpha, 1.0f);

        List<PhysRopePoint> result = new ArrayList<>(current.size());
        for (int i = 0; i < current.size(); i++) {
            Vector3d from = previous.get(i).localPos();
            Vector3d to = current.get(i).localPos();

            Vector3d blendedPos = new Vector3d(
                    from.x + (to.x - from.x) * lerpAlpha,
                    from.y + (to.y - from.y) * lerpAlpha,
                    from.z + (to.z - from.z) * lerpAlpha
            );

            result.add(new PhysRopePoint(blendedPos, current.get(i).shipId()));
        }
        return result;
    }
    public static void clearNormalRopeConstraints() {
        clientConstraints.keySet().removeIf(id -> id < PHYS_ROPE_ID_START);
    }
}