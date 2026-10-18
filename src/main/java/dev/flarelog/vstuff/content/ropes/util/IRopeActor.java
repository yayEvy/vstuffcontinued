package dev.flarelog.vstuff.content.ropes.util;

import dev.flarelog.vstuff.content.ropes.data.LocalPosAndBodyId;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;


public interface IRopeActor {
    BooleanProperty CONNECTED = BooleanProperty.create("connected");

    void connectRope(Integer rope, BlockState state, Level level, BlockPos pos);
    void removeRope(Integer ropeId, BlockState state, Level level, BlockPos pos);
    BlockState getActorBlockState();
    BlockPos getPos();

    default boolean canAttach() {
        return this.getActorBlockState().hasProperty(CONNECTED) && !this.getActorBlockState().getValue(CONNECTED);
    }

    default void blockConnect(BlockState state, Level level, BlockPos pos) {
        level.setBlock(pos, state.setValue(CONNECTED, true), 3);
    }

    default void blockRemove(BlockState state, Level level, BlockPos pos) {
        level.setBlock(pos, state.setValue(CONNECTED, false), 3);
    }

    static boolean canAttach(BlockEntity be) {
        if (!(be instanceof IRopeActor actor)) return true;
        return actor.canAttach();
    }

    static IRopeActor getAt(ServerLevel level, BlockPos pos) {
        return pos != null && level.getBlockEntity(pos) instanceof IRopeActor a ? a : null;
    }

    static IRopeActor getAt(ServerLevel level, LocalPosAndBodyId posAndId) {
        BlockPos pos = posAndId.blockPos(level);
        return getAt(level, pos);
    }
}
