package yay.evy.everest.vstuff.content.ships.reactionwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.index.VStuffPartialModels;

public class ReactionWheelRenderer extends KineticBlockEntityRenderer<ReactionWheelBlockEntity> {

    public ReactionWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ReactionWheelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(ReactionWheelBlock.FACING);
        Direction direction = facing.getOpposite();

        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        boolean ponder = be.getLevel() != null
                && be.getLevel().getClass().getName().contains("Ponder");

        int renderLight = ponder ? 0xF000F0 : light;
        Direction shaftDirection = ponder
                ? Direction.UP
                : (facing == Direction.DOWN ? Direction.DOWN : direction);

        Direction coreDirection = ponder
                ? Direction.NORTH
                : (facing == Direction.DOWN ? Direction.UP : direction);

        SuperByteBuffer shaftHalf =
                CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, shaftDirection);

        SuperByteBuffer coreModel =
                CachedBuffers.partialFacing(VStuffPartialModels.REACTION_WHEEL_CORE, state, coreDirection);

        standardKineticRotationTransform(shaftHalf, be, renderLight)
                .renderInto(ms, vb);

        standardKineticRotationTransform(coreModel, be, renderLight)
                .renderInto(ms, vb);
    }
}

