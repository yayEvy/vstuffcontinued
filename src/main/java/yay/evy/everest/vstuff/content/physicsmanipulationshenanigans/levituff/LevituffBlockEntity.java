package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.sound.LevituffSoundPlayer;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

import java.util.function.Consumer;

public class LevituffBlockEntity extends BlockEntity {

    public LevituffBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        withAttachment(a -> a.addBlock(getBlockPos()));
    }
    @Override
    public void setRemoved() {
        withAttachment(a -> a.removeBlock(getBlockPos()));
        super.setRemoved();
    }

    private void withAttachment(Consumer<LevituffAttachment> action) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, getBlockPos());
        if (ship == null) return;
        action.accept(LevituffAttachment.getOrCreateAsAttachment(ship));
    }

    private final LevituffSoundPlayer soundPlayer = new LevituffSoundPlayer();

    public static void tick(Level level, BlockPos pos, LevituffBlockEntity be) {
        if (level.isClientSide) {
            int count = 1;
            if (level instanceof ServerLevel serverLevel) {
                LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, be.getBlockPos());
                if (ship != null) {
                    LevituffAttachment attachment = LevituffAttachment.get(level, pos);
                    if (attachment != null) count = attachment.levituffBlocks.size();
                }
            }
            be.soundPlayer.tick(level, pos, count);
        }
    }
}