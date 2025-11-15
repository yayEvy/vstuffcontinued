package yay.evy.everest.vstuff.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import yay.evy.everest.vstuff.VStuff;

public class VStuffPartialModels {

    public static final PartialModel

    CORE = model("item/core"),
    CORE_OUTER = model("item/core_outer"),
    CORE_INNER = model("item/core_inner"),
    GRABBER_CORE = model("item/core"),
    GRABBER_CORE_INNER = model("item/core_inner"),
    GRABBER_CORE_OUTER = model("item/core_outer")

            ;

    private static PartialModel model(String path) {
        return PartialModel.of(VStuff.asResource(path));
    }

    public static void init() {}
}

