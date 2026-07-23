package dev.flarelog.vstuff.content.physics.ships.nail;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.HashMap;

public class NailItem extends Item {

  public static HashMap<BlockPos,BlockPos> posMap= new HashMap<>();
  public static Direction coolDirection;
    public NailItem(Properties pProperties) { super(pProperties);}

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        coolDirection = context.getClickedFace();
        BlockPos nextPos = clickedPos.relative(coolDirection, 1);

        posMap.put(nextPos,clickedPos);

        return InteractionResult.SUCCESS;
    }


}

