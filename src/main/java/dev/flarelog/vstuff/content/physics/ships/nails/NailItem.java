package dev.flarelog.vstuff.content.physics.ships.nails;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.ropes.Rope;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.HashMap;

public class NailItem extends Item {


  public static Double rotation = 0.0;
  public static HashMap<BlockPos,BlockPos> posMap= new HashMap<>();
  public static Direction coolDirection;
    public NailItem(Properties pProperties) { super(pProperties);}

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        coolDirection = context.getClickedFace();
        BlockPos nextPos = clickedPos.relative(coolDirection, 1);

        posMap.put(nextPos,clickedPos);

        if (context.getClickedPos() == posMap.get(nextPos) && context.getPlayer().isShiftKeyDown()){
            if (rotation > 360){rotation = 0.0;}

         rotation += 7.5;
         context.getPlayer().displayClientMessage(VStuff.translate("Rotation: " + rotation + " degrees").withStyle(ChatFormatting.GREEN), true);

        }
        return InteractionResult.SUCCESS;
    }


}

