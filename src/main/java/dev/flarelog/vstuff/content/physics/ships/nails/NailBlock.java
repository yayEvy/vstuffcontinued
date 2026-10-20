package dev.flarelog.vstuff.content.physics.ships.nails;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import dev.flarelog.vstuff.index.VStuffShapes;
import dev.flarelog.vstuff.internal.utility.FixedConstraintUtils;
import dev.flarelog.vstuff.internal.utility.VSUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ForgeSoundType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;
import org.valkyrienskies.mod.common.item.ShipAssemblerItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.simibubi.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.FACE;
import static dev.flarelog.vstuff.internal.utility.FixedConstraintUtils.updateFixedConstraintRotation;


public class NailBlock extends DirectionalBlock implements IWrenchable  {

    public Integer nailId;

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
        System.out.println("wrenched");


        Vector3d clickedPos = VSUtils.getWorldPos(context.getLevel(),context.getClickedPos());

        Vector3d v3d = clickedPos.add(FixedConstraintUtils.getOffset(context.getClickedFace()));

        BlockPos pos = new BlockPos((int) v3d.x , (int) v3d.y , (int) v3d.z );

        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();


      //  if (block instanceof NailBlock nailBlock) {

            Nail nail = Nail.grabById(nailId);

            System.out.println(context.getClickedPos());
            System.out.println(pos);
            System.out.println(nail);


            if (nail.rotation + 7.5 > 360) {
                nail.rotation = 0.0;
            }

            nail.rotation += 7.5;
            context.getPlayer().displayClientMessage(Component.literal("Rotation updated to " + nail.rotation + " degrees").withStyle(ChatFormatting.GREEN), true);

            System.out.println("level: " + context.getLevel());
            System.out.println("nail: " + nail);
            System.out.println("naid: " + nailId);

            updateFixedConstraintRotation(context.getLevel(), nail);
      //  }
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        System.out.println("nail id on place: "+nailId);
        if (level instanceof ServerLevel serverLevel) {


           // Ship ship = ShipAssembler.assembleToShip(serverLevel, Set.of(pos), 1);



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


    public static void setIDToBlockPos(Long ID, BlockPos pos){

        /// todo: make ts work

    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {

        Direction facing = state.getValue(NailBlock.FACING);
        int xRot = 0;
        int yRot = 0;

        switch (facing) {
            case DOWN  -> { xRot = 90;  yRot = 0;   } // Pointing Down
            case UP    -> { xRot = -90; yRot = 0;   } // Pointing Up
            case NORTH -> { xRot = 0;   yRot = 0;   } // Base position (0, 0)
            case SOUTH -> { xRot = 0;   yRot = 180; } // Rotated 180 around Y
            case WEST  -> { xRot = 0;   yRot = 270; } // Rotated 270 around Y
            case EAST  -> { xRot = 0;   yRot = 90;  } // Rotated 90 around Y
        }


        return VStuffShapes.NAIL.get(facing);
    }



    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace();
        System.out.println(direction);
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getShape(state, blockGetter, blockPos, collisionContext);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
       // super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return true;
    }






}
