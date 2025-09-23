package yay.evy.everest.vstuff.content.thrust;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import yay.evy.everest.vstuff.index.VStuffShapes;

import java.util.Arrays;


@SuppressWarnings("deprecation")
public abstract class AbstractThrusterBlock extends KineticBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final IntegerProperty RPM = IntegerProperty.create("rpm", 0, 256);

    protected AbstractThrusterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }


    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        Direction baseDirection = context.getNearestLookingDirection();
        Direction placeDirection;
        Player player = context.getPlayer();
        if (player != null) {
            placeDirection = !player.isShiftKeyDown() ? baseDirection : baseDirection.getOpposite();
        } else {
            placeDirection = baseDirection.getOpposite();
        }

        return this.defaultBlockState().setValue(FACING, placeDirection).setValue(AXIS, baseDirection.getAxis());
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS, RPM);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        Direction facing = state.getValue(FACING);
        return getAxisFromFacingDir(facing);
    }


    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(FACING);
        return face.getAxis() == getAxisFromFacingDir(facing);
    }

    private Direction.Axis getAxisFromFacingDir(Direction facing) {
        switch (facing) {
            case EAST, WEST  -> { return Direction.Axis.Z; }
            case UP, DOWN -> { return Direction.Axis.X;}
            default -> { return Direction.Axis.X; } // north, south
        }
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state);

    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type);

    @Override
    public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                        @Nonnull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof AbstractThrusterBlockEntity thrusterBE)) return;

        ThrusterForceAttachment attachment = ThrusterForceAttachment.get(level, pos);

        ThrusterData data = thrusterBE.getThrusterData();
        data.setDirection(VectorConversionsMCKt.toJOMLD(state.getValue(FACING).getNormal()));
        data.setThrust(0);

        if (attachment != null) {
            ThrusterForceApplier applier = new ThrusterForceApplier(data);
            attachment.addApplier(pos, applier);
            System.out.println("[Thruster] onPlace: added applier at " + pos);
        } else {
            level.getServer().execute(() -> {
                ThrusterForceAttachment deferredAttachment = ThrusterForceAttachment.get(level, pos);
                if (deferredAttachment != null) {
                    ThrusterForceApplier applier = new ThrusterForceApplier(data);
                    deferredAttachment.addApplier(pos, applier);
                    System.out.println("[Thruster] onPlace: deferred registration succeeded at " + pos);
                } else {
                    System.out.println("[Thruster] onPlace: deferred registration still null at " + pos);
                }
            });
        }

        // Initial obstruction check & thrust update
        thrusterBE.calculateObstruction(level, pos, state.getValue(FACING));
        thrusterBE.updateThrust(state);
        thrusterBE.setChanged();

    }


    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (level.isClientSide()) return;

        ThrusterForceAttachment ship = ThrusterForceAttachment.get(level, pos);
        if (ship != null) {
            ship.removeApplier((ServerLevel) level, pos);
        }
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                @Nonnull Block block, @Nonnull BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractThrusterBlockEntity thrusterBlockEntity) {
            thrusterBlockEntity.calculateObstruction(level, pos, state.getValue(FACING));
            thrusterBlockEntity.updateThrust(state);
            thrusterBlockEntity.setChanged();
            state.setValue(RPM, (int) thrusterBlockEntity.getSpeed());

        }
    }


    @Override
    public BlockState rotate(@Nonnull BlockState state, @Nonnull Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)))
                .setValue(AXIS, getAxisFromFacingDir(rot.rotate(state.getValue(FACING))));
    }

    @Override
    public BlockState mirror(@Nonnull BlockState state, @Nonnull Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
                                        CollisionContext p_220053_4_) {
        return VStuffShapes.ROTATIONAL_THRUSTER.get(state.getValue(FACING));
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter p_220071_2_, BlockPos p_220071_3_,
                                                 CollisionContext p_220071_4_) {
        return getShape(state, p_220071_2_, p_220071_3_, p_220071_4_);
    }

}