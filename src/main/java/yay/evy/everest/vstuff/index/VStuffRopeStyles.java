package yay.evy.everest.vstuff.index;

import net.minecraft.network.chat.Component;

import yay.evy.everest.vstuff.content.ropestyler.components.RopeStyleCategory;
import yay.evy.everest.vstuff.util.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.util.RopeStyles.PrimitiveRopeStyle;

import java.util.ArrayList;
import java.util.List;

public class VStuffRopeStyles {

    private static final RopeStyle NORMAL = new RopeStyle("normal", PrimitiveRopeStyle.NORMAL, "vstuff.rope.normal");
    private static final RopeStyle PLAIN = new RopeStyle("plain", PrimitiveRopeStyle.NORMAL, "vstuff.rope.plain");
    private static final RopeStyle CHAIN = new RopeStyle("chain", PrimitiveRopeStyle.CHAIN, "block.minecraft.chain");
    private static final RopeStyle WOOL = new RopeStyle("white", PrimitiveRopeStyle.WOOL, "vstuff.rope.wool");

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
        Component.translatable("vstuff.rope_category.normal"),
        new RopeStyle[]{
                NORMAL, PLAIN, CHAIN, WOOL
        }
    );

    private static final RopeStyleCategory CASING_STYLES = new RopeStyleCategory(
        Component.translatable("vstuff.rope_category.casing"),
        new RopeStyle[]{
                ANDESITE, BRASS, COPPER, TRAIN, INDUSTRIAL
        }
    );

    private static final RopeStyleCategory FLAGS = new RopeStyleCategory(
        Component.translatable("vstuff.rope_category.pride_flags"),
        new RopeStyle[]{
            PRIDE, GAY, LESBIAN, BISEXUAL, TRANSGENDER, NONBINARY, ASEXUAL
        }
    );

    private static final RopeStyleCategory LOGS = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.logs"),
            new RopeStyle[]{
                OAK, OAK_STRIPPED, BIRCH, BIRCH_STRIPPED, SPRUCE, SPRUCE_STRIPPED, DARK_OAK, DARK_OAK_STRIPPED,
                    JUNGLE, JUNGLE_STRIPPED, ACACIA, ACACIA_STRIPPED, CHERRY, CHERRY_STRIPPED, MANGROVE,
                    MANGROVE_STRIPPED
            }
    );


    public static List<RopeStyleCategory> CATEGORIES = new ArrayList<>();

    public static void register() {
        CATEGORIES.add(BASIC_STYLES);
        CATEGORIES.add(CASING_STYLES);
        CATEGORIES.add(FLAGS);
        CATEGORIES.add(LOGS);
    }
}

