package dev.flarelog.vstuff.content.physics.ships.nails;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.Map;

public class Nail {
    public Double rotation = 0.0;
    public Direction direction;
    public BlockPos initialPos;
    public Integer id;
    public static Integer nextNail = 0;

    public static Map<Integer, Nail> nails = new HashMap<>();


    private Nail(Double rotation, Direction direction, BlockPos initialPos, Integer id){
        this.rotation = rotation;
        this.direction = direction;
        this.initialPos = initialPos;
        this.id = id;
    }

    public static Nail newNail(Double rotation, Direction direction, BlockPos initialPos){
        Nail nail = new Nail(rotation, direction, initialPos, nextNail++);
        nails.put(nextNail++, nail);

        return nail;
    }

public static Nail grabThatNailOerThereSonny(BlockPos pos){
        for (Nail nail : nails.values()){
            if (nail.initialPos.equals(pos)) {
                return nail; }
        }
        return null;
}
    public static Nail grabById(int id) {
        for (Nail nail : nails.values()) {
            if (nail.id.equals(id)) {
                return nail;
            }
        }
        return null;
    }
}
