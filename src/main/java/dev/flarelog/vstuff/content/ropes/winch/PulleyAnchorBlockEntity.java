package dev.flarelog.vstuff.content.ropes.winch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import dev.flarelog.vstuff.content.ropes.util.IRopeActor;
import dev.flarelog.vstuff.content.ropes.RopeManager;
import dev.flarelog.vstuff.content.ropes.Rope;

public class PulleyAnchorBlockEntity extends BlockEntity implements IRopeActor {

    private Integer ropeId = null;
    private Rope rope = null;

    public PulleyAnchorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void connectRope(Integer ropeId) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (ropeId == null || !RopeManager.get(serverLevel).hasRope(ropeId)) return;

        this.ropeId = ropeId;
        this.rope = RopeManager.get(serverLevel).getRope(ropeId);

        blockConnect(this.getActorBlockState(), level, this.getPos());

        setChanged();
    }

    @Override
    public void removeRope(Integer ropeId) {
        this.ropeId = null;
        this.rope = null;

        blockRemove(this.getActorBlockState(), level, this.getPos());

        setChanged();
    }

    @Override
    public BlockState getActorBlockState() {
        return getBlockState();
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }
}
