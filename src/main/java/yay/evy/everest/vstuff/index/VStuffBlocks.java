package yay.evy.everest.vstuff.index;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlock;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlock;

import static yay.evy.everest.vstuff.VStuff.REGISTRATE;

public class VStuffBlocks {

    static {
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final BlockEntry<RotationalThrusterBlock> ROTATIONAL_THRUSTER =
            REGISTRATE.block("rotational_thruster", RotationalThrusterBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .transform(BlockStressDefaults.setImpact(32.0))
                    .simpleItem()
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .register();

    public static final BlockEntry<PhysPulleyBlock> PHYS_PULLEY =
            REGISTRATE.block("phys_pulley", PhysPulleyBlock::new)
                    .properties(props -> BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK))
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .simpleItem()
                    .blockstate(BlockStateGen.horizontalBlockProvider(false))
                    .register();





    public static void register() {}
}
