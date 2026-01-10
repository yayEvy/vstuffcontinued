package yay.evy.everest.vstuff.content.propeller;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import yay.evy.everest.vstuff.content.propeller.base.AbstractBladeItem;
import yay.evy.everest.vstuff.index.VStuffBladeTypes;
import yay.evy.everest.vstuff.index.VStuffPartialModels;

public class WoodenDoubleBladeItem extends AbstractBladeItem {

    public WoodenDoubleBladeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public PartialModel getBladeModel() {
        return VStuffBladeTypes.WOODEN_DOUBLE_BLADE.bladeModel;
    }

    @Override
    public double getPower() {
        return VStuffBladeTypes.WOODEN_DOUBLE_BLADE.power;
    }

    @Override
    public double getWaterPower() {
        return VStuffBladeTypes.WOODEN_DOUBLE_BLADE.waterPower;
    }

    @Override
    public double getStressImpact() {
        return VStuffBladeTypes.WOODEN_DOUBLE_BLADE.suImpact;
    }

    @Override
    public double getMaxThrust() {
        return VStuffBladeTypes.WOODEN_DOUBLE_BLADE.maxThrust;
    }
}