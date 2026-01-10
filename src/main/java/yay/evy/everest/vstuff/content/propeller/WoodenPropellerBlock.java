package yay.evy.everest.vstuff.content.propeller;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import yay.evy.everest.vstuff.content.propeller.base.AbstractPropellerBlock;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffShapes;

public class WoodenPropellerBlock extends AbstractPropellerBlock<WoodenPropellerBlockEntity> {

    public WoodenPropellerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VStuffShapes.WOODEN_ENGINE.get(pState.getValue(AXIS));
    }

    @Override
    public Class<WoodenPropellerBlockEntity> getBlockEntityClass() {
        return WoodenPropellerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WoodenPropellerBlockEntity> getBlockEntityType() {
        return VStuffBlockEntities.WOODEN_PROPELLER_BE.get();
    }
}