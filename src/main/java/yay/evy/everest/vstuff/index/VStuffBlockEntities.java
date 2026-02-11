package yay.evy.everest.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.LevituffBlockEntity;
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlockEntity;
import yay.evy.everest.vstuff.content.ropes.pulley.PulleyAnchorBlockEntity;
import yay.evy.everest.vstuff.content.thrust.MechanicalThrusterBlockEntity;

public class VStuffBlockEntities {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntityEntry<MechanicalThrusterBlockEntity> MECHANICAL_THRUSTER_BE =
            REGISTRATE.blockEntity("mechanical_thruster", MechanicalThrusterBlockEntity::new)
                    .visual(() -> SingleAxisRotatingVisual::shaft, false)
                    .validBlocks(VStuffBlocks.MECHANICAL_THRUSTER)
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



    public static final BlockEntityEntry<LevituffBlockEntity> LEVITUFF_BE =
            REGISTRATE.blockEntity("levituff", LevituffBlockEntity::new)
                    .validBlocks(VStuffBlocks.LEVITUFF)
                    .register();
    public static void register() {}
}
