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
import dev.flarelog.vstuff.content.physics.handle.HandlebarBlock;
import dev.flarelog.vstuff.content.physics.ships.nails.NailBlock;
import dev.flarelog.vstuff.content.physics.ships.nails.NailItem;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
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


    public static final BlockEntry<HandlebarBlock> HANDLE =
            REGISTRATE.block("handlebar", HandlebarBlock::new)
                    .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .transform(pickaxeOnly())
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .blockstate((ctx, prov) -> {
                        prov.getVariantBuilder(ctx.get()).forAllStates(state -> {
                            Direction facing = state.getValue(HandlebarBlock.FACING);
                            DyeColor color = state.getValue(HandlebarBlock.COLOR);
                            boolean rotated = state.getValue(HandlebarBlock.ROTATED);

                            int xRot = switch (facing) {
                                case DOWN -> 180;
                                case UP -> 0;
                                default -> 90;
                            };

                            int yRot = switch (facing) {
                                case SOUTH -> 180;
                                case WEST -> 270;
                                case EAST -> 90;
                                default -> 0;
                            };

                            if (rotated) {
                                if (facing.getAxis().isVertical()) {
                                    yRot = (yRot + 90) % 360;
                                } else {
                                    xRot = (xRot + 90) % 360;
                                }
                            }

                            return ConfiguredModel.builder()
                                    .modelFile(prov.models()
                                            .withExistingParent("handlebar_" + color.getName(),
                                                    prov.modLoc("block/handlebar"))
                                            .texture("0", "vstuff:block/handlebar/" + color.getName())
                                            .texture("particle", "vstuff:block/handlebar/" + color.getName()))
                                    .rotationX(xRot)
                                    .rotationY(yRot)
                                    .build();
                        });
                    })
                    .loot((lt, block) -> lt.add(block, LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .when(ExplosionCondition.survivesExplosion())
                                    .add(LootItem.lootTableItem(block)
                                            .apply(CopyBlockState.copyState(block)
                                                    .copy(HandlebarBlock.COLOR))))))
                    .item()
                    .model((ctx, prov) -> {
                        ItemModelBuilder builder = prov.withExistingParent(ctx.getName(),
                                prov.modLoc("block/handlebar_gray"));
                        ResourceLocation colorPredicate = new ResourceLocation("vstuff", "handlebar_color");
                        for (DyeColor dye : DyeColor.values()) {
                            builder.override()
                                    .predicate(colorPredicate, dye.getId() / 16f)
                                    .model(new ModelFile.UncheckedModelFile(
                                            prov.modLoc("block/handlebar_" + dye.getName())))
                                    .end();
                        }
                    })
                    .build()
                    .register();



    public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel() {
        return b -> b.model(AssetLookup::customItemModel)
                .build();
    }

    public static void register() {}
}
