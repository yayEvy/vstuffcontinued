package yay.evy.everest.vstuff.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import yay.evy.everest.vstuff.VStuff;

public class VStuffPartialModels {

    public static final PartialModel

    CORE = model("item/energy_core/core"),
    CORE_OUTER = model("item/energy_core/core_outer"),
    CORE_INNER = model("item/energy_core/core_inner"),
    GRABBER_CORE = model("item/phys_grabber/core"),
    GRABBER_CORE_INNER = model("item/phys_grabber/core_inner"),
    GRABBER_CORE_OUTER = model("item/phys_grabber/core_outer")

            ;

    private static PartialModel model(String path) {
        return PartialModel.of(VStuff.asResource(path));
    }

    public static void init() {}
}

