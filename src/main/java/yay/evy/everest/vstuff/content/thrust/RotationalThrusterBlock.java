package yay.evy.everest.vstuff.content.thrust;


import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class RotationalThrusterBlock extends AbstractThrusterBlock {
    public RotationalThrusterBlock(Properties properties) {
        super(properties);
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

}