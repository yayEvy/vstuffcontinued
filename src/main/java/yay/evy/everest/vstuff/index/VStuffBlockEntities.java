package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlockEntity;

public class VStuffBlockEntities {

    private static final CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntityEntry<RotationalThrusterBlockEntity> ROTATIONAL_THRUSTER_BE =
            REGISTRATE.blockEntity("rotational_thruster", RotationalThrusterBlockEntity::new)
                    .validBlocks(VStuffBlocks.ROTATIONAL_THRUSTER)
                    .register();

    public static final BlockEntityEntry<PhysPulleyBlockEntity> PHYS_PULLEY_BE =
            REGISTRATE.blockEntity("phys_pulley", PhysPulleyBlockEntity::new)
                    .validBlocks(VStuffBlocks.PHYS_PULLEY)
                    .register();


    public static void register() {}
}
