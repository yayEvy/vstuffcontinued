package dev.flarelog.vstuff.content.physics.ships.nails;

import dev.flarelog.vstuff.index.VStuffShapes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class NailBlock extends Block {

    public NailBlock(Properties pProperties) { super(pProperties); }





    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos theNailBlockPos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

//        BlockPos notTheNailBlockPos = hit.getBlockPos();
//        Direction directionOfMalnourishment = hit.getDirection();
//        System.out.println("on block use: " + directionOfMalnourishment);
//
//        Nail nail = Nail.newNail(0.0, directionOfMalnourishment, notTheNailBlockPos);
//
//        if ( player.isShiftKeyDown()){
//            if (nail.rotation + 7.5 > 360){nail.rotation = 0.0;}
//
//            nail.rotation += 7.5;
//            player.displayClientMessage(Component.literal("Rotation set to " + nail.rotation + " degrees").withStyle(ChatFormatting.GREEN), true);
//
//        }

        return super.use(state, level, theNailBlockPos, player, hand, hit);
    }


    @Override
    @SuppressWarnings("deprecation")
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        return true;
    }





}
