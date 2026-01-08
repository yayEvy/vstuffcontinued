package yay.evy.everest.vstuff.content.rope.pulley;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffShapes;

import javax.annotation.ParametersAreNonnullByDefault;

public class PhysPulleyBlock extends HorizontalKineticBlock implements IBE<PhysPulleyBlockEntity>, IWrenchable {

    public PhysPulleyBlock(Properties properties) {
        super(properties.strength(3.0f).requiresCorrectToolForDrops());
    }
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");


    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof TieredItem tieredItem && tieredItem.getTier().getLevel() >= 1;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
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
        return this.defaultBlockState()
                .setValue(HORIZONTAL_FACING, facing)
                .setValue(POWERED, false);

    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (AllItems.WRENCH.isIn(heldItem)) {
            UseOnContext context = new UseOnContext(player, hand, hit);
            InteractionResult result = onWrenched(state, context);
            if (result.consumesAction()) {
                return result;
            }
        }

        return InteractionResult.PASS;
    }


    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
                                        CollisionContext p_220053_4_) {
        return VStuffShapes.PHYS_PULLEY.get(state.getValue(HORIZONTAL_FACING));
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter p_220071_2_, BlockPos p_220071_3_,
                                                 CollisionContext p_220071_4_) {
        return getShape(state, p_220071_2_, p_220071_3_, p_220071_4_);
    }


    @Override
    public Class<PhysPulleyBlockEntity> getBlockEntityClass() {
        return PhysPulleyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PhysPulleyBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.PHYS_PULLEY_BE.get(); // change later
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);
        boolean wasPowered = state.getValue(POWERED);

        if (powered != wasPowered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);

            if (level.getBlockEntity(pos) instanceof PhysPulleyBlockEntity be) {
                be.onRedstoneUpdate(powered);
            }
        }
    }



}