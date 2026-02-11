package yay.evy.everest.vstuff.index;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.level.material.MapColor;
import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.reaction_wheel.ReactionWheelBlock;
import yay.evy.everest.vstuff.content.ropes.pulley.PhysPulleyBlock;
import yay.evy.everest.vstuff.content.ropes.pulley.PulleyAnchorBlock;
import yay.evy.everest.vstuff.content.thrust.MechanicalThrusterBlock;

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
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.mapColor(MapColor.COLOR_YELLOW)
                            .noOcclusion())
                    .addLayer(() -> RenderType::cutout)
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.directionalAxisBlockProvider())
                    .transform(b -> b
                            .onRegister(block -> BlockStressValues.setGeneratorSpeed(32).accept(block)))
                    .item()
                    .model(AssetLookup.itemModel("mechanical_thruster.json"))
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
                    .blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
                            .getExistingFile(p.modLoc("block/pulley_anchor/block"))))
                    .item()
                    .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/pulley_anchor/block")))
                    .build()
                    .register();


    public static final BlockEntry<ReactionWheelBlock> REACTION_WHEEL_BLOCK = REGISTRATE.block("reaction_wheel", ReactionWheelBlock::new)
            .properties(p -> p.noOcclusion())
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
                    .getExistingFile(p.modLoc("block/reaction_wheel/block"))))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/pulley_anchor/block")))
            .build()
            .register();
    public static void register() {}
}
