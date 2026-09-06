package dev.flarelog.vstuff.content.physics.ships.nails;


import dev.flarelog.vstuff.internal.utility.FixedConstraintUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import java.util.HashMap;
import java.util.Set;

import static dev.flarelog.vstuff.content.physics.ships.nails.Nail.nails;


public class NailItem extends BlockItem {


  public static HashMap<BlockPos,BlockPos> posMap= new HashMap<>();
  public static Direction coolDirection;
  public static BlockPos blockToBeNailedTo;
    public NailItem(NailBlock pBlock, Properties pProperties) { super(pBlock, pProperties); }


    @Override
    public InteractionResult useOn(UseOnContext context) {


        BlockPos clickedPos = context.getClickedPos();
        coolDirection = context.getClickedFace();
        BlockPos nextPos = clickedPos.relative(coolDirection, 1);
        blockToBeNailedTo = clickedPos;
        Nail nail = Nail.grabThatNailOerThereSonny(clickedPos);

        if ( nail == null) {
            nail = Nail.newNail(0.0, coolDirection, clickedPos);
        }
        posMap.put(nextPos,clickedPos);

        if (Nail.grabThatNailOerThereSonny(clickedPos) != null && context.getPlayer().isShiftKeyDown()){
            if (nail.rotation + 7.5 > 360){nail.rotation = 0.0;}

            nail.rotation += 7.5;
            context.getPlayer().displayClientMessage(Component.literal("Rotation set to " + nail.rotation + " degrees").withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.PASS;
        }
        nail.direction = coolDirection;

        super.useOn(context);
        if (context.getLevel() instanceof ServerLevel serverLevel) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ServerShip ship = ShipAssembler.assembleToShip(serverLevel, Set.of(nextPos), 1);
            FixedConstraintUtils.createFixedConstraint(serverLevel, clickedPos, ship, nail);
        }



        return InteractionResult.PASS;
    }

//    @Override
//    public InteractionResult place(BlockPlaceContext context) {
//
//
//        BlockPos clickedPos = context.getClickedPos();
//        coolDirection = context.getClickedFace();
//        BlockPos nextPos = clickedPos.relative(coolDirection, 1);
//        blockToBeNailedTo = clickedPos;
//        Nail nail = Nail.grabThatNailOerThereSonny(clickedPos);
//
//        if ( nail == null) {
//            nail = Nail.newNail(0.0, coolDirection, clickedPos);
//        }
//        posMap.put(nextPos,clickedPos);
//
//        if (Nail.grabThatNailOerThereSonny(clickedPos) != null && context.getPlayer().isShiftKeyDown()){
//            if (nail.rotation + 7.5 > 360){nail.rotation = 0.0;}
//
//            nail.rotation += 7.5;
//            context.getPlayer().displayClientMessage(Component.literal("Rotation set to " + nail.rotation + " degrees").withStyle(ChatFormatting.GREEN), true);
//            return InteractionResult.PASS;
//        }
//
//        super.place(context);
//            if (context.getLevel() instanceof ServerLevel serverLevel) {
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                ServerShip ship = ShipAssembler.assembleToShip(serverLevel, Set.of(nextPos), 1);
//                FixedConstraintUtils.createFixedConstraint(serverLevel, clickedPos, ship, nail);
//            }
//
//
//
//        return InteractionResult.PASS;
//    }

    public Nail getNail(Integer id) {
        return nails.get(id);
    }


        }

