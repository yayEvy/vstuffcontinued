package yay.evy.everest.vstuff.blocks;


import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.spaceeye.elementa.svg.data.Rotation;
import yay.evy.everest.vstuff.thruster.AbstractThrusterBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class RotationalThrusterBlock extends AbstractThrusterBlock {
    public RotationalThrusterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new RotationalThrusterBlockEntity(ModBlockEntities.ROTATIONAL_THRUSTER_BE.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if (type == ModBlockEntities.ROTATIONAL_THRUSTER_BE.get()) {
            return new SmartBlockEntityTicker<>();
        }
        return null;
    }

}