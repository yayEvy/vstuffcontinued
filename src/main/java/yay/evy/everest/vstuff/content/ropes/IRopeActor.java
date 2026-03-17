package yay.evy.everest.vstuff.content.ropes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;


public interface IRopeActor {
    BooleanProperty CONNECTED = BooleanProperty.create("connected");

    void connectRope(Integer rope, BlockState state, Level level, BlockPos pos);
    void removeRope(Integer ropeId, BlockState state, Level level, BlockPos pos);
    BlockState getActorBlockState();

    default void blockConnect(BlockState state, Level level, BlockPos pos) {
        level.sendBlockUpdated(pos, state, state.setValue(CONNECTED, true), 3);
    }

    default void blockRemove(BlockState state, Level level, BlockPos pos) {
        level.sendBlockUpdated(pos, state, state.setValue(CONNECTED, false), 3);
    }

    static boolean canActorAttach(BlockState state) {
        return state.hasProperty(CONNECTED) && !state.getValue(CONNECTED);
        // true ONLY for unconnected actors
    }

    static boolean canAttach(BlockState state) {
        if (state.hasProperty(CONNECTED)) {
            return !state.getValue(CONNECTED);
        }
        return true; // true for all normal blocks + unconnected actors
    }
}
