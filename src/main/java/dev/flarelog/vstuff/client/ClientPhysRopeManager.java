package dev.flarelog.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.infrastructure.registry.VStuffRegistries;
import dev.flarelog.vstuff.internal.rendering.IRopeRenderer;
import dev.flarelog.vstuff.internal.rendering.RopeRenderContext;
import dev.flarelog.vstuff.internal.styling.data.RopeStyle;
import dev.flarelog.vstuff.internal.utility.RopeRenderUtils;
import dev.flarelog.vstuff.internal.utility.records.RopeSegment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPhysRopeManager {
    private static final Map<Integer, ClientPhysRope> clientConstraints = new ConcurrentHashMap<>();
    private static final Map<Integer, Pair<Vector3d,Vector3d>> prevStartEndRelativeVectors = new ConcurrentHashMap<>();

    public static void addClientConstraint(Integer id, Vector3d pos0, Vector3d pos1, List<RopeSegment> segments, ResourceKey<RopeStyle> style) {
        clientConstraints.put(id, new ClientPhysRope(id, pos0, pos1, segments, style));
    }

    public static void removeClientConstraint(Integer constraintId) {
        if (constraintId == null) return;
        clientConstraints.remove(constraintId);
    }

    public static Map<Integer, ClientPhysRope> getClientConstraints() {
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
                ClientLevel level = mc.level;
                if (level == null) return;

                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                Vec3 cameraPos = event.getCamera().getPosition();
                float partialTick = event.getPartialTick();

                boolean renderedAny = false;

                for (Map.Entry<Integer, ClientPhysRope> entry : getClientConstraints().entrySet()) {
                    try {
                        boolean rendered = renderClientRope(level,
                                poseStack, bufferSource, entry.getValue(),
                                cameraPos, partialTick, entry.getKey()
                        );
                        if (rendered) renderedAny = true;
                    } catch (Exception e) {
                        VStuff.LOGGER.error("Error occurred rendering rope {}: {}", entry.getKey(), e.getMessage());
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


        private static boolean renderClientRope(ClientLevel level, PoseStack poseStack, MultiBufferSource bufferSource,
                                                ClientPhysRope rope, Vec3 cameraPos, float partialTick, int ropeId) {
            if (!level.isClientSide) return false;

            RopeStyle style = getClientStyle(level, rope.styleKey());
            if (style == null) return false;

            IRopeRenderer renderer = style.createRenderer();
            if (renderer == null) return false;

            for (RopeSegment seg : rope.segments()) {

                Vector3d startPos = seg.pos0(level);
                Vector3d endPos = seg.pos1(level);

                if (VSGameUtilsKt.isBlockInShipyard(level, startPos.x, startPos.y, startPos.z)) return false;
                if (VSGameUtilsKt.isBlockInShipyard(level, endPos.x, endPos.y, endPos.z)) return false;

                Vector3d startRelative = new Vector3d(startPos.x - cameraPos.x, startPos.y - cameraPos.y, startPos.z - cameraPos.z);
                Vector3d endRelative = new Vector3d(endPos.x - cameraPos.x, endPos.y - cameraPos.y, endPos.z - cameraPos.z);

                int renderChunks = Minecraft.getInstance().options.renderDistance().get();
                double maxRenderDistSq = (renderChunks * 16d) * (renderChunks * 16d);
                if (startRelative.lengthSquared() > maxRenderDistSq && endRelative.lengthSquared() > maxRenderDistSq) return false;

                if (startRelative.distance(endRelative) < 0.1) return false;

                double actualLength = startPos.distance(endPos);

                float stableGameTime = (level.getGameTime() + partialTick) / 20.0f;

                float windOffset = RopeRenderUtils.computeWindOffset(stableGameTime);

                Vector3d[] curve = RopeRenderUtils.computeCurve(startRelative, endRelative, 0, windOffset, stableGameTime);
                int[] light = RopeRenderUtils.computeLighting(curve, level, cameraPos);

                Pair<Vector3d, Vector3d> prevStartRelativeAndEndRelative = prevStartEndRelativeVectors
                        .computeIfAbsent(ropeId, (id) -> new Pair<>(startRelative, endRelative));

                RopeRenderContext ctx = new RopeRenderContext(
                        startRelative, endRelative,
                        prevStartRelativeAndEndRelative.getFirst(), prevStartRelativeAndEndRelative.getSecond(),
                        actualLength, partialTick, level,
                        BlockPos.containing(startPos.x, startPos.y, startPos.z),
                        BlockPos.containing(endPos.x, endPos.y, endPos.z)
                );

                prevStartEndRelativeVectors.put(ropeId, new Pair<>(startRelative, endRelative));

                poseStack.pushPose();
                renderer.render(ctx, poseStack, bufferSource, curve, light);
                poseStack.popPose();
            }

            return true;
        }
    }

    private static RopeStyle getClientStyle(ClientLevel level, ResourceKey<RopeStyle> styleKey) {
        Registry<RopeStyle> registry = level.registryAccess().registryOrThrow(VStuffRegistries.ROPE_STYLE);

        return registry.get(styleKey);
    }
}