package yay.evy.everest.vstuff.content.propeller.base;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractPropellerBlock<T extends AbstractPropellerBlockEntity> extends RotatedPillarKineticBlock implements IBE<T> {

    public static BooleanProperty HAS_BLADES = BooleanProperty.create("has_blades");
    public static DirectionProperty FACING = BlockStateProperties.FACING;

    public AbstractPropellerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(HAS_BLADES, false)
                .setValue(AXIS, Direction.NORTH.getAxis())
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_BLADES, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis preferredAxis = getPreferredAxis(context);

        if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer()
                .isShiftKeyDown()))
            return this.defaultBlockState()
                    .setValue(AXIS, preferredAxis);

        Direction.Axis axisProperty = preferredAxis != null && context.getPlayer()
                .isShiftKeyDown() ? context.getClickedFace()
                .getAxis()
                : context.getNearestLookingDirection()
                .getAxis();


        return this.defaultBlockState().setValue(AXIS, axisProperty).setValue(HAS_BLADES, false);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }


    @Override
    public abstract Class<T> getBlockEntityClass();

    @Override
    public abstract BlockEntityType<? extends T> getBlockEntityType();
}