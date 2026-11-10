package dev.flarelog.vstuff.content.physics.levituff;

import dev.flarelog.vstuff.index.VStuffAdvancements;
import dev.flarelog.vstuff.index.VStuffBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import dev.flarelog.vstuff.content.physics.levituff.attachment.LevituffAttachment;
import dev.flarelog.vstuff.content.physics.levituff.sound.LevituffSoundPlayer;
import dev.flarelog.vstuff.internal.utility.AttachmentUtils;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LevituffBlockEntity extends BlockEntity {

    public LevituffBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AttachmentUtils.getOrCreateAttachment(level, getBlockPos(), LevituffAttachment.class, LevituffAttachment::new, a -> a.addBlock(getBlockPos()));
    }
    @Override
    public void setRemoved() {
        AttachmentUtils.getAttachment(level, getBlockPos(), LevituffAttachment.class, a -> a.removeBlock(getBlockPos()), a -> a.levituffBlocks.isEmpty());
        super.setRemoved();
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        if (event.getState().is(VStuffBlocks.LEVITUFF.get())) {
            VStuffAdvancements.awardFirstLevituff(player);
        }
    }

    private final LevituffSoundPlayer soundPlayer = new LevituffSoundPlayer();

    public static void tick(Level level, BlockPos pos, LevituffBlockEntity be) {
        if (level.isClientSide) {
            AtomicInteger count = new AtomicInteger(1);
            AtomicBoolean isLeader = new AtomicBoolean(true);
            AttachmentUtils.getAttachment(level, pos, LevituffAttachment.class, a -> {
                count.set(a.levituffBlocks.size());
                long myPos = pos.asLong();
                long minPos = a.levituffBlocks.stream()
                        .mapToLong(v -> BlockPos.asLong((int) v.x, (int) v.y, (int) v.z))
                        .min()
                        .orElse(myPos);
                isLeader.set(minPos == myPos);
            });
            if (isLeader.get()) {
                be.soundPlayer.tick(level, pos, count.get());
            }
        }
    }
}