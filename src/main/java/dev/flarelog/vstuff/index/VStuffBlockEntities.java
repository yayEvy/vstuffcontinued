package dev.flarelog.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.physics.levituff.LevituffBlockEntity;
import dev.flarelog.vstuff.content.physics.levituff.RefinedLevituffBlockEntity;
import dev.flarelog.vstuff.content.ships.reactionwheel.ReactionWheelBlockEntity;
import dev.flarelog.vstuff.content.ships.reactionwheel.ReactionWheelRenderer;
import dev.flarelog.vstuff.content.ships.reactionwheel.ReactionWheelVisual;
import dev.flarelog.vstuff.content.ropes.pulley.PhysPulleyBlockEntity;
import dev.flarelog.vstuff.content.ropes.pulley.PhysPulleyRenderer;
import dev.flarelog.vstuff.content.ropes.pulley.PhysPulleyVisual;
import dev.flarelog.vstuff.content.ropes.pulley.PulleyAnchorBlockEntity;
import dev.flarelog.vstuff.content.ships.thrust.MechanicalThrusterBlockEntity;
import dev.flarelog.vstuff.content.ships.thrust.MechanicalThrusterRenderer;
import dev.flarelog.vstuff.content.ships.thrust.MechanicalThrusterVisual;

public class VStuffBlockEntities {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntityEntry<MechanicalThrusterBlockEntity> MECHANICAL_THRUSTER_BE =
            REGISTRATE.blockEntity("mechanical_thruster", MechanicalThrusterBlockEntity::new)
                    .visual(() -> MechanicalThrusterVisual::new, false)
                    .validBlocks(VStuffBlocks.MECHANICAL_THRUSTER)
                    .renderer(() -> MechanicalThrusterRenderer::new)
                    .register();

    public static final BlockEntityEntry<PhysPulleyBlockEntity> PHYS_PULLEY_BE =
            REGISTRATE.blockEntity("phys_pulley", PhysPulleyBlockEntity::new)
                    .visual(() -> PhysPulleyVisual::new, false)
                    .validBlocks(VStuffBlocks.PHYS_PULLEY)
                    .renderer(() -> PhysPulleyRenderer::new)
                    .register();

    public static final BlockEntityEntry<PulleyAnchorBlockEntity> PULLEY_ANCHOR_BE =
            REGISTRATE.blockEntity("pulley_anchor", PulleyAnchorBlockEntity::new)
                    .validBlocks(VStuffBlocks.PULLEY_ANCHOR)
                    .register();

    public static final BlockEntityEntry<ReactionWheelBlockEntity> REACTION_WHEEL_BE =
            REGISTRATE.blockEntity("reaction_wheel", ReactionWheelBlockEntity::new)
                    .visual(() -> ReactionWheelVisual::new, false)
                    .validBlocks(VStuffBlocks.REACTION_WHEEL)
                    .renderer(() -> ReactionWheelRenderer::new)
                    .register();

    public static final BlockEntityEntry<LevituffBlockEntity> LEVITUFF_BE =
            REGISTRATE.blockEntity("levituff", LevituffBlockEntity::new)
                    .validBlocks(VStuffBlocks.LEVITUFF)
                    .register();

    public static final BlockEntityEntry<RefinedLevituffBlockEntity> REFINED_LEVITUFF_BE =
            REGISTRATE.blockEntity("refined_levituff", RefinedLevituffBlockEntity::new)
                    .validBlocks(VStuffBlocks.REFINED_LEVITUFF)
                    .register();

    public static void register() {}
}
