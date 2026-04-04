package yay.evy.everest.vstuff.content.ropes.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.createmod.catnip.outliner.Outline;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OutlinePacket extends SimplePacketBase {

    private BlockPos pos;
    private int color;

    public OutlinePacket(BlockPos pos, int color) {
        this.pos = pos;
        this.color = color;
    }

    public OutlinePacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.color = buffer.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(color);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient));
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            var state = mc.level.getBlockState(pos);
            var shape = state.getShape(mc.level, pos);
            if (shape.isEmpty()) return;

            var bb = shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());

            Outliner outliner = Outliner.getInstance();
            Outline.OutlineParams params = outliner.showAABB(pos, bb);

            params.colored(color).lineWidth(1 / 16f);
        }
    }

    public static final int RED = 0xFF6961;
    public static final int GREEN = 0x77DD77;
}
