package yay.evy.everest.vstuff.impl.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import yay.evy.everest.vstuff.api.data.DataProviderContext;
import yay.evy.everest.vstuff.api.data.RopeRestylingProvider;

import java.util.List;

import static yay.evy.everest.vstuff.infrastructure.data.DatagenUtils.*;

public class RopeRestylingProviderImpl extends RopeRestylingProvider {
    public RopeRestylingProviderImpl(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void restyling(@NotNull DataProviderContext ctx) {
        restyles(ctx, listOfLists(DYE_ITEMS), resourceList(dyes), "vstuff:dyed_styles");
        restyles(ctx, zipToList(WOOL_ITEMS, DYE_ITEMS), resourceList(wools), "vstuff:wool_styles");
        restyles(ctx, zipToList(LOG_ITEMS, WOOD_ITEMS), resourceList(logs), "vstuff:log_styles");
    }

    @Override
    public @NotNull String getName() {
        return "vstuff_roperestyles";
    }

    static final List<Item> DYE_ITEMS = List.of(
            Items.RED_DYE,
            Items.ORANGE_DYE,
            Items.YELLOW_DYE,
            Items.LIME_DYE,
            Items.GREEN_DYE,
            Items.CYAN_DYE,
            Items.BLUE_DYE,
            Items.LIGHT_BLUE_DYE,
            Items.PURPLE_DYE,
            Items.PINK_DYE,
            Items.MAGENTA_DYE,
            Items.BROWN_DYE,
            Items.BLACK_DYE,
            Items.GRAY_DYE,
            Items.LIGHT_GRAY_DYE,
            Items.WHITE_DYE
    );

    static final List<Item> WOOL_ITEMS = List.of(
            Items.RED_WOOL,
            Items.ORANGE_WOOL,
            Items.YELLOW_WOOL,
            Items.LIME_WOOL,
            Items.GREEN_WOOL,
            Items.CYAN_WOOL,
            Items.BLUE_WOOL,
            Items.LIGHT_BLUE_WOOL,
            Items.PURPLE_WOOL,
            Items.PINK_WOOL,
            Items.MAGENTA_WOOL,
            Items.BROWN_WOOL,
            Items.BLACK_WOOL,
            Items.GRAY_WOOL,
            Items.LIGHT_GRAY_WOOL,
            Items.WHITE_WOOL
    );

    static final List<Item> LOG_ITEMS = List.of(
            Items.OAK_LOG,
            Items.BIRCH_LOG,
            Items.SPRUCE_LOG,
            Items.DARK_OAK_LOG,
            Items.JUNGLE_LOG,
            Items.ACACIA_LOG,
            Items.MANGROVE_LOG,
            Items.CHERRY_LOG,
            Items.STRIPPED_OAK_LOG,
            Items.STRIPPED_BIRCH_LOG,
            Items.STRIPPED_SPRUCE_LOG,
            Items.STRIPPED_DARK_OAK_LOG,
            Items.STRIPPED_JUNGLE_LOG,
            Items.STRIPPED_ACACIA_LOG,
            Items.STRIPPED_MANGROVE_LOG,
            Items.STRIPPED_CHERRY_LOG
    );

    static final List<Item> WOOD_ITEMS = List.of(
            Items.OAK_WOOD,
            Items.BIRCH_WOOD,
            Items.SPRUCE_WOOD,
            Items.DARK_OAK_WOOD,
            Items.JUNGLE_WOOD,
            Items.ACACIA_WOOD,
            Items.MANGROVE_WOOD,
            Items.CHERRY_WOOD,
            Items.STRIPPED_OAK_WOOD,
            Items.STRIPPED_BIRCH_WOOD,
            Items.STRIPPED_SPRUCE_WOOD,
            Items.STRIPPED_DARK_OAK_WOOD,
            Items.STRIPPED_JUNGLE_WOOD,
            Items.STRIPPED_ACACIA_WOOD,
            Items.STRIPPED_MANGROVE_WOOD,
            Items.STRIPPED_CHERRY_WOOD
    );
}
