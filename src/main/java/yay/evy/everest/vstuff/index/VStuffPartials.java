package yay.evy.everest.vstuff.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;


public class VStuffPartials {


    public static final PartialModel REACTION_WHEEL_CORE = partial("reaction_wheel/core");
    public static final PartialModel THRUSTER_FAN = partial("mechanical_thruster/fans");
    public static final PartialModel PULLEY_COIL = partial("phys_pulley/coil");

    private static PartialModel partial(String path) {
        return PartialModel.of(new ResourceLocation(VStuff.MOD_ID, "block/" + path));
    }

    public static void register() {}

}

