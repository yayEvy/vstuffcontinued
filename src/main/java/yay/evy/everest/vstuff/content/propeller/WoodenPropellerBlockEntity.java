package yay.evy.everest.vstuff.content.propeller;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.content.propeller.base.AbstractPropellerBlockEntity;
import yay.evy.everest.vstuff.index.VStuffBlocks;
import yay.evy.everest.vstuff.index.VStuffPartialModels;

public class WoodenPropellerBlockEntity extends AbstractPropellerBlockEntity {

    public WoodenPropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public PartialModel getPartialBladeModel() {
        return this.bladeModel;
    }
    @Override
    public void setPartialBladeModel(PartialModel model) {
        bladeModel = model;
    }
    @Override
    public double getPower() {
        return power;
    }
    @Override
    public void setPower(double setTo) {
        power = setTo;
    }
    @Override
    public double getWaterPower() {
        return waterPower;
    }
    @Override
    public void setWaterPower(double setTo) {
        waterPower = setTo;
    }
    @Override
    public double getStressImpact() {
        return stressImpact;
    }
    @Override
    public void setStressImpact(double setTo) {
        stressImpact = setTo;
    }
    @Override
    public double getMaxThrust() {
        return maxThrust;
    }
    @Override
    public void setMaxThrust(double setTo) {
        stressImpact = setTo;
    }
}