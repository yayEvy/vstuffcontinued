package yay.evy.everest.vstuff.ropes;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import yay.evy.everest.vstuff.block.ModBlockEntities;

public class RopePulleyBlock extends HorizontalKineticBlock implements IBE<RopePulleyBlockEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public RopePulleyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RopePulleyBlockEntity pulleyEntity) {
                pulleyEntity.removeExistingConstraint();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(HORIZONTAL_FACING, facing)
                .setValue(POWERED, powered);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        // For HorizontalKineticBlock, the shaft should connect from the back
        Direction facing = state.getValue(HORIZONTAL_FACING);
        return face == facing.getClockWise() || face ==facing.getCounterClockWise();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        return facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.SLOW;
    }

    @Override
    public boolean hideStressImpact() {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RopePulleyBlockEntity pulley) {
            if (player.isShiftKeyDown()) {
                pulley.resetRope();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
        }
    }

    @Override
    public Class<RopePulleyBlockEntity> getBlockEntityClass() {
        return RopePulleyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RopePulleyBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ROPE_PULLEY.get();
    }
}
