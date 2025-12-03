package yay.evy.everest.vstuff.content.pulley;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

public class PulleyAnchorBlock extends HorizontalDirectionalBlock implements IBE<PulleyAnchorBlockEntity> {
    public PulleyAnchorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Override
    public Class<PulleyAnchorBlockEntity> getBlockEntityClass() {
        return PulleyAnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PulleyAnchorBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.PULLEY_ANCHOR_BE.get();
    }
}
