package dev.flarelog.vstuff.content.physics.ships.nails;


import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.index.VStuffBlocks;
import dev.flarelog.vstuff.index.VStuffSounds;
import dev.flarelog.vstuff.internal.utility.FixedConstraintUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.ForgeSoundType;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;
import org.valkyrienskies.mod.common.item.ShipAssemblerItem;
import org.valkyrienskies.mod.common.item.VSBlockItem;
import org.valkyrienskies.mod.common.item.VSItem;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

import static dev.flarelog.vstuff.content.physics.ships.nails.Nail.nails;


public class NailItem extends BlockItem {

    public static SoundEvent[] boxOfNailSounds = {
            VStuffSounds.NAIL_1.get(),
            VStuffSounds.NAIL_2.get(),
            VStuffSounds.NAIL_3.get()
    };



    public static SoundEvent getRandomNailFromBox(){
         return boxOfNailSounds[(int)( Util.getMillis() % boxOfNailSounds.length )];
    }

    public static Supplier<SoundEvent> boxSupplier = new Supplier<SoundEvent>() {
        @Override
        public SoundEvent get() {
            return getRandomNailFromBox();
        }
    };

  public static HashMap<BlockPos,BlockPos> posMap= new HashMap<>();
  public static Direction coolDirection;
  public static BlockPos blockToBeNailedTo;
  public NailItem(NailBlock pBlock, Properties pProperties) { super(pBlock, pProperties); }




    @Override
    public InteractionResult useOn(UseOnContext context) {

            BlockPos clickedPos = context.getClickedPos();
            Block block = context.getLevel().getBlockState(clickedPos).getBlock();
            coolDirection = context.getClickedFace();
            BlockPos nextPos = clickedPos.relative(coolDirection, 1);
            blockToBeNailedTo = clickedPos;
            Nail nail = Nail.grabThatNailOerThereSonny(clickedPos);


            if (block instanceof NailBlock) return InteractionResult.PASS;

            if (nail == null) {
                nail = Nail.newNail(0.0, coolDirection, clickedPos, nextPos);
            }
            posMap.put(nextPos, clickedPos);


            if (Nail.grabThatNailOerThereSonny(clickedPos) != null && context.getPlayer().isShiftKeyDown()) {

                    if (nail.rotation + 7.5 > 360) {
                        nail.rotation = 0.0;
                    }

                    nail.rotation += 7.5;

                    context.getPlayer().displayClientMessage(Component.literal("Rotation set to " + nail.rotation + " degrees").withStyle(ChatFormatting.GREEN), true);
                    return InteractionResult.PASS;

            }
            nail.direction = coolDirection;


        super.useOn(context);
        if (context.getLevel() instanceof ServerLevel serverLevel) {


                if (context.getLevel().getBlockState(nextPos).getBlock() instanceof NailBlock nailBlock) {

                    nailBlock.nailId = nail.id;


//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }


                Ship nailHead = ShipAssembler.assembleToShip(serverLevel, Set.of(nextPos), 1);

                    Vector3dc c = new Vector3d(nailHead.getTransform().getPositionInShip());
                    BlockPos pos = new BlockPos((int) c.x(), (int) c.y(), (int) c.z());

                    nail.nailHeadPos = pos;

                    FixedConstraintUtils.createFixedConstraint(serverLevel, clickedPos, nailHead, nail);

                    System.out.println(getRandomNailFromBox());

                }
            }


        return InteractionResult.PASS;
    }

    public static ForgeSoundType nailSoundType = new ForgeSoundType(
            1.0f,
            1.0f,
            boxSupplier,
            boxSupplier,
            boxSupplier,
            boxSupplier,
            boxSupplier
            );


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

