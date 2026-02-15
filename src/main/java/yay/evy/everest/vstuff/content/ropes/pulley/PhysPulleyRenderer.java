package yay.evy.everest.vstuff.content.ropes.pulley;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import yay.evy.everest.vstuff.index.VStuffPartials;

import static yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlock.HORIZONTAL_FACING;

public class PhysPulleyRenderer extends ShaftRenderer<PhysPulleyBlockEntity> {

    public PhysPulleyRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PhysPulleyBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction direction = be.getBlockState().getValue(HORIZONTAL_FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        SuperByteBuffer coil = CachedBuffers.partialFacing(VStuffPartials.PULLEY_COIL, be.getBlockState(), direction);

        standardKineticRotationTransform(coil, be, light).renderInto(ms, vb);
    }
}
