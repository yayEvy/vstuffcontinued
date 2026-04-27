package yay.evy.everest.vstuff.content.physics.levituff;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import yay.evy.everest.vstuff.content.physics.levituff.attachment.RefinedLevituffAttachment;
import yay.evy.everest.vstuff.content.physics.levituff.sound.LevituffSoundPlayer;
import yay.evy.everest.vstuff.internal.utility.AttachmentUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class RefinedLevituffBlockEntity extends BlockEntity {
    public RefinedLevituffBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AttachmentUtils.getOrCreateAttachment(level, getBlockPos(), RefinedLevituffAttachment.class, RefinedLevituffAttachment::new, a -> a.addBlock(getBlockPos()));
    }

    @Override
    public void setRemoved() {
        AttachmentUtils.getAttachment(level, getBlockPos(), RefinedLevituffAttachment.class, a -> a.removeBlock(getBlockPos()), a -> a.levituffBlocks.isEmpty());
        super.setRemoved();
    }

    private final LevituffSoundPlayer soundPlayer = new LevituffSoundPlayer();

    public static void tick(Level level, BlockPos pos, RefinedLevituffBlockEntity be) {
        if (level.isClientSide) {
            AtomicInteger count = new AtomicInteger(1);
            AttachmentUtils.getAttachment(level, pos, RefinedLevituffAttachment.class, a -> count.set(a.levituffBlocks.size()));
            be.soundPlayer.tick(level, pos, count.get());
        }
    }
}
