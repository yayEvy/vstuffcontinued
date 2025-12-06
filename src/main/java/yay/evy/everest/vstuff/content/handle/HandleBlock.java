package yay.evy.everest.vstuff.content.handle;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.EntityBlock;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.network.StartHandleHoldPacket;
import yay.evy.everest.vstuff.network.HandlePackets;

public class HandleBlock extends DirectionalKineticBlock implements EntityBlock {

    public HandleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (level.isClientSide) {
            HandlePackets.sendToServer(new StartHandleHoldPacket(pos));
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof HandleBlockEntity handleBe) {
                handleBe.forceStopHolding();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (type != VStuffBlockEntities.HANDLE_BLOCK_BE.get())
            return null;

        return level.isClientSide
                ? (lvl, pos, st, be) -> HandleBlockEntity.clientTick(lvl, pos, st, be)
                : (lvl, pos, st, be) -> HandleBlockEntity.serverTick(lvl, pos, st, be);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return VStuffBlockEntities.HANDLE_BLOCK_BE.create(pos, state);
    }
}