package yay.evy.everest.vstuff.content.pulley;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.AllItems;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.Collections;
import java.util.List;

public class PhysPulleyBlock extends HorizontalKineticBlock implements IBE<PhysPulleyBlockEntity>, IWrenchable {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public PhysPulleyBlock(Properties properties) {
        super(properties.strength(3.0f).requiresCorrectToolForDrops());
    }


    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof TieredItem tieredItem && tieredItem.getTier().getLevel() >= 1;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof PhysPulleyBlockEntity pulley) {
            CompoundTag tag = pulley.saveWithoutMetadata();
            if (!tag.isEmpty()) {
                stack.getOrCreateTag().merge(tag);
            }
        }
        return Collections.singletonList(stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PhysPulleyBlockEntity pulleyEntity) {
                pulleyEntity.removeExistingConstraint();
                pulleyEntity.onBlockRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = getCloneItemStack(state, null, level, pos, player);
        if (stack.isEmpty()) {
            stack = new ItemStack(this);
        }

        ItemHandlerHelper.giveItemToPlayer(player, stack);

        level.removeBlock(pos, false);

        return InteractionResult.SUCCESS;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PhysPulleyBlockEntity pulley) {
        }
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
        if (level.getBlockEntity(pos) instanceof PhysPulleyBlockEntity pulley) {
            System.out.println("RopePulleyBlock.use called - Hand: " + hand + ", Shift: " + player.isShiftKeyDown() + ", Item: " + player.getItemInHand(hand).getItem());

            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }

            return pulley.use(state, level, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PhysPulleyBlockEntity pulley) {
            ItemStack stack = new ItemStack(this);
            CompoundTag tag = pulley.saveWithoutMetadata();
            if (!tag.isEmpty()) {
                stack.getOrCreateTag().merge(tag);
            }
            return stack;
        }
        return new ItemStack(this);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }

            if (powered) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof PhysPulleyBlockEntity pulley) {
                    if (!pulley.isManualMode()) {
                        pulley.removeExistingConstraint();
                    }
                }
            }

        }
    }


    @Override
    public Class<PhysPulleyBlockEntity> getBlockEntityClass() {
        return PhysPulleyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PhysPulleyBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.PHYS_PULLEY_BE.get();
    }
}
