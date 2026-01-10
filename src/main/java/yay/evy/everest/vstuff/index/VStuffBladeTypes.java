package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import yay.evy.everest.vstuff.VStuff;


public enum VStuffBladeTypes {


    WOODEN_DOUBLE_BLADE("wooden_double_blade", 1.0, 0.3, 4.0, 2500f, VStuffPartialModels.getWoodenDoubleBlade()),
    WOODEN_QUAD_BLADE("wooden_quad_blade", 1.4, 0.3, 6.0, 5000f, null)
    ;

    CreateRegistrate REGISTRATE = VStuff.registrate();

    public String name;
    public double power;
    public double waterPower;
    public double suImpact;
    public float maxThrust;
    public PartialModel bladeModel;

    private VStuffBladeTypes(String name, double power, double waterPower, double suImpact, float maxThrust, PartialModel bladeModel) {
        this.name = name;
        this.power = power;
        this.waterPower = waterPower;
        this.suImpact = suImpact;
        this.maxThrust = maxThrust;
        this.bladeModel = bladeModel;
    }
}