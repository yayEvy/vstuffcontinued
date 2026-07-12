package dev.flarelog.vstuff.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.flarelog.vstuff.VStuff;


public class VStuffPartialModels {

    public static final PartialModel REACTION_WHEEL_CORE = block("reaction_wheel/core");
    public static final PartialModel THRUSTER_FAN = block("mechanical_thruster/fans");
    public static final PartialModel PULLEY_COIL = block("phys_pulley/coil");

    private static PartialModel block(String path) {
        return PartialModel.of(VStuff.asResource("block/" + path));
    }

    public static void register() {

    }
}

