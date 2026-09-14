package dev.flarelog.vstuff.content.physics.ships.nails;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.funnel.AndesiteFunnelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class NailBlock extends FaceAttachedHorizontalDirectionalBlock  implements IWrenchable {

 //   public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public NailBlock(Properties pProperties) { super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));

    }


    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos theNailBlockPos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        return super.use(state, level, theNailBlockPos, player, hand, hit);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return IWrenchable.super.onWrenched(state, context);
    }




    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0046875, 0.25, 0.8125, 0.15625, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.16751, 0.0015625, 0.133455625, 0.29251, 0.1859375, 0.539705625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.16751, 0.00156875, 0.460294375, 0.29251, 0.18594375, 0.866544375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.70749, 0.0015625, 0.133455625, 0.83249, 0.1859375, 0.539705625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.70749, 0.00156875, 0.460294375, 0.83249, 0.18594375, 0.866544375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.125, 0.75, 0.1875, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.75, 0.75, 0.1875, 0.875), BooleanOp.OR);



        return shape;
    }

    public BlockState getFacingForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getShape(state, blockGetter, blockPos, collisionContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
       // super.createBlockStateDefinition(builder);
        builder.add(FACING, FACE);
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return true;
    }






}
