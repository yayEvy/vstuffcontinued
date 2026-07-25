package dev.flarelog.vstuff.content.physics.ships.nails;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;

public class Nail {
    public Double rotation = 0.0;
    public Direction direction;
    public HashMap<BlockPos,BlockPos> positionMap= new HashMap<>();

    public Nail(Double rotation, Direction direction, BlockPos pos1, BlockPos pos2){

        this.rotation = rotation;
        this.direction = direction;
    }
}
