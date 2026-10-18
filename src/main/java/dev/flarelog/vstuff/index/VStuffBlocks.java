package dev.flarelog.vstuff.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.flarelog.vstuff.content.physics.ships.nails.NailBlock;
import dev.flarelog.vstuff.content.physics.ships.nails.NailItem;
import dev.flarelog.vstuff.content.ropes.winch.PhysPulleyBlock;
import dev.flarelog.vstuff.content.ropes.winch.PulleyAnchorBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.physics.levituff.LevituffBlock;
import dev.flarelog.vstuff.content.physics.levituff.RefinedLevituffBlock;
import dev.flarelog.vstuff.content.physics.ships.reactionwheel.ReactionWheelBlock;
import dev.flarelog.vstuff.content.physics.ships.thrust.MechanicalThrusterBlock;
import dev.flarelog.vstuff.infrastructure.config.VStress;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.util.ForgeSoundType;
import org.valkyrienskies.core.internal.config.ConfigEntry;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static dev.flarelog.vstuff.content.physics.ships.nails.NailItem.nailSoundType;

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

    public static final BlockEntry<NailBlock> NAIL_BLOCK =
            REGISTRATE.block("nail_block", NailBlock::new)
                    .initialProperties(AllBlocks.ANALOG_LEVER)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.sound(nailSoundType))
                    .blockstate((ctx, prov) -> {

                        ModelFile model = new ModelFile.UncheckedModelFile(prov.modLoc("block/nail_block"));
//
                        // prov.horizontalBlock(ctx.get(),model);
                       prov.directionalBlock(ctx.get(), model);

//                        prov.getVariantBuilder(ctx.get()).forAllStates(state -> {
//                            Direction facing = state.getValue(NailBlock.FACING);
//                            int xRot = 0;
//                            int yRot = 0;
//
//                            switch (facing) {
//                                case DOWN  -> { xRot = 90;  yRot = 0;   } // Pointing Down
//                                case UP    -> { xRot = -90; yRot = 0;   } // Pointing Up
//                                case NORTH -> { xRot = 0;   yRot = 0;   } // Base position (0, 0)
//                                case SOUTH -> { xRot = 0;   yRot = 180; } // Rotated 180 around Y
//                                case WEST  -> { xRot = 0;   yRot = 270; } // Rotated 270 around Y
//                                case EAST  -> { xRot = 0;   yRot = 90;  } // Rotated 90 around Y
//                            }
//
//                            return ConfiguredModel.builder()
//                                    .modelFile(model)
//                                    .rotationX(xRot)
//                                    .rotationY(yRot)
//                                    .build();
//                        });
                    })







                    .transform(axeOrPickaxe())
                    .item(NailItem::new)
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

    public static final BlockEntry<LevituffBlock> LEVITUFF_BRICKS =
            REGISTRATE.block("levituff_bricks", LevituffBlock::new)
                    .initialProperties(() -> Blocks.TUFF)
                    .transform(pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<LevituffBlock> POLISHED_LEVITUFF =
            REGISTRATE.block("polished_levituff", LevituffBlock::new)
                    .initialProperties(() -> Blocks.TUFF)
                    .transform(pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<LevituffBlock> CHISELED_LEVITUFF =
            REGISTRATE.block("chiseled_levituff", LevituffBlock::new)
                    .initialProperties(() -> Blocks.TUFF)
                    .transform(pickaxeOnly())
                    .simpleItem()
                    .register();

    public static final BlockEntry<LevituffBlock> CHISELED_LEVITUFF_BRICKS =
            REGISTRATE.block("chiseled_levituff_bricks", LevituffBlock::new)
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

    /*
    public static final BlockEntry<HandleBlock> HANDLE =
            REGISTRATE.block("handle", HandleBlock::new)
                    .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .transform(pickaxeOnly())
                    .simpleItem()
                    .register();

     */

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


    public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel() {
        return b -> b.model(AssetLookup::customItemModel)
                .build();
    }

    public static void register() {}
}
