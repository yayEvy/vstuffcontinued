package yay.evy.everest.vstuff.content.physics.levituff;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

public class RefinedLevituffBlock extends Block implements IBE<RefinedLevituffBlockEntity> {
    public RefinedLevituffBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (l, p, s, be) -> RefinedLevituffBlockEntity.tick(l, p, (RefinedLevituffBlockEntity) be);
    }

    @Override
    public Class<RefinedLevituffBlockEntity> getBlockEntityClass() {
        return RefinedLevituffBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RefinedLevituffBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.REFINED_LEVITUFF_BE.get();
    }
}
