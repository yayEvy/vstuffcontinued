package dev.flarelog.vstuff.content.physics.ships.nails;

import lombok.Data;
import lombok.Getter;
import lombok.Lombok;
import lombok.extern.java.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public  class Nail {


    public Double rotation = 0.0;
    public Direction direction;
    public BlockPos initialPos;
    public BlockPos nailHeadPos;
    public Integer id;
    public static Integer nextNail = 0;


    public static Map<Integer, Nail> nails = new HashMap<>();


    private Nail(Double rotation, Direction direction, BlockPos initialPos, BlockPos nailHeadPos, Integer id){
        this.rotation = rotation;
        this.direction = direction;
        this.initialPos = initialPos;
        this.nailHeadPos = nailHeadPos;
        this.id = id;

    }

    public static Nail newNail(Double rotation, Direction direction, BlockPos initialPos, BlockPos nailHeadPos){
        System.out.println("creating new nail with id: " + nextNail);
        Nail nail = new Nail(rotation, direction, initialPos, nailHeadPos, nextNail);
        nails.put(nextNail, nail);

        nextNail++;

        return nail;
    }

    public static Nail grabThatNailOerThereSonny(BlockPos pos) {
        for (Nail nail : nails.values()) {
            System.out.println("nail pos: " + nail.initialPos);
            if (nail.initialPos.equals(pos)) {
                return nail;
            }
        }
        return null;
    }

    public static Nail nowBoyWhatDidISayAboutManHandlingThemThereNails(BlockPos pos){
        for (Nail nail : nails.values()){

            BlockPos regBlockPos = new BlockPos(pos.getX() + 1,pos.getY(),pos.getZ());
            System.out.println("better head pos: " + regBlockPos);
            System.out.println("head pos: " + nail.nailHeadPos);

            if (nail.nailHeadPos.equals(regBlockPos)) {
                System.out.println("True");
                return nail; }
        }
        return null;
}

    public static Nail grabById(int id) {
        for (Nail nail : nails.values()) {
            System.out.println(nail.id);
            if (nail.id.equals(id)) {
                return nail;
            }
        }
        return null;
    }
}
