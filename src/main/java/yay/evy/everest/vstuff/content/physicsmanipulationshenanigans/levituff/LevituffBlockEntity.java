package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.sound.LevituffSoundPlayer;
import yay.evy.everest.vstuff.infrastructure.config.VStuffConfigs;
import yay.evy.everest.vstuff.internal.utility.ShipUtils;

public class LevituffBlockEntity extends BlockEntity {

    public LevituffBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, getBlockPos());

            if (ship != null) {
                LevituffAttachment attachment = LevituffAttachment.getOrCreateAsAttachment(ship);

                attachment.addBlock(getBlockPos());
            }
        }
    }
    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            LoadedServerShip ship = ShipUtils.getLoadedServerShipAtPos(serverLevel, getBlockPos());

            if (ship != null) {
                LevituffAttachment attachment = LevituffAttachment.getOrCreateAsAttachment(ship);

                attachment.removeBlock(getBlockPos());
            }
        }

        super.setRemoved();
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