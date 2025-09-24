package yay.evy.everest.vstuff.index;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yay.evy.everest.vstuff.content.rope_changer_menu.components.RopeStyleCategory;
import yay.evy.everest.vstuff.util.RopeStyles;
import yay.evy.everest.vstuff.util.RopeStyles.RopeStyle;
import yay.evy.everest.vstuff.util.RopeStyles.PrimitiveRopeStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VStuffRopeStyles {

    public static final RopeStyle NORMAL = new RopeStyle("normal", PrimitiveRopeStyle.NORMAL, "vstuff.ropes.normal");
    public static final RopeStyle PLAIN = new RopeStyle("plain", PrimitiveRopeStyle.NORMAL, "vstuff.ropes.plain");
    public static final RopeStyle CHAIN = new RopeStyle("chain", PrimitiveRopeStyle.CHAIN, "minecraft.block.chain");
    public static final RopeStyle WOOL = new RopeStyle("white", PrimitiveRopeStyle.WOOL, "vstuff.ropes.wool");

    public static final RopeStyle ANDESITE = new RopeStyle("andesite", PrimitiveRopeStyle.OTHER, "create.block.andesite_casing");
    public static final RopeStyle BRASS = new RopeStyle("brass", PrimitiveRopeStyle.OTHER, "create.block.brass_casing");
    public static final RopeStyle COPPER = new RopeStyle("copper", PrimitiveRopeStyle.OTHER, "create.block.copper_casing");
    public static final RopeStyle TRAIN = new RopeStyle("railway", PrimitiveRopeStyle.OTHER, "create.block.railway_casing");
    public static final RopeStyle INDUSTRIAL = new RopeStyle("industrial", PrimitiveRopeStyle.OTHER, "create.block.industrial_iron");


    private static final RopeStyleCategory BASIC_STYLES = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.normal"),
            new RopeStyles.RopeStyle[]{
                    NORMAL, PLAIN, CHAIN, WOOL
            }
    );

    private static final RopeStyleCategory CASING_STYLES = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.casing"),
            new RopeStyles.RopeStyle[]{
                    ANDESITE, BRASS, COPPER, TRAIN, INDUSTRIAL, ANDESITE, BRASS, COPPER, TRAIN, INDUSTRIAL
            }
    );

    private static final RopeStyleCategory FLAGS = new RopeStyleCategory(
            Component.translatable("vstuff.rope_category.flags")
    );


    public static List<RopeStyleCategory> CATEGORIES = Arrays.stream(new RopeStyleCategory[]{
            BASIC_STYLES, CASING_STYLES, FLAGS
    }).toList();
}