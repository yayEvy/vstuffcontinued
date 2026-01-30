package yay.evy.everest.vstuff.client;

import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.outliner.Outline.OutlineParams;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yay.evy.everest.vstuff.internal.network.packet.OutlinePacket;

@OnlyIn(Dist.CLIENT)
public class ClientOutlineHandler {

    public static final int RED = 0xFF6961;
    public static final int GREEN = 0x77DD77;

    public static void drawOutline(Level level, BlockPos pos, int color) {
        if (!level.isClientSide()) return;

        var state = level.getBlockState(pos);
        var shape = state.getShape(level, pos);
        if (shape.isEmpty()) return;

        var bb = shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());

        Outliner outliner = Outliner.getInstance();
        OutlineParams params = outliner.showAABB(pos, bb);

        params.colored(color).lineWidth(1 / 16f);
    }

    public static void handleOutlinePacket(OutlinePacket pkt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            drawOutline(mc.level, pkt.pos(), pkt.color());
        }
    }
}
