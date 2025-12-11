package yay.evy.everest.vstuff.client;

import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.outliner.Outline.OutlineParams;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClientRopeUtil {

    public static final class Colors {
        public static final int GREEN = 0x77DD77;
        public static final int YELLOW = 0xFDFD96;
        public static final int RED = 0xFF6961;
    }

    public static void drawOutline(ClientLevel level, BlockPos pos, int color) {
        if (!level.isClientSide())
            return;

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);

        if (shape.isEmpty())
            return;

        AABB bb = shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());

        Outliner outliner = Outliner.getInstance();
        OutlineParams params = outliner.showAABB(pos, bb);

        params.colored(color)
                .lineWidth(1 / 16f);
    }
}
