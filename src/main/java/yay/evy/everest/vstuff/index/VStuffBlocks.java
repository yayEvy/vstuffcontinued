package yay.evy.everest.vstuff.index;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlock;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;


public class VStuffBlocks  {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    static {
        REGISTRATE.setCreativeTab(VStuffCreativeModeTabs.VSTUFF_MAIN);
    }

    public static final BlockEntry<RotationalThrusterBlock> ROTATIONAL_THRUSTER =
            REGISTRATE.block("rotational_thruster", RotationalThrusterBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .transform(b -> b
                            .onRegister(block -> BlockStressValues.setGeneratorSpeed(32).accept(block)))
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .transform(axeOrPickaxe())
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<PhysPulleyBlock> PHYS_PULLEY =
            REGISTRATE.block("phys_pulley", PhysPulleyBlock::new)
                    .properties(props -> BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK))
                    .transform(b -> b
                            .onRegister(block -> BlockStressValues.setGeneratorSpeed(4).accept(block)))
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .transform(axeOrPickaxe())
                    .item()
                    .transform(customItemModel())
                    .register();

    public static void register() {}
}
