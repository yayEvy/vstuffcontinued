package yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

public class LevituffBlockEntity extends BlockEntity {

    public LevituffBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {

            LoadedServerShip ship =
                    VSGameUtilsKt.getShipObjectManagingPos(serverLevel, worldPosition);

            if (ship != null) {
                LevituffAttachment attachment =
                        LevituffAttachment.getOrCreateAsAttachment(ship);

                attachment.addApplier(
                        worldPosition,
                        new LevituffForceApplier(100000.0)
                );
            }
        }
    }
    @Override
    public void setRemoved() {

        if (level instanceof ServerLevel serverLevel) {

            LoadedServerShip ship =
                    VSGameUtilsKt.getShipObjectManagingPos(serverLevel, worldPosition);

            if (ship != null) {
                LevituffAttachment attachment =
                        LevituffAttachment.getOrCreateAsAttachment(ship);

                attachment.removeApplier(serverLevel, worldPosition);
            }
        }

        super.setRemoved();
    }
}