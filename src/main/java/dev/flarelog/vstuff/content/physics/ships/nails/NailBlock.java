package dev.flarelog.vstuff.content.physics.ships.nails;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import javax.annotation.Nullable;
import java.util.Set;

public class NailBlock extends FaceAttachedHorizontalDirectionalBlock  implements IWrenchable  {

 //   public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public NailBlock(Properties pProperties) { super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));

    }


    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos theNailBlockPos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        return super.use(state, level, theNailBlockPos, player, hand, hit);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        System.out.println("Apple");
        if (level instanceof ServerLevel serverLevel) {


            Ship ship = ShipAssembler.assembleToShip(serverLevel, Set.of(pos), 1);


        }


    }


//    @Override
//    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean pMovedByPiston) {
//        super.onPlace(state, level, pos, oldState, pMovedByPiston);
//
//        System.out.println("Apple");
//        if (level instanceof ServerLevel serverLevel) {
//
//
//            Ship ship = ShipAssembler.assembleToShip(serverLevel, Set.of(pos), 1);
//
//        }
//
//    }


    //    @Override
//    @ParametersAreNonnullByDefault
//    public @NotNull VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
//        VoxelShape shape = Shapes.empty();
//        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0046875, 0.25, 0.8125, 0.15625, 0.75), BooleanOp.OR);
//        shape = Shapes.join(shape, Shapes.box(0.16751, 0.0015625, 0.133455625, 0.29251, 0.1859375, 0.539705625), BooleanOp.OR);
//        shape = Shapes.join(shape, Shapes.box(0.16751, 0.00156875, 0.460294375, 0.29251, 0.18594375, 0.866544375), BooleanOp.OR);
//        shape = Shapes.join(shape, Shapes.box(0.70749, 0.0015625, 0.133455625, 0.83249, 0.1859375, 0.539705625), BooleanOp.OR);
//        shape = Shapes.join(shape, Shapes.box(0.70749, 0.00156875, 0.460294375, 0.83249, 0.18594375, 0.866544375), BooleanOp.OR);
//        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.125, 0.75, 0.1875, 0.25), BooleanOp.OR);
//        shape = Shapes.join(shape, Shapes.box(0.25, 0, 0.75, 0.75, 0.1875, 0.875), BooleanOp.OR);
//
//
//
//        return shape;
//    }

    public BlockState getFacingForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

//    @Override
//    @ParametersAreNonnullByDefault
//    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
//        return getShape(state, blockGetter, blockPos, collisionContext);
//    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
       // super.createBlockStateDefinition(builder);
        builder.add(FACING, FACE);
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return true;
    }






}
