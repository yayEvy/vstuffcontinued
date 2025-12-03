package yay.evy.everest.vstuff.content.thrust;


import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.processing.AssemblyOperatorUseContext;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffShapes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


public class RotationalThrusterBlock extends DirectionalAxisKineticBlock implements IBE<RotationalThrusterBlockEntity> {
    public RotationalThrusterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS_ALONG_FIRST_COORDINATE);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new RotationalThrusterBlockEntity(VStuffBlockEntities.ROTATIONAL_THRUSTER_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if (type == VStuffBlockEntities.ROTATIONAL_THRUSTER_BE.get()) {
            return new SmartBlockEntityTicker<>();
        }
        return null;
    }
    public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                        @Nonnull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof RotationalThrusterBlockEntity thrusterBE)) return;

        ThrusterForceAttachment attachment = ThrusterForceAttachment.get(level, pos);

        ThrusterData data = thrusterBE.getThrusterData();
        data.setDirection(VectorConversionsMCKt.toJOMLD(state.getValue(FACING).getNormal()));
        data.setThrust(0);

        if (attachment != null) {
            ThrusterForceApplier applier = new ThrusterForceApplier(data);
            attachment.addApplier(pos, applier);
        } else {
            level.getServer().execute(() -> {
                ThrusterForceAttachment deferredAttachment = ThrusterForceAttachment.get(level, pos);
                if (deferredAttachment != null) {
                    ThrusterForceApplier applier = new ThrusterForceApplier(data);
                    deferredAttachment.addApplier(pos, applier);
                    //  System.out.println("[Thruster] onPlace: deferred registration succeeded at " + pos);
                } else {
                    //  System.out.println("[Thruster] onPlace: deferred registration still null at " + pos);
                }
            });
        }

        // Initial obstruction check & thrust update
        thrusterBE.calculateObstruction(level, pos, state.getValue(FACING));
        thrusterBE.updateThrust(state);
        thrusterBE.setChanged();
    }



    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof TieredItem tieredItem && tieredItem.getTier().getLevel() >= 1;
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
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return super.onWrenched(state, context);
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                @Nonnull Block block, @Nonnull BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RotationalThrusterBlockEntity thrusterBlockEntity) {
            thrusterBlockEntity.calculateObstruction(level, pos, state.getValue(FACING));
            thrusterBlockEntity.updateThrust(state);
            thrusterBlockEntity.setChanged();
        }
    }

    @Override
    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        if (context instanceof AssemblyOperatorUseContext)
            return Direction.DOWN;
        else
            return super.getFacingForPlacement(context);
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

    @Override
    public Class<RotationalThrusterBlockEntity> getBlockEntityClass() {
        return RotationalThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotationalThrusterBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.ROTATIONAL_THRUSTER_BE.get();
    }

}