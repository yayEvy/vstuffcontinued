package yay.evy.everest.vstuff.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.magnetism.MagnetismManager;

public class RedstoneMagnetBlock extends Block {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RedstoneMagnetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        System.out.println("Placing magnet at " + context.getClickedPos() + " with power: " + powered);
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean wasPowered = isPowered(state);
            boolean isPowered = level.hasNeighborSignal(pos);

            System.out.println("Magnet at " + pos + " neighbor changed. Was powered: " + wasPowered + ", Is powered: " + isPowered);

            if (isPowered != wasPowered) {
                BlockState newState = state.setValue(POWERED, isPowered);
                level.setBlock(pos, newState, 3);

                if (isPowered) {
                    System.out.println("Magnet at " + pos + " just got powered - activating");
                    level.scheduleTick(pos, this, 1);
                } else {
                    System.out.println("Magnet at " + pos + " just lost power - deactivating");
                    MagnetismManager.onMagnetDeactivated((ServerLevel) level, pos);
                }
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (isPowered(state)) {
            System.out.println("Scheduled tick: Activating magnet at " + pos);
            MagnetismManager.onMagnetActivated(level, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide) {
            System.out.println("Magnet placed at " + pos + " with powered state: " + isPowered(state));

            if (isPowered(state)) {
                System.out.println("Magnet placed already powered - scheduling activation");
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && isPowered(state) && !newState.is(this)) {
            System.out.println("Powered magnet removed at " + pos);
            MagnetismManager.onMagnetDeactivated((ServerLevel) level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    public static Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    public static boolean isPowered(BlockState state) {
        return state.getValue(POWERED);
    }

    public static Direction getAttractSide(BlockState state) {
        return state.getValue(FACING);
    }

    public static Direction getRepelSide(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    public static BlockPos getAttractPos(BlockPos magnetPos, BlockState state) {
        return magnetPos.relative(getAttractSide(state));
    }

    public static BlockPos getRepelPos(BlockPos magnetPos, BlockState state) {
        return magnetPos.relative(getRepelSide(state));
    }
}
