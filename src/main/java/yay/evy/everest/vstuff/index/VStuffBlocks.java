package yay.evy.everest.vstuff.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.physicsmanipulationshenanigans.levituff.LevituffBlock;
import yay.evy.everest.vstuff.content.ships.reactionwheel.ReactionWheelBlock;
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlock;
import yay.evy.everest.vstuff.content.ropes.pulley.PulleyAnchorBlock;
import yay.evy.everest.vstuff.content.ships.thrust.MechanicalThrusterBlock;
import yay.evy.everest.vstuff.infrastructure.config.VStress;

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


    public static final BlockEntry<PhysPulleyBlock> PHYS_PULLEY =
            REGISTRATE.block("phys_pulley", PhysPulleyBlock::new)
                    .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .transform(VStress.setImpact(4))
                    .item()
                    .transform(customItemModel())
                    .register();


    public static final BlockEntry<PulleyAnchorBlock> PULLEY_ANCHOR =
            REGISTRATE.block("pulley_anchor", PulleyAnchorBlock::new)
                    .initialProperties(AllBlocks.ZINC_BLOCK)
                    .transform(pickaxeOnly())
                    .blockstate(BlockStateGen.directionalBlockProvider(false))
                    .item()
                    .build()
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


    public static void register() {}
}
