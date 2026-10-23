package dev.flarelog.vstuff.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.physics.levituff.LevituffBlockEntity;
import dev.flarelog.vstuff.content.physics.levituff.RefinedLevituffBlockEntity;
import dev.flarelog.vstuff.content.physics.ships.reactionwheel.ReactionWheelBlockEntity;
import dev.flarelog.vstuff.content.physics.ships.reactionwheel.ReactionWheelRenderer;
import dev.flarelog.vstuff.content.physics.ships.reactionwheel.ReactionWheelVisual;
import dev.flarelog.vstuff.content.physics.ships.thrust.MechanicalThrusterBlockEntity;
import dev.flarelog.vstuff.content.physics.ships.thrust.MechanicalThrusterRenderer;
import dev.flarelog.vstuff.content.physics.ships.thrust.MechanicalThrusterVisual;
import dev.flarelog.vstuff.content.ropes.winch.WinchBlockEntity;
import dev.flarelog.vstuff.content.ropes.winch.WinchRenderer;
import dev.flarelog.vstuff.content.ropes.winch.WinchVisual;
import dev.flarelog.vstuff.content.ropes.winch.AnchorBlockEntity;

public class VStuffBlockEntities {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntityEntry<MechanicalThrusterBlockEntity> MECHANICAL_THRUSTER_BE =
            REGISTRATE.blockEntity("mechanical_thruster", MechanicalThrusterBlockEntity::new)
                    .visual(() -> MechanicalThrusterVisual::new, false)
                    .validBlocks(VStuffBlocks.MECHANICAL_THRUSTER)
                    .renderer(() -> MechanicalThrusterRenderer::new)
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

    public static final BlockEntityEntry<WinchBlockEntity> WINCH_BE =
            REGISTRATE.blockEntity("winch", WinchBlockEntity::new)
                    .visual(() -> WinchVisual::new, false)
                    .validBlocks(VStuffBlocks.WINCH)
                    .renderer(() -> WinchRenderer::new)
                    .register();

    public static final BlockEntityEntry<AnchorBlockEntity> ANCHOR_BE =
            REGISTRATE.blockEntity("anchor", AnchorBlockEntity::new)
                    .validBlocks(VStuffBlocks.ANCHOR)
                    .register();

    public static void register() {}
}
