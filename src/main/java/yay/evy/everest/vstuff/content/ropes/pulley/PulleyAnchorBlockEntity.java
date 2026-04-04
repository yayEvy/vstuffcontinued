package yay.evy.everest.vstuff.content.ropes.pulley;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.content.ropes.IRopeActor;
import yay.evy.everest.vstuff.content.ropes.RopeManager;
import yay.evy.everest.vstuff.content.ropes.ReworkedRope;

public class PulleyAnchorBlockEntity extends BlockEntity implements IRopeActor {

    private Integer ropeId = null;
    private ReworkedRope rope = null;

    public PulleyAnchorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void connectRope(Integer ropeId, BlockState state, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (ropeId == null || !RopeManager.get(serverLevel).hasRope(ropeId)) return;

        this.ropeId = ropeId;
        this.rope = RopeManager.get(serverLevel).getRope(ropeId);

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
