package yay.evy.everest.vstuff.content.pulley;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.index.VStuffBlockEntities;
import yay.evy.everest.vstuff.index.VStuffShapes;

import javax.annotation.ParametersAreNonnullByDefault;

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
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
                                        CollisionContext p_220053_4_) {
        return VStuffShapes.PULLEY_ANCHOR.get(state.getValue(FACING));
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter p_220071_2_, BlockPos p_220071_3_,
                                                 CollisionContext p_220071_4_) {
        return getShape(state, p_220071_2_, p_220071_3_, p_220071_4_);
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
