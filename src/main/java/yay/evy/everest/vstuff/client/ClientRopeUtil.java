// src/main/java/yay/evy/everest/vstuff/client/ClientRopeUtil.java
package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClientRopeUtil {

    private static BlockPos highlightPos = null;
    private static long highlightUntil = 0;

    public static void drawOutline(Level level, BlockPos pos) {
        highlightPos = pos.immutable();
        highlightUntil = System.currentTimeMillis() + 2000;
    }

    public static void render(PoseStack poseStack) {
        if (highlightPos == null || System.currentTimeMillis() > highlightUntil)
            return;

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null)
            return;

        BlockState state = level.getBlockState(highlightPos);
        VoxelShape shape = state.getShape(level, highlightPos);
        if (shape.isEmpty())
            return;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        AABB box = shape.bounds().move(highlightPos).move(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        LevelRenderer.renderLineBox(
                poseStack,
                mc.renderBuffers().bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.lines()),
                box,
                0f, 1f, 0f, 1f
        );
    }
}
