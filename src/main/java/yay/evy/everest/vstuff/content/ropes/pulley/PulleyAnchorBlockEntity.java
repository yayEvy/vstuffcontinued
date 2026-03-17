package yay.evy.everest.vstuff.content.ropes.pulley;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.content.ropes.IRopeActor;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;
import yay.evy.everest.vstuff.content.ropes.RopeManager;

public class PulleyAnchorBlockEntity extends BlockEntity implements IRopeActor {

    private Integer ropeId = null;
    private ReworkedRope rope = null;

    public PulleyAnchorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void connectRope(Integer ropeId, BlockState state, Level level, BlockPos pos) {
        if (ropeId == null || RopeManager.getRope(ropeId) == null) return;

        this.ropeId = ropeId;
        this.rope = RopeManager.getRope(ropeId);

        blockConnect(state, level, pos);

        setChanged();
    }

    @Override
    public void removeRope(Integer ropeId, BlockState state, Level level, BlockPos pos) {
        this.ropeId = null;
        this.rope = null;

        blockRemove(state, level, pos);

        setChanged();
    }

    @Override
    public BlockState getActorBlockState() {
        return getBlockState();
    }
}
