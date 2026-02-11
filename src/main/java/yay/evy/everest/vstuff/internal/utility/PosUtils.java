package yay.evy.everest.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.index.VStuffBlocks;


public class PosUtils {

    
    public static boolean isCompatibleWithType(ServerLevel level, BlockPos pos, RopeUtils.SelectType type) {
        return switch (type) {
            case NORMAL -> !(isPhysPulley(level, pos) || isPulleyAnchor(level, pos));
            case PULLEY -> isPulleyAnchor(level, pos);
        };
    }

    public static RopeUtils.BlockType getBlockType(ServerLevel level, BlockPos pos) {
        if (isPhysPulley(level, pos)) return RopeUtils.BlockType.PULLEY;
        if (isPulleyAnchor(level, pos)) return RopeUtils.BlockType.PULLEY_ANCHOR;
        else return RopeUtils.BlockType.NORMAL;
    }

    public static boolean isPhysPulley(BlockState state) {
        return state.getBlock().equals(VStuffBlocks.PHYS_PULLEY.get());
    }

    public static boolean isPulleyAnchor(BlockState state) {
        return state.getBlock().equals(VStuffBlocks.PULLEY_ANCHOR.get());
    }

    public static boolean isPhysPulley(Level level, BlockPos pos) {
        return isPhysPulley(level.getBlockState(pos));
    }

    public static boolean isPulleyAnchor(Level level, BlockPos pos) {
        return isPulleyAnchor(level.getBlockState(pos));
    }
}