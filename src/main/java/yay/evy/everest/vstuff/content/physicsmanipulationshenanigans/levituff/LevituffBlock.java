package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;

public class LevituffBlock extends Block implements IBE<LevituffBlockEntity> {
    public LevituffBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<LevituffBlockEntity> getBlockEntityClass() {
        return LevituffBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LevituffBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.LEVITUFF_BE.get();
    }
}
