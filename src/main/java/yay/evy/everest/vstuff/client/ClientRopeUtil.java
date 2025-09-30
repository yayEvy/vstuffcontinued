// src/main/java/yay/evy/everest/vstuff/client/ClientRopeUtil.java

package yay.evy.everest.vstuff.client;

import com.simibubi.create.CreateClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClientRopeUtil {

    public static void drawOutline(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getShape(level, pos);

            if (shape.isEmpty())
                return;


            CreateClient.OUTLINER.showAABB(pos, shape.bounds()
                            .move(pos))
                    .colored(0x00FF00)
                    .lineWidth(1 / 16f);
        }
    }
}