package yay.evy.everest.vstuff.content.thrust;


import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.processing.AssemblyOperatorUseContext;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffShapes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


public class MechanicalThrusterBlock extends DirectionalAxisKineticBlock implements IBE<MechanicalThrusterBlockEntity> {
    public MechanicalThrusterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new MechanicalThrusterBlockEntity(VStuffBlockEntities.MECHANICAL_THRUSTER_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if (type == VStuffBlockEntities.MECHANICAL_THRUSTER_BE.get()) {
            return new SmartBlockEntityTicker<>();
        }
        return null;
    }
    public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                        @Nonnull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MechanicalThrusterBlockEntity thrusterBE)) return;

        ThrusterForceAttachment attachment = ThrusterForceAttachment.get(level, pos);

        ThrusterData data = thrusterBE.getThrusterData();
        data.setDirection(VectorConversionsMCKt.toJOMLD(state.getValue(FACING).getOpposite().getNormal()));
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
                } else {
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
        if (blockEntity instanceof MechanicalThrusterBlockEntity thrusterBlockEntity) {
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
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return VStuffShapes.MECHANICAL_THRUSTER.get(state.getValue(FACING));
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getShape(state, blockGetter, blockPos, collisionContext);
    }

    @Override
    public Class<MechanicalThrusterBlockEntity> getBlockEntityClass() {
        return MechanicalThrusterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalThrusterBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.MECHANICAL_THRUSTER_BE.get();
    }

}