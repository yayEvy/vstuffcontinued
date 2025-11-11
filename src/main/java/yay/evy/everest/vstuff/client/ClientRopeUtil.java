package yay.evy.everest.vstuff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.outliner.Outline.OutlineParams;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClientRopeUtil {

    public static void drawOutline(Level level, BlockPos pos) {
        if (!level.isClientSide())
            return;

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);

        if (shape.isEmpty())
            return;

        AABB bb = shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());

        Outliner outliner = Outliner.getInstance();
        OutlineParams params = outliner.showAABB(pos, bb);

        params.colored(0x00FF00)
                .lineWidth(1 / 16f);
    }

    public static void drawFailOutline(Level level, BlockPos pos) {
        if (!level.isClientSide())
            return;

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);

        if (shape.isEmpty())
            return;

        AABB bb = shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());

        Outliner outliner = Outliner.getInstance();
        OutlineParams params = outliner.showAABB(pos, bb);

        params.colored(0xFF0000)
                .lineWidth(1 / 16f);
    }
}
