package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRopeManager {
    private static final Map<Integer, ClientRopeData> clientConstraints = new ConcurrentHashMap<>();
    private static final Map<Integer, Pair<Vector3d,Vector3d>> previousStartRelativeAndEndRelativeVectors = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> snapWorldPos0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> snapWorldPos1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> snapVelocity0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> snapVelocity1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> smoothedVelocity0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> smoothedVelocity1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> prevSnapWorldPos0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Vector3d> prevSnapWorldPos1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> snapGameTime0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> snapGameTime1 = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> lastSeq0 = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> lastSeq1 = new ConcurrentHashMap<>();

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
        snapWorldPos0.remove(constraintId);
        snapWorldPos1.remove(constraintId);
        prevSnapWorldPos0.remove(constraintId);  // was missing
        prevSnapWorldPos1.remove(constraintId);  // was missing
        snapVelocity0.remove(constraintId);
        snapVelocity1.remove(constraintId);
        snapGameTime0.remove(constraintId);
        snapGameTime1.remove(constraintId);
        smoothedVelocity0.remove(constraintId);
        smoothedVelocity1.remove(constraintId);
        lastSeq0.remove(constraintId);
        lastSeq1.remove(constraintId);
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

            long gameTime = level.getGameTime();
            Vector3d startPos = (ropeData.ship0() == null)
                    ? getInterpolatedPos0(ropeId, partialTick, gameTime)
                    : RopeUtils.renderLocalToWorld(level, ropeData.localPos0(), ropeData.ship0());

            Vector3d endPos = (ropeData.ship1() == null)
                    ? getInterpolatedPos1(ropeId, partialTick, gameTime)
                    : RopeUtils.renderLocalToWorld(level, ropeData.localPos1(), ropeData.ship1());

            if (VSGameUtilsKt.isBlockInShipyard(level, startPos.x, startPos.y, startPos.z)) return false;
            if (VSGameUtilsKt.isBlockInShipyard(level, endPos.x, endPos.y, endPos.z)) return false;

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
            double maxLength = ropeData.maxLength();

            float stableGameTime = (level.getGameTime() + partialTick) / 20.0f;

            double sagAmount = RopeRenderUtils.computeSag(actualLength, maxLength);
            float windOffset = RopeRenderUtils.computeWindOffset(stableGameTime);

            Vector3d[] curve = RopeRenderUtils.computeCurve(startRelative, endRelative, sagAmount, windOffset, stableGameTime);
            int[] light = RopeRenderUtils.computeLighting(curve, level, cameraPos);

            Pair<Vector3d, Vector3d> prevStartRelativeAndEndRelative = previousStartRelativeAndEndRelativeVectors
                    .computeIfAbsent(ropeId, (id) -> new Pair<>(startRelative, endRelative));

            RopeRenderContext ctx = new RopeRenderContext(
                    startRelative, endRelative,
                    prevStartRelativeAndEndRelative.getFirst(), prevStartRelativeAndEndRelative.getSecond(),
                    maxLength, actualLength, partialTick, level,
                    new net.minecraft.core.BlockPos((int) Math.floor(startPos.x), (int) Math.floor(startPos.y), (int) Math.floor(startPos.z)),
                    new net.minecraft.core.BlockPos((int) Math.floor(endPos.x), (int) Math.floor(endPos.y), (int) Math.floor(endPos.z))
            );

            previousStartRelativeAndEndRelativeVectors.put(ropeId, new Pair<>(startRelative, endRelative));

            poseStack.pushPose();
            renderer.render(ctx, poseStack, bufferSource, curve, light);
            poseStack.popPose();

            return true;
        }
    }
    public static void updateClientRopePositions(Integer ropeId, long sequence,
                                                 @Nullable Vector3d newPos0, @Nullable Vector3d newVel0,
                                                 @Nullable Vector3d newPos1, @Nullable Vector3d newVel1) {
        clientConstraints.computeIfPresent(ropeId, (k, ropeData) -> {

            if (newPos0 != null && sequence > lastSeq0.getOrDefault(ropeId, -1L)) {
                lastSeq0.put(ropeId, sequence);
                Vector3d prev = snapWorldPos0.get(ropeId);
                prevSnapWorldPos0.put(ropeId, prev != null ? new Vector3d(prev) : new Vector3d(newPos0));
                snapWorldPos0.put(ropeId, new Vector3d(newPos0));
                snapGameTime0.put(ropeId, Minecraft.getInstance().level.getGameTime() - 1);
            }

            if (newPos1 != null && sequence > lastSeq1.getOrDefault(ropeId, -1L)) {
                lastSeq1.put(ropeId, sequence);
                Vector3d prev = snapWorldPos1.get(ropeId);
                prevSnapWorldPos1.put(ropeId, prev != null ? new Vector3d(prev) : new Vector3d(newPos1));
                snapWorldPos1.put(ropeId, new Vector3d(newPos1));
                snapGameTime1.put(ropeId, Minecraft.getInstance().level.getGameTime() - 1);
            }

            Vector3d p0 = newPos0 != null ? new Vector3d(newPos0) : ropeData.localPos0();
            Vector3d p1 = newPos1 != null ? new Vector3d(newPos1) : ropeData.localPos1();

            return new ClientRopeData(ropeData.ship0(), ropeData.ship1(), p0, p1, ropeData.maxLength(), ropeData.style());
        });
    }
    public static Vector3d getInterpolatedPos0(Integer ropeId, float partialTick, long currentGameTime) {
        Vector3d snap = snapWorldPos0.get(ropeId);
        if (snap == null) return new Vector3d();

        Vector3d prev = prevSnapWorldPos0.getOrDefault(ropeId, snap);
        long snapTime = snapGameTime0.getOrDefault(ropeId, currentGameTime - 1);

        double ticksSinceSnap = (currentGameTime - snapTime - 1) + partialTick;
        double alpha = Mth.clamp(ticksSinceSnap, 0.0, 1.0);

        return prev.lerp(snap, alpha, new Vector3d());
    }

    public static Vector3d getInterpolatedPos1(Integer ropeId, float partialTick, long currentGameTime) {
        Vector3d snap = snapWorldPos1.get(ropeId);
        if (snap == null) return new Vector3d();

        Vector3d prev = prevSnapWorldPos1.getOrDefault(ropeId, snap);
        long snapTime = snapGameTime1.getOrDefault(ropeId, currentGameTime - 1);

        double ticksSinceSnap = (currentGameTime - snapTime - 1) + partialTick;
        double alpha = Mth.clamp(ticksSinceSnap, 0.0, 1.0);

        return prev.lerp(snap, alpha, new Vector3d());
    }
}