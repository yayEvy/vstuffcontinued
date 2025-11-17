package yay.evy.everest.vstuff.index;

import net.minecraft.network.chat.Component;

import yay.evy.everest.vstuff.content.ropestyler.components.RopeStyleCategory;
import yay.evy.everest.vstuff.util.RopeStyles;
import yay.evy.everest.vstuff.util.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.util.RopeStyles.PrimitiveRopeStyle;

import java.util.*;
public class VStuffRopeStyles {

    private static final RopeStyle NORMAL = new RopeStyle("normal", PrimitiveRopeStyle.NORMAL, "vstuff.rope.normal");
    private static final RopeStyle CHAIN = new RopeStyle("chain", PrimitiveRopeStyle.CHAIN, "block.minecraft.chain");

    private static final RopeStyle LIGHT_GRAY = new RopeStyle("light_gray_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.light_gray");
    private static final RopeStyle GRAY = new RopeStyle("gray_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.gray");
    private static final RopeStyle BLACK = new RopeStyle("black_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.black");
    private static final RopeStyle BROWN = new RopeStyle("brown_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.brown");
    private static final RopeStyle RED = new RopeStyle("red_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.red");
    private static final RopeStyle ORANGE = new RopeStyle("orange_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.orange");
    private static final RopeStyle YELLOW = new RopeStyle("yellow_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.yellow");
    private static final RopeStyle LIME = new RopeStyle("lime_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.lime");
    private static final RopeStyle GREEN = new RopeStyle("green_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.green");
    private static final RopeStyle CYAN = new RopeStyle("cyan_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.cyan");
    private static final RopeStyle LIGHT_BLUE = new RopeStyle("light_blue_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.light_blue");
    private static final RopeStyle BLUE = new RopeStyle("blue_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.blue");
    private static final RopeStyle PURPLE = new RopeStyle("purple_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.purple");
    private static final RopeStyle MAGENTA = new RopeStyle("magenta_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.magenta");
    private static final RopeStyle PINK = new RopeStyle("pink_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.pink");
    private static final RopeStyle WHITE = new RopeStyle("white_dye", PrimitiveRopeStyle.DYED, "vstuff.rope.white");

    private static final RopeStyle LIGHT_GRAY_WOOL = new RopeStyle("light_gray_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.light_gray_wool");
    private static final RopeStyle GRAY_WOOL = new RopeStyle("gray_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.gray_wool");
    private static final RopeStyle BLACK_WOOL = new RopeStyle("black_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.black_wool");
    private static final RopeStyle BROWN_WOOL = new RopeStyle("brown_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.brown_wool");
    private static final RopeStyle RED_WOOL = new RopeStyle("red_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.red_wool");
    private static final RopeStyle ORANGE_WOOL = new RopeStyle("orange_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.orange_wool");
    private static final RopeStyle YELLOW_WOOL = new RopeStyle("yellow_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.yellow_wool");
    private static final RopeStyle LIME_WOOL = new RopeStyle("lime_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.lime_wool");
    private static final RopeStyle GREEN_WOOL = new RopeStyle("green_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.green_wool");
    private static final RopeStyle CYAN_WOOL = new RopeStyle("cyan_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.cyan_wool");
    private static final RopeStyle LIGHT_BLUE_WOOL = new RopeStyle("light_blue_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.light_blue_wool");
    private static final RopeStyle BLUE_WOOL = new RopeStyle("blue_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.blue_wool");
    private static final RopeStyle PURPLE_WOOL = new RopeStyle("purple_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.purple_wool");
    private static final RopeStyle MAGENTA_WOOL = new RopeStyle("magenta_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.magenta_wool");
    private static final RopeStyle PINK_WOOL = new RopeStyle("pink_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.pink_wool");
    private static final RopeStyle WHITE_WOOL = new RopeStyle("white_wool", PrimitiveRopeStyle.WOOL, "block.minecraft.white_wool");

    private static final RopeStyle ANDESITE = new RopeStyle("andesite", PrimitiveRopeStyle.OTHER, "block.create.andesite_casing");
    private static final RopeStyle BRASS = new RopeStyle("brass", PrimitiveRopeStyle.OTHER, "block.create.brass_casing");
    private static final RopeStyle COPPER = new RopeStyle("copper", PrimitiveRopeStyle.OTHER, "block.create.copper_casing");
    private static final RopeStyle TRAIN = new RopeStyle("railway", PrimitiveRopeStyle.OTHER, "block.create.railway_casing");
    private static final RopeStyle INDUSTRIAL = new RopeStyle("industrial", PrimitiveRopeStyle.OTHER, "vstuff.rope.industrial");

    private static final RopeStyle PRIDE = new RopeStyle("pride", PrimitiveRopeStyle.OTHER, "vstuff.rope.pride");
    private static final RopeStyle GAY = new RopeStyle("gay", PrimitiveRopeStyle.OTHER, "vstuff.rope.gay");
    private static final RopeStyle LESBIAN = new RopeStyle("lesbian", PrimitiveRopeStyle.OTHER, "vstuff.rope.lesbian");
    private static final RopeStyle BISEXUAL = new RopeStyle("bisexual", PrimitiveRopeStyle.OTHER, "vstuff.rope.bisexual");
    private static final RopeStyle TRANSGENDER = new RopeStyle("transgender", PrimitiveRopeStyle.OTHER, "vstuff.rope.transgender");
    private static final RopeStyle NONBINARY = new RopeStyle("nonbinary", PrimitiveRopeStyle.OTHER, "vstuff.rope.nonbinary");
    private static final RopeStyle ASEXUAL = new RopeStyle("asexual", PrimitiveRopeStyle.OTHER, "vstuff.rope.asexual");

    private static final RopeStyle OAK = new RopeStyle("oak", PrimitiveRopeStyle.LOG, "block.minecraft.oak_log");
    private static final RopeStyle OAK_STRIPPED = new RopeStyle("stripped_oak", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_oak_log");
    private static final RopeStyle BIRCH = new RopeStyle("birch", PrimitiveRopeStyle.LOG, "block.minecraft.birch_log");
    private static final RopeStyle BIRCH_STRIPPED = new RopeStyle("stripped_birch", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_birch_log");
    private static final RopeStyle SPRUCE = new RopeStyle("spruce", PrimitiveRopeStyle.LOG, "block.minecraft.spruce_log");
    private static final RopeStyle SPRUCE_STRIPPED = new RopeStyle("stripped_spruce", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_spruce_log");
    private static final RopeStyle DARK_OAK = new RopeStyle("dark_oak", PrimitiveRopeStyle.LOG, "block.minecraft.dark_oak_log");
    private static final RopeStyle DARK_OAK_STRIPPED = new RopeStyle("stripped_dark_oak", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_dark_oak_log");
    private static final RopeStyle JUNGLE = new RopeStyle("jungle", PrimitiveRopeStyle.LOG, "block.minecraft.jungle_log");
    private static final RopeStyle JUNGLE_STRIPPED = new RopeStyle("stripped_jungle", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_jungle_log");
    private static final RopeStyle ACACIA = new RopeStyle("acacia", PrimitiveRopeStyle.LOG, "block.minecraft.acacia_log");
    private static final RopeStyle ACACIA_STRIPPED = new RopeStyle("stripped_acacia", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_acacia_log");
    private static final RopeStyle CHERRY = new RopeStyle("cherry", PrimitiveRopeStyle.LOG, "block.minecraft.cherry_log");
    private static final RopeStyle CHERRY_STRIPPED = new RopeStyle("stripped_cherry", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_cherry_log");
    private static final RopeStyle MANGROVE = new RopeStyle("mangrove", PrimitiveRopeStyle.LOG, "block.minecraft.mangrove_log");
    private static final RopeStyle MANGROVE_STRIPPED = new RopeStyle("stripped_mangrove", PrimitiveRopeStyle.LOG, "block.minecraft.stripped_mangrove_log");

    private static final RopeStyleCategory BASIC_STYLES = new RopeStyleCategory(
            "vstuff.rope_category.normal",
            new RopeStyle[]{
                    NORMAL, CHAIN
            }
    );

    private static final RopeStyleCategory COLORED_STYLES = new RopeStyleCategory(
            "vstuff.rope_category.colored",
            new  RopeStyle[]{
                    LIGHT_GRAY, GRAY, BLACK, BROWN, RED, ORANGE, YELLOW, LIME,
                    GREEN, CYAN, LIGHT_BLUE, BLUE, PURPLE, MAGENTA, PINK, WHITE
            }
    );

    private static final RopeStyleCategory COLORED_WOOL_STYLES = new RopeStyleCategory(
            "vstuff.rope_category.colored_wool",
            new  RopeStyle[]{
                    LIGHT_GRAY_WOOL, GRAY_WOOL, BLACK_WOOL, BROWN_WOOL, RED_WOOL, ORANGE_WOOL, YELLOW_WOOL, LIME_WOOL,
                    GREEN_WOOL, CYAN_WOOL, LIGHT_BLUE_WOOL, BLUE_WOOL, PURPLE_WOOL, MAGENTA_WOOL, PINK_WOOL, WHITE_WOOL
            }
    );

    private static final RopeStyleCategory CASING_STYLES = new RopeStyleCategory(
            "vstuff.rope_category.casing",
            new RopeStyle[]{
                    ANDESITE, BRASS, COPPER, TRAIN, INDUSTRIAL
            }
    );

    private static final RopeStyleCategory FLAGS = new RopeStyleCategory(
            "vstuff.rope_category.pride_flags",
            new RopeStyle[]{
                    PRIDE, GAY, LESBIAN, BISEXUAL, TRANSGENDER, NONBINARY, ASEXUAL
            }
    );

    private static final RopeStyleCategory LOGS = new RopeStyleCategory(
            "vstuff.rope_category.logs",
            new RopeStyle[]{
                    OAK, OAK_STRIPPED, BIRCH, BIRCH_STRIPPED, SPRUCE, SPRUCE_STRIPPED, DARK_OAK, DARK_OAK_STRIPPED,
                    JUNGLE, JUNGLE_STRIPPED, ACACIA, ACACIA_STRIPPED, CHERRY, CHERRY_STRIPPED, MANGROVE,
                    MANGROVE_STRIPPED
            }
    );

    public static List<RopeStyleCategory> CATEGORIES = new ArrayList<>();

    public static void register() {
        CATEGORIES.add(BASIC_STYLES);
        CATEGORIES.add(COLORED_STYLES);
        CATEGORIES.add(COLORED_WOOL_STYLES);
        CATEGORIES.add(CASING_STYLES);
        CATEGORIES.add(FLAGS);
        CATEGORIES.add(LOGS);
    }
}

