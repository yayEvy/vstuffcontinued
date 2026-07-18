package dev.flarelog.vstuff.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.physics.levituff.LevituffBlock;
import dev.flarelog.vstuff.content.physics.levituff.RefinedLevituffBlock;
import dev.flarelog.vstuff.content.ships.reactionwheel.ReactionWheelBlock;
import dev.flarelog.vstuff.content.ships.thrust.MechanicalThrusterBlock;
import dev.flarelog.vstuff.infrastructure.config.VStress;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class VStuffBlocks  {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    static {
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final BlockEntry<MechanicalThrusterBlock> MECHANICAL_THRUSTER =
            REGISTRATE.block("mechanical_thruster", MechanicalThrusterBlock::new)
                    .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .transform(VStress.setImpact(8))
                    .item()
                    .transform(customItemModel())
                    .register();


    public static final BlockEntry<ReactionWheelBlock> REACTION_WHEEL =
            REGISTRATE.block("reaction_wheel", ReactionWheelBlock::new)
                    .initialProperties(AllBlocks.BRASS_BLOCK)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(pickaxeOnly())
                    .blockstate(BlockStateGen.directionalBlockProvider(true))
                    .transform(VStress.setImpact(4))
                    .item()
                    .transform(customItemModel())
                    .register();


    public static final BlockEntry<LevituffBlock> LEVITUFF =
            REGISTRATE.block("levituff", LevituffBlock::new)
                    .initialProperties(() -> Blocks.TUFF)
                    .transform(pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<RefinedLevituffBlock> REFINED_LEVITUFF =
            REGISTRATE.block("refined_levituff", RefinedLevituffBlock::new)
                    .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .transform(pickaxeOnly())
                    .simpleItem()
                    .register();


    public static void register() {}
}
