package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.rope.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.rope.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlockEntity;

public class VStuffBlockEntities {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntityEntry<RotationalThrusterBlockEntity> ROTATIONAL_THRUSTER_BE =
            REGISTRATE.blockEntity("rotational_thruster", RotationalThrusterBlockEntity::new)
                    .visual(() -> SingleAxisRotatingVisual::shaft, false)
                    .validBlocks(VStuffBlocks.ROTATIONAL_THRUSTER)
                    .renderer(() -> ShaftRenderer::new)
                    .register();

    public static final BlockEntityEntry<PhysPulleyBlockEntity> PHYS_PULLEY_BE =
            REGISTRATE.blockEntity("phys_pulley", PhysPulleyBlockEntity::new)
                    .visual(() -> SingleAxisRotatingVisual::shaft, false)
                    .validBlocks(VStuffBlocks.PHYS_PULLEY)
                    .renderer(() -> ShaftRenderer::new)
                    .register();

    public static final BlockEntityEntry<PulleyAnchorBlockEntity> PULLEY_ANCHOR_BE =
            REGISTRATE.blockEntity("pulley_anchor", PulleyAnchorBlockEntity::new)
                    .validBlocks(VStuffBlocks.PULLEY_ANCHOR)
                    .register();

    public static void register() {}
}
