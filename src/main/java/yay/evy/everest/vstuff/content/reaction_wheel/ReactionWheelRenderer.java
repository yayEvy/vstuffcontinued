package yay.evy.everest.vstuff.content.reaction_wheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.index.VStuffPartials;

public class ReactionWheelRenderer extends KineticBlockEntityRenderer<ReactionWheelBlockEntity> {

    public ReactionWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ReactionWheelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        BlockState state = be.getBlockState();
        Direction direction = state.getValue(ReactionWheelBlock.FACING).getOpposite();
        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        int lightBehind = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(direction));

        SuperByteBuffer shaftHalf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, direction);
        SuperByteBuffer coreModel = CachedBuffers.partialFacing(VStuffPartials.REACTION_WHEEL_CORE, state, direction);

        standardKineticRotationTransform(shaftHalf, be, lightBehind).renderInto(ms, vb);
        standardKineticRotationTransform(coreModel, be, lightBehind).renderInto(ms, vb);
    }


}

