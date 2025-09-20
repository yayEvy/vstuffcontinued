package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
//import yay.evy.everest.vstuff.ropes.RopePulleyBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlockEntity;

public class VStuffBlockEntities {

    private static final CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntityEntry<RotationalThrusterBlockEntity> ROTATIONAL_THRUSTER_BE =
            REGISTRATE.blockEntity("rotational_thruster", RotationalThrusterBlockEntity::new).register();
    public static void register() {}
}
