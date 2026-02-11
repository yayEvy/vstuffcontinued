package yay.evy.everest.vstuff.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.VStuff;


public class VStuffPartials {


    public static final PartialModel REACTION_WHEEL_CORE = partial("reaction_wheel_core");

    private static PartialModel partial(String path) {
        return PartialModel.of(new ResourceLocation(VStuff.MOD_ID, "partial/" + path));
    }

    public static void register() {}

}

