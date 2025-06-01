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

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean wasPowered = isPowered(state);
            boolean isPowered = level.hasNeighborSignal(pos);

            if (isPowered != wasPowered) {
                BlockState newState = state.setValue(POWERED, isPowered);
                level.setBlock(pos, newState, 3);

                if (isPowered) {
                    // Magnet just got powered - activate it
                    MagnetismManager.onMagnetActivated((ServerLevel) level, pos);
                } else {
                    // Magnet just lost power - deactivate it
                    MagnetismManager.onMagnetDeactivated((ServerLevel) level, pos);
                }
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }


    private void onPowerChanged(ServerLevel level, BlockPos pos, BlockState state, boolean powered) {
        if (powered) {
            System.out.println("Redstone Magnet at " + pos + " is now ACTIVE - searching for targets...");

            // Find nearby magnets when this one activates
            Vector3d magnetPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            var nearbyMagnets = MagnetismManager.findMagnetsInArea(level, magnetPos, 64.0);

            System.out.println("Found " + nearbyMagnets.size() + " nearby magnets");
        } else {
            System.out.println("Redstone Magnet at " + pos + " is now INACTIVE");
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide && isPowered(state)) {
            this.onPowerChanged((ServerLevel) level, pos, state, true);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && isPowered(state)) {
            System.out.println("Powered magnet removed at " + pos);
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

    // Helper methods for magnet functionality
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
