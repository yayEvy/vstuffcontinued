package yay.evy.everest.vstuff.index;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.material.MapColor;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.pulley.PhysPulleyBlock;
import yay.evy.everest.vstuff.content.pulley.PulleyAnchorBlock;
import yay.evy.everest.vstuff.content.thrust.RotationalThrusterBlock;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class VStuffBlocks  {

    static CreateRegistrate REGISTRATE = VStuff.registrate();

    public static final BlockEntry<RotationalThrusterBlock> ROTATIONAL_THRUSTER =
            REGISTRATE.block("rotational_thruster", RotationalThrusterBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .transform(b -> b
                            .onRegister(block -> BlockStressValues.setGeneratorSpeed(32).accept(block)))
                    .item()
                    .model(AssetLookup.itemModel("rotational_thruster.json"))
                    .build()
                    .register();

    public static final BlockEntry<PhysPulleyBlock> PHYS_PULLEY =
            REGISTRATE.block("phys_pulley", PhysPulleyBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(props -> BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK))
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .transform(b -> b
                            .onRegister(block -> BlockStressValues.setGeneratorSpeed(4).accept(block)))
                    .item()
                    .model(AssetLookup.itemModel("phys_pulley.json"))
                    .build()
                    .register();

    public static final BlockEntry<PulleyAnchorBlock> PULLEY_ANCHOR =
            REGISTRATE.block("pulley_anchor", PulleyAnchorBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .transform(pickaxeOnly())
                    .blockstate(BlockStateGen.horizontalBlockProvider(true))
                    .item()
                    .model(AssetLookup.itemModel("pulley_anchor.json"))
                    .build()
                    .register();

    public static void register() {}
}
