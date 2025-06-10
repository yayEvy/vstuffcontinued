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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import yay.evy.everest.vstuff.magnetism.MagnetRegistry;

public class RedstoneMagnetBlock extends Block {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

    public RedstoneMagnetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, Boolean.FALSE)
                .setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, POWER);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        int power = getRedstonePower(context.getLevel(), context.getClickedPos());
        boolean powered = power > 0;

        System.out.println("[MAGNET] Placing magnet at " + context.getClickedPos() +
                " facing " + facing + " powered: " + powered + " power: " + power);

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(POWERED, powered)
                .setValue(POWER, power);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean wasPowered = isPowered(state);
            int oldPower = getPowerLevel(state);
            int newPower = getRedstonePower(level, pos);
            boolean isPowered = newPower > 0;

            System.out.println("[MAGNET] Neighbor changed at " + pos +
                    ". Was powered: " + wasPowered + " (power: " + oldPower +
                    "), Is powered: " + isPowered + " (power: " + newPower + ")");

            if (isPowered != wasPowered || newPower != oldPower) {
                BlockState newState = state.setValue(POWERED, isPowered).setValue(POWER, newPower);
                level.setBlock(pos, newState, 3);

                if (isPowered && !wasPowered) {
                    System.out.println("[MAGNET] Registering magnet at " + pos + " with power " + newPower);
                    MagnetRegistry.getInstance().registerMagnet((ServerLevel) level, pos, newState);
                } else if (!isPowered && wasPowered) {
                    System.out.println("[MAGNET] Unregistering magnet at " + pos);
                    MagnetRegistry.getInstance().unregisterMagnet((ServerLevel) level, pos);
                } else if (isPowered && newPower != oldPower) {
                    System.out.println("[MAGNET] Updating magnet at " + pos + " power: " + oldPower + " -> " + newPower);
                    MagnetRegistry.getInstance().unregisterMagnet((ServerLevel) level, pos);
                    MagnetRegistry.getInstance().registerMagnet((ServerLevel) level, pos, newState);
                }
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    private static int getRedstonePower(Level level, BlockPos pos) {
        int maxPower = 0;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);

            int directPower = level.getSignal(neighborPos, direction);
            maxPower = Math.max(maxPower, directPower);

            int indirectPower = level.getDirectSignalTo(neighborPos);
            maxPower = Math.max(maxPower, indirectPower);

            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.hasAnalogOutputSignal()) {
                int analogPower = neighborState.getAnalogOutputSignal(level, neighborPos);
                maxPower = Math.max(maxPower, analogPower);
            }

            if (neighborState.isSignalSource()) {
                int sourcePower = neighborState.getSignal(level, neighborPos, direction.getOpposite());
                maxPower = Math.max(maxPower, sourcePower);
            }
        }

        int standardPower = level.getBestNeighborSignal(pos);
        maxPower = Math.max(maxPower, standardPower);

        System.out.println("[MAGNET] Power detection at " + pos + ": " + maxPower);
        return Math.min(maxPower, 15);
    }

    public static void updatePowerLevel(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RedstoneMagnetBlock)) return;

        int newPower = getRedstonePower(level, pos);
        boolean newPowered = newPower > 0;

        if (newPowered != isPowered(state) || newPower != getPowerLevel(state)) {
            BlockState newState = state.setValue(POWERED, newPowered).setValue(POWER, newPower);
            level.setBlock(pos, newState, 3);
            System.out.println("[MAGNET] Manual power update at " + pos + ": " + newPower);
        }
    }


    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 2);

            if (isActive(state)) {
                System.out.println("[MAGNET] Placed active magnet at " + pos + " with power " + getPowerLevel(state) + ", registering");
                MagnetRegistry.getInstance().registerMagnet((ServerLevel) level, pos, state);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        updatePowerLevel(level, pos);
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !newState.is(this)) {
            System.out.println("[MAGNET] Removing magnet at " + pos);
            MagnetRegistry.getInstance().unregisterMagnet((ServerLevel) level, pos);
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

    // Static helper methods
    public static Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    public static boolean isPowered(BlockState state) {
        return state.getValue(POWERED);
    }

    public static int getPowerLevel(BlockState state) {
        return state.getValue(POWER);
    }

    public static Direction getAttractSide(BlockState state) {
        return state.getValue(FACING);
    }

    public static Direction getRepelSide(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    public static boolean isActive(BlockState state) {
        return isPowered(state) && getPowerLevel(state) > 0;
    }
}
