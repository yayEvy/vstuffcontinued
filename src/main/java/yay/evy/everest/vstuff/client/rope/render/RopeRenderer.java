package yay.evy.everest.vstuff.client.rope.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.client.rope.ClientRope;
import yay.evy.everest.vstuff.client.rope.ClientRopeManager;
import yay.evy.everest.vstuff.internal.utility.RopeRenderUtils;
import yay.evy.everest.vstuff.internal.utility.RopeUtils;

import java.util.HashMap;
import java.util.Map;

public class RopeRenderer {

    private static final Map<Integer, Pair<Vector3d,Vector3d>> previousStartRelativeAndEndRelativeVectors = new HashMap<>();

    public static void render(RenderLevelStageEvent event) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level == null) return;
            if (!level.isClientSide) return;

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            Vec3 cameraPos = event.getCamera().getPosition();
            float partialTick = event.getPartialTick();

            boolean renderedAny = false;

            for (Map.Entry<Integer, ClientRope> entry : ClientRopeManager.getClientRopes().entrySet()) {
                try {
                    boolean rendered = renderClientRope(
                            poseStack, bufferSource, entry.getValue(),
                            level, cameraPos, partialTick, entry.getKey()
                    );
                    renderedAny = !renderedAny && rendered;
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


    private static boolean renderClientRope(PoseStack poseStack, MultiBufferSource bufferSource, ClientRope rope,
                                            Level level, Vec3 cameraPos, float partialTick, int ropeId) {
        if (!rope.canRender(level)) return false;

        Vector3d startPos = RopeUtils.renderLocalToWorld(level, rope.localPos0, rope.ship0);
        Vector3d endPos   = RopeUtils.renderLocalToWorld(level, rope.localPos1, rope.ship1);

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

        IRopeRenderer renderer = rope.getStyle().createRenderer();
        if (renderer == null) return false;

        double actualLength = startPos.distance(endPos);
        double maxLength    = rope.getLength();

        float stableGameTime = (level.getGameTime() + partialTick) / 20.0f;

        double sagAmount  = RopeRenderUtils.computeSag(actualLength, maxLength);
        float  windOffset = RopeRenderUtils.computeWindOffset(stableGameTime);

        Vector3d[] curve  = RopeRenderUtils.computeCurve(startRelative, endRelative, sagAmount, windOffset, stableGameTime);
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
