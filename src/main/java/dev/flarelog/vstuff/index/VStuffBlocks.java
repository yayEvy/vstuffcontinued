package dev.flarelog.vstuff.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.flarelog.vstuff.content.physics.handle.HandlebarBlock;
import dev.flarelog.vstuff.content.physics.ships.nails.NailBlock;
import dev.flarelog.vstuff.content.physics.ships.nails.NailItem;
import dev.flarelog.vstuff.content.ropes.winch.WinchBlock;
import dev.flarelog.vstuff.content.ropes.winch.AnchorBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.content.physics.levituff.LevituffBlock;
import dev.flarelog.vstuff.content.physics.levituff.RefinedLevituffBlock;
import dev.flarelog.vstuff.content.physics.ships.reactionwheel.ReactionWheelBlock;
import dev.flarelog.vstuff.content.physics.ships.thrust.MechanicalThrusterBlock;
import dev.flarelog.vstuff.infrastructure.config.VStress;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

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

    public static final BlockEntry<HandlebarBlock> HANDLE_WHITE = registerHandlebar(DyeColor.WHITE);
    public static final BlockEntry<HandlebarBlock> HANDLE_ORANGE = registerHandlebar(DyeColor.ORANGE);
    public static final BlockEntry<HandlebarBlock> HANDLE_MAGENTA = registerHandlebar(DyeColor.MAGENTA);
    public static final BlockEntry<HandlebarBlock> HANDLE_LIGHT_BLUE = registerHandlebar(DyeColor.LIGHT_BLUE);
    public static final BlockEntry<HandlebarBlock> HANDLE_YELLOW = registerHandlebar(DyeColor.YELLOW);
    public static final BlockEntry<HandlebarBlock> HANDLE_LIME = registerHandlebar(DyeColor.LIME);
    public static final BlockEntry<HandlebarBlock> HANDLE_PINK = registerHandlebar(DyeColor.PINK);
    public static final BlockEntry<HandlebarBlock> HANDLE_GRAY = registerHandlebar(DyeColor.GRAY);
    public static final BlockEntry<HandlebarBlock> HANDLE_LIGHT_GRAY = registerHandlebar(DyeColor.LIGHT_GRAY);
    public static final BlockEntry<HandlebarBlock> HANDLE_CYAN = registerHandlebar(DyeColor.CYAN);
    public static final BlockEntry<HandlebarBlock> HANDLE_PURPLE = registerHandlebar(DyeColor.PURPLE);
    public static final BlockEntry<HandlebarBlock> HANDLE_BLUE = registerHandlebar(DyeColor.BLUE);
    public static final BlockEntry<HandlebarBlock> HANDLE_BROWN = registerHandlebar(DyeColor.BROWN);
    public static final BlockEntry<HandlebarBlock> HANDLE_GREEN = registerHandlebar(DyeColor.GREEN);
    public static final BlockEntry<HandlebarBlock> HANDLE_RED = registerHandlebar(DyeColor.RED);
    public static final BlockEntry<HandlebarBlock> HANDLE_BLACK = registerHandlebar(DyeColor.BLACK);


    private static BlockEntry<HandlebarBlock> registerHandlebar(DyeColor color) {
        String name = "handlebar_" + color.getName();
        String texture = "vstuff:block/handlebar/" + color.getName();

        return REGISTRATE.block(name, properties -> new HandlebarBlock(properties, color))
                .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                .transform(pickaxeOnly())
                .properties(BlockBehaviour.Properties::noOcclusion)
                .blockstate((ctx, prov) -> {
                    ModelFile horizontalModel = prov.models()
                            .withExistingParent(name, prov.modLoc("block/handlebar"))
                            .texture("0", texture)
                            .texture("particle", texture);

                    ModelFile verticalModel = prov.models()
                            .withExistingParent(name + "_vertical", prov.modLoc("block/handlebar_vertical"))
                            .texture("0", texture)
                            .texture("particle", texture);

                    prov.getVariantBuilder(ctx.get()).forAllStates(state -> {
                        Direction facing = state.getValue(HandlebarBlock.FACING);
                        boolean rotated = state.getValue(HandlebarBlock.ROTATED);

                        ModelFile model;
                        int xRot;
                        int yRot;

                        if (rotated && facing.getAxis().isHorizontal()) {
                            model = verticalModel;
                            xRot = 0;
                            yRot = switch (facing) {
                                case NORTH -> 90;
                                case EAST -> 180;
                                case SOUTH -> 270;
                                default -> 0; // WEST
                            };
                        } else {
                            model = horizontalModel;
                            xRot = switch (facing) {
                                case DOWN -> 180;
                                case UP -> 0;
                                default -> 90;
                            };

                            yRot = switch (facing) {
                                case SOUTH -> 180;
                                case WEST -> 270;
                                case EAST -> 90;
                                default -> 0;
                            };

                            if (rotated) {
                                yRot = (yRot + 90) % 360;
                            }
                        }

                        return ConfiguredModel.builder()
                                .modelFile(model)
                                .rotationX(xRot)
                                .rotationY(yRot)
                                .build();
                    });
                })
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + name)))
                .build()
                .register();
    }



    public static final BlockEntry<WinchBlock> WINCH =
            REGISTRATE.block("winch", WinchBlock::new)
                    .initialProperties(AllBlocks.ANDESITE_ALLOY_BLOCK)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(axeOrPickaxe())
                    .blockstate(BlockStateGen.directionalBlockProvider(true))
                    .transform(VStress.setImpact(4))
                    .item()
                    .transform(customItemModel())
                    .register();


    public static final BlockEntry<AnchorBlock> ANCHOR =
            REGISTRATE.block("anchor", AnchorBlock::new)
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
