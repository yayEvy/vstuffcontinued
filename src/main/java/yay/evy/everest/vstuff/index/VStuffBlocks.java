package yay.evy.everest.vstuff.index;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
                    .transform(b -> {
                        b.onRegister(block -> BlockStressValues.setGeneratorSpeed(32).accept(block));
                        return b;
                    })
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .register();

    public static final ItemEntry<BlockItem> ROTATIONAL_THRUSTER_ITEM =
            REGISTRATE.item("rotational_thruster",
                            b -> new BlockItem(ROTATIONAL_THRUSTER.get(), new Item.Properties()))
                    .register();

    public static final BlockEntry<PhysPulleyBlock> PHYS_PULLEY =
            REGISTRATE.block("phys_pulley", PhysPulleyBlock::new)
                    .properties(props -> BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK))
                    .transform(b -> {
                        b.onRegister(block -> BlockStressValues.setGeneratorSpeed(4).accept(block));
                        return b;
                    })
                    .blockstate(BlockStateGen.horizontalBlockProvider(false))
                    .register();

    public static final ItemEntry<BlockItem> PHYS_PULLEY_ITEM =
            REGISTRATE.item("phys_pulley",
                            b -> new BlockItem(PHYS_PULLEY.get(), new Item.Properties()))
                    .register();

    public static void register() {}
}
