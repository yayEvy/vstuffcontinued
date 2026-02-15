package yay.evy.everest.vstuff.content.thrust;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import yay.evy.everest.vstuff.index.VStuffPartials;

import static yay.evy.everest.vstuff.content.thrust.MechanicalThrusterBlock.FACING;

public class MechanicalThrusterRenderer extends ShaftRenderer<MechanicalThrusterBlockEntity> {

    public MechanicalThrusterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(MechanicalThrusterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Direction facing = be.getBlockState().getValue(FACING);
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        int lightInFront = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(facing));

        SuperByteBuffer fan = CachedBuffers.partialFacing(VStuffPartials.THRUSTER_FAN, be.getBlockState(), facing);

        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float speed = be.getSpeed() * 5;
        if (speed > 0)
            speed = Mth.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = Mth.clamp(speed, -64 * 20, -80);
        float angle = (time * speed * 3 / 10f) % 360;
        angle = angle / 180f * (float) Math.PI;

        kineticRotationTransform(fan, be, facing.getAxis(), angle, lightInFront).renderInto(ms, vb);
    }
}
